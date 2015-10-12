package reaper.android.app.ui.screens.create;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.DateTimeUtils;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.communicator.Communicator;
import rx.Subscriber;
import rx.Subscription;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CreateEventDetailsFragment extends BaseFragment implements CreateEventView,
        LocationSuggestionAdapter.SuggestionClickListener,
        TimePickerDialog.OnTimeSetListener
{
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_START_DAY = "arg_start_day";
    private static final String ARG_START_TIME = "arg_start_time";

    public static CreateEventDetailsFragment newInstance(String title, EventCategory category,
                                                         Event.Type type, LocalDate startDay, LocalTime startTime)
    {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putSerializable(ARG_CATEGORY, category);
        args.putSerializable(ARG_TYPE, type);
        args.putSerializable(ARG_START_DAY, startDay);
        args.putSerializable(ARG_START_TIME, startTime);

        CreateEventDetailsFragment fragment = new CreateEventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /* UI Elements */
    ScrollView parent;
    Toolbar toolbar;

    EditText title;
    EditText description;

    ImageView icon;
    View iconContainer;

    TabLayout typeSelector;

    TextView time;
    ImageView timeIcon;
    View timeContainer;

    TextView day;
    ImageView dayIcon;
    View dayContainer;

    View daySelectorContainer;
    TabLayout daySelector;
    boolean isDaySelectorVisible;

    View timeSelectorContainer;
    TabLayout timeSelector;
    boolean isTimeSelectorVisible;

    EditText location;
    ImageView locationIcon;
    boolean isLocationUpdating;

    RecyclerView suggestionList;
    View suggestionContainer;

    ProgressDialog progressDialog;

    /* Data */
    DateTimeUtils dateTimeUtils;

    EventCategory category;
    Event.Type type;
    Location eventLocation;
    LocalTime startTime;
    LocalDate startDate;

    /* Subscriptions */
    CompositeSubscription subscriptions;

    /* Listeners */
    ClickListener clickListener;

    /* Presenter */
    CreateEventPresenter presenter;

    /* View Methods */
    @Override
    public void displaySuggestions(List<Suggestion> suggestions)
    {
        suggestionList.setAdapter(new LocationSuggestionAdapter(suggestions, this));

        if (suggestions.isEmpty())
        {
            suggestionContainer.setVisibility(View.GONE);
        }
        else
        {
            suggestionContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLocation(Location loc)
    {
        eventLocation = loc;
        isLocationUpdating = true;
        location.setText(loc.getName());
        location.setSelection(location.length());
        isLocationUpdating = false;

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
    }

    @Override
    public void onSuggestionClicked(Suggestion suggestion)
    {
        presenter.selectSuggestion(suggestion);
    }

    @Override
    public void showLoading()
    {
        progressDialog = ProgressDialog
                .show(getActivity(), "Creating your clan", "Please wait ...");
    }

    @Override
    public void displayEmptyTitleError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Title cannot be empty", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void displayInvalidTimeError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Start time cannot be before the current time", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void navigateToInviteScreen(Event event)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, event);
        bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, true);
        inviteUsersContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getFragmentManager(), inviteUsersContainerFragment);
    }

    @Override
    public void displayError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Unable to create event", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
    {
        startTime = new LocalTime(hourOfDay, minute);
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        subscriptions = new CompositeSubscription();
        dateTimeUtils = new DateTimeUtils();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_details, container, false);

        parent = (ScrollView) view.findViewById(R.id.sv_createEvent);
        toolbar = (Toolbar) view.findViewById(R.id.tb_createEvent);

        title = (EditText) view.findViewById(R.id.et_createEvent_title);
        description = (EditText) view.findViewById(R.id.et_createEvent_description);

        icon = (ImageView) view.findViewById(R.id.iv_createEvent_icon);
        iconContainer = view.findViewById(R.id.ll_create_event_iconContainer);

        typeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_eventType);

        day = (TextView) view.findViewById(R.id.tv_createEvent_day);
        dayIcon = (ImageView) view.findViewById(R.id.iv_dayIcon);
        dayContainer = view.findViewById(R.id.ll_createEvent_dayContainer);

        time = (TextView) view.findViewById(R.id.tv_createEvent_time);
        timeIcon = (ImageView) view.findViewById(R.id.iv_timeIcon);
        timeContainer = view.findViewById(R.id.ll_createEvent_timeContainer);

        daySelector = (TabLayout) view.findViewById(R.id.tl_createEvent_daySelector);
        daySelectorContainer = view.findViewById(R.id.ll_createEvent_daySelectorContainer);
        timeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_timeSelector);
        timeSelectorContainer = view.findViewById(R.id.ll_createEvent_timeSelectorContainer);

        suggestionList = (RecyclerView) view.findViewById(R.id.rv_createEvent_locationSuggestions);
        suggestionContainer = view.findViewById(R.id.ll_createEvent_locationContainer);

        location = (EditText) view.findViewById(R.id.et_createEvent_location);
        locationIcon = (ImageView) view.findViewById(R.id.iv_createEvent_locationIcon);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        String inputTitle = getArguments().getString(ARG_TITLE);
        if (inputTitle != null && !inputTitle.isEmpty())
        {
            title.setText(inputTitle);
            title.setSelection(title.getText().length());
        }

        type = (Event.Type) getArguments().getSerializable(ARG_TYPE);
        if (type == null)
        {
            type = Event.Type.INVITE_ONLY;
        }
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_invite_only));
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_public));

        if (type == Event.Type.PUBLIC)
        {
            typeSelector.getTabAt(0).select();
        }
        else
        {
            typeSelector.getTabAt(1).select();
        }

        category = (EventCategory) getArguments().getSerializable(ARG_CATEGORY);
        if (category == null)
        {
            category = EventCategory.GENERAL;
        }

        icon.setImageDrawable(DrawableFactory
                .get(category, Dimensions.CREATE_EVENT_ICON_SIZE));
        iconContainer.setBackground(DrawableFactory.randomIconBackground());

        dayIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
                                                        .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR)
                                                        .setColor(ContextCompat
                                                                .getColor(getActivity(), R.color.primary))
                                                        .setSizeDp(24).build());

        timeIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
                                                         .setIcon(MaterialDrawableBuilder.IconValue.CLOCK)
                                                         .setColor(ContextCompat
                                                                 .getColor(getActivity(), R.color.primary))
                                                         .setSizeDp(24).build());

        locationIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.MAP_MARKER)
                                                             .setColor(ContextCompat
                                                                     .getColor(getActivity(), R.color.primary))
                                                             .setSizeDp(24).build());

        clickListener = new ClickListener();
        dayContainer.setOnClickListener(clickListener);
        timeContainer.setOnClickListener(clickListener);

        initDaySelector();
        initTimeSelector();
        initRecyclerView();

        presenter = new CreateEventPresenterImpl(Communicator.getInstance().getBus(), category);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        title.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        title.clearFocus();

        CacheManager.getGenericCache().put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.CREATE);

        presenter.attachView(this);
        initDayTime();

        location.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    parent.scrollTo(0, suggestionContainer.getBottom());
                }
            }
        });

        Subscription subscription = WidgetObservable
                .text(location)
                .map(new Func1<OnTextChangeEvent, String>()
                {
                    @Override
                    public String call(OnTextChangeEvent onTextChangeEvent)
                    {
                        return onTextChangeEvent.text()
                                                .toString();
                    }
                })
                .subscribe(new Subscriber<String>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(String s)
                    {
                        parent.scrollTo(0, suggestionContainer.getBottom());

                        if (!isLocationUpdating)
                        {
                            if (s.length() == 0)
                            {
                                presenter.changeCategory(EventCategory.EAT_OUT);
                            }
                            else if (s.length() >= 3)
                            {
                                presenter.autocomplete(s);
                            }
                        }
                    }
                });

        typeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                String selected = tab.getText().toString();
                if (selected.equalsIgnoreCase(getResources()
                        .getString(R.string.event_details_type_public)))
                {
                    type = Event.Type.PUBLIC;
                }
                else
                {
                    type = Event.Type.INVITE_ONLY;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
            }
        });

        subscriptions.add(subscription);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        location.setOnFocusChangeListener(null);
        typeSelector.setOnTabSelectedListener(null);
        subscriptions.clear();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_create, menu);

        menu.findItem(R.id.action_create)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    String eventTitle = title.getText().toString();
                    String eventDescription = description.getText().toString();
                    DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                    DateTime end = DateTimeUtils.getEndTime(start);
                    Timber.v("Title : " + eventTitle);
                    Timber.v("Start Time : " + start);
                    Timber.v("End Time : " + end);
                    Timber.v("Location : " + location.getText().toString());
                    Timber.v("Type : " + type);
                    Timber.v("Category : " + category);

                    presenter
                            .create(eventTitle, type, category, eventDescription, start, end, eventLocation);

                    return true;
                }
            });
    }

    private void initRecyclerView()
    {
        suggestionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        suggestionList.setAdapter(new LocationSuggestionAdapter(new ArrayList<Suggestion>(), this));

        suggestionContainer.setVisibility(View.GONE);
    }

    private void initDayTime()
    {
        day.setText(dateTimeUtils.formatDate(startDate));
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    private void initDaySelector()
    {
        daySelectorContainer.setVisibility(View.GONE);

        daySelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        daySelector.setTabGravity(TabLayout.GRAVITY_FILL);
        daySelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        daySelector.removeAllTabs();

        List<String> days = dateTimeUtils.getDayList();

        startDate = (LocalDate) getArguments().getSerializable(ARG_START_DAY);
        if (startDate == null)
        {
            startDate = dateTimeUtils.getDate(days.get(0));
        }

        for (String day : days)
        {
            daySelector.addTab(daySelector.newTab().setText(day));
        }

        daySelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                String key = tab.getText().toString();
                day.setText(key);
                startDate = dateTimeUtils.getDate(key);
                hideDaySelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                String key = tab.getText().toString();
                day.setText(key);
                startDate = dateTimeUtils.getDate(key);
                hideDaySelector();
            }
        });
    }

    private void initTimeSelector()
    {
        timeSelectorContainer.setVisibility(View.GONE);

        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
        timeSelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        timeSelector.removeAllTabs();

        startTime = (LocalTime) getArguments().getSerializable(ARG_START_TIME);
        if (startTime == null)
        {
            LocalTime now = LocalTime.now();
            startTime = now.plusHours(1).withField(DateTimeFieldType.minuteOfHour(), 0);
        }

        List<String> times = dateTimeUtils.getTimeList();
        for (String time : times)
        {
            timeSelector.addTab(timeSelector.newTab().setText(time));
        }

        timeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                String key = tab.getText().toString();
                if (key.equalsIgnoreCase(DateTimeUtils.PICK_YOUR_OWN))
                {
                    TimePickerDialog dialog = TimePickerDialog
                            .newInstance(CreateEventDetailsFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                }
                else
                {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));
                }

                hideTimeSelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                String key = tab.getText().toString();
                if (key.equalsIgnoreCase(DateTimeUtils.PICK_YOUR_OWN))
                {
                    TimePickerDialog dialog = TimePickerDialog
                            .newInstance(CreateEventDetailsFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                }
                else
                {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));
                }

                hideTimeSelector();
            }
        });
    }

    private void showDaySelector()
    {
        VisibilityAnimationUtil.expand(daySelectorContainer, 200);
        isDaySelectorVisible = true;
    }

    private void hideDaySelector()
    {
        VisibilityAnimationUtil.collapse(daySelectorContainer, 200);
        isDaySelectorVisible = false;
    }

    private void showTimeSelector()
    {
        VisibilityAnimationUtil.expand(timeSelectorContainer, 200);
        isTimeSelectorVisible = true;
    }

    private void hideTimeSelector()
    {
        VisibilityAnimationUtil.collapse(timeSelectorContainer, 200);
        isTimeSelectorVisible = false;
    }

    private class ClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {

            if (v == dayContainer)
            {
                if (isTimeSelectorVisible)
                {
                    hideTimeSelector();
                }

                if (!isDaySelectorVisible)
                {
                    showDaySelector();
                }
                else
                {
                    hideDaySelector();
                }
            }
            else if (v == timeContainer)
            {
                if (isDaySelectorVisible)
                {
                    hideDaySelector();
                }

                if (!isTimeSelectorVisible)
                {
                    showTimeSelector();
                }
                else
                {
                    hideTimeSelector();
                }
            }
        }
    }
}
