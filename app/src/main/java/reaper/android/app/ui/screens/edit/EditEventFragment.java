package reaper.android.app.ui.screens.edit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
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
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.create.LocationSuggestionAdapter;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.DateTimeUtils;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;


public class EditEventFragment extends BaseFragment implements EditEventView,
        LocationSuggestionAdapter.SuggestionClickListener, TimePickerDialog.OnTimeSetListener
{
    private static final String ARG_EVENT = "arg_event";
    private static final String ARG_EVENT_DETAILS = "arg_event_details";

    public static EditEventFragment newInstance(Event event, EventDetails eventDetails)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putSerializable(ARG_EVENT_DETAILS, eventDetails);

        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /* UI Elements */
    ScrollView parent;
    Toolbar toolbar;

    TextView title;
    TextView type;
    EditText description;

    ImageView icon;
    View iconContainer;

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
    TextWatcher locationListener;
    ImageView locationIcon;
    boolean isLocationUpdating;

    RecyclerView suggestionList;
    View suggestionContainer;

    ProgressDialog progressDialog;

    boolean isFinalizeOptionVisible;
    boolean isUnfinalizeOptionVisible;
    MenuItem finalize;

    boolean isDeleteOptionVisible;
    MenuItem delete;

    /* Data */
    DateTimeUtils dateTimeUtils;
    boolean isFinalized;
    LocalDate startDate;
    LocalTime startTime;

    ClickListener clickListener;

    EditEventPresenter presenter;

    Bus bus;
    UserService userService;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        dateTimeUtils = new DateTimeUtils();

        bus = Communicator.getInstance().getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        parent = (ScrollView) view.findViewById(R.id.sv_editEvent);
        toolbar = (Toolbar) view.findViewById(R.id.tb_editEvent);

        title = (TextView) view.findViewById(R.id.tv_editEvent_title);
        description = (EditText) view.findViewById(R.id.et_editEvent_description);

        icon = (ImageView) view.findViewById(R.id.iv_editEvent_icon);
        iconContainer = view.findViewById(R.id.ll_create_event_iconContainer);

        type = (TextView) view.findViewById(R.id.tv_editEvent_type);

        day = (TextView) view.findViewById(R.id.tv_editEvent_day);
        dayIcon = (ImageView) view.findViewById(R.id.iv_dayIcon);
        dayContainer = view.findViewById(R.id.ll_editEvent_dayContainer);

        time = (TextView) view.findViewById(R.id.tv_editEvent_time);
        timeIcon = (ImageView) view.findViewById(R.id.iv_timeIcon);
        timeContainer = view.findViewById(R.id.ll_editEvent_timeContainer);

        daySelector = (TabLayout) view.findViewById(R.id.tl_editEvent_daySelector);
        daySelectorContainer = view.findViewById(R.id.ll_editEvent_daySelectorContainer);
        timeSelector = (TabLayout) view.findViewById(R.id.tl_editEvent_timeSelector);
        timeSelectorContainer = view.findViewById(R.id.ll_editEvent_timeSelectorContainer);

        suggestionList = (RecyclerView) view.findViewById(R.id.rv_editEvent_locationSuggestions);
        suggestionContainer = view.findViewById(R.id.ll_editEvent_locationContainer);

        location = (EditText) view.findViewById(R.id.et_editEvent_location);
        locationIcon = (ImageView) view.findViewById(R.id.iv_editEvent_locationIcon);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Event event = (Event) getArguments().getSerializable(ARG_EVENT);
        EventDetails eventDetails = (EventDetails) getArguments()
                .getSerializable(ARG_EVENT_DETAILS);
        if (event == null || eventDetails == null)
        {
            throw new IllegalStateException("event/event_details is null");
        }

        presenter = new EditEventPresenterImpl(bus, event, eventDetails);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);

        setActionBarTitle(event);

        locationListener = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                parent.scrollTo(0, suggestionContainer.getBottom());

                if (!isLocationUpdating)
                {
                    if (s.length() == 0)
                    {
                        presenter.fetchSuggestions();
                    }
                    else if (s.length() >= 3)
                    {
                        presenter.autocomplete(s.toString());
                    }

                    presenter.setLocationName(s.toString());
                }
            }
        };
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);

        CacheManager.getGenericCache().put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.EDIT);

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

        location.addTextChangedListener(locationListener);

        isFinalizeOptionVisible = false;
        isUnfinalizeOptionVisible = false;
        isDeleteOptionVisible = false;

        presenter.attachView(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
        location.setOnFocusChangeListener(null);
        location.removeTextChangedListener(locationListener);
        presenter.detachView();
    }

    private void edit()
    {
        presenter.setDescription(description.getText().toString());
        presenter.edit();

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_EDITED, userService.getActiveUserId());
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

        daySelector.setOnTabSelectedListener(null);
        daySelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        daySelector.setTabGravity(TabLayout.GRAVITY_FILL);
        daySelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        daySelector.removeAllTabs();

        List<String> days = dateTimeUtils.getDayList();

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

                DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                presenter.updateTime(start);
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

                DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                presenter.updateTime(start);
            }
        });
    }

    private void initTimeSelector()
    {
        timeSelectorContainer.setVisibility(View.GONE);

        timeSelector.setOnTabSelectedListener(null);
        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
        timeSelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        timeSelector.removeAllTabs();

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
                            .newInstance(EditEventFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.dismissOnPause(true);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                }
                else
                {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));

                    DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                    presenter.updateTime(start);
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
                            .newInstance(EditEventFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.dismissOnPause(true);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                }
                else
                {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));

                    DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                    presenter.updateTime(start);
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

    @Override
    public void init(Event event, String description)
    {
        isFinalized = event.isFinalized();

        startDate = event.getStartTime().toLocalDate();
        startTime = event.getStartTime().toLocalTime();
        initDayTime();

        title.setText(event.getTitle());
        this.description.setText(description);

        String locationName = event.getLocation().getName();
        if (locationName != null)
        {
            setLocation(locationName);
        }


        if (event.getType() == Event.Type.PUBLIC)
        {
            type.setText(R.string.event_details_type_public);
        }
        else
        {
            type.setText(R.string.event_details_type_invite_only);
        }

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

        icon.setImageDrawable(DrawableFactory
                .get(EventCategory
                        .valueOf(event.getCategory()), Dimensions.CREATE_EVENT_ICON_SIZE));
        iconContainer.setBackground(DrawableFactory.randomIconBackground());
    }

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
    public void setLocation(String locationName)
    {
        isLocationUpdating = true;
        location.setText(locationName);
        location.setSelection(location.length());
        isLocationUpdating = false;

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);

        presenter.fetchSuggestions();
    }

    @Override
    public void displayDeleteOption()
    {
        if (delete != null && !isDeleteOptionVisible)
        {
            Drawable deleteDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                                                             .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                             .setSizeDp(36)
                                                             .build();
            delete.setIcon(deleteDrawable);
            delete.setVisible(true);
        }

        isDeleteOptionVisible = true;
    }

    @Override
    public void displayFinalizationOption()
    {
        if (finalize != null && !isFinalizeOptionVisible)
        {
            Drawable lockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                                                             .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                             .setSizeDp(36)
                                                             .build();

            finalize.setIcon(lockedDrawable);
            finalize.setVisible(true);
        }

        isFinalizeOptionVisible = true;
    }

    @Override
    public void showLoading()
    {
        progressDialog = ProgressDialog.show(getActivity(), "Updating Clan", "Please Wait..");
    }

    @Override
    public void displayUnfinalizationOption()
    {
        if (finalize != null && !isUnfinalizeOptionVisible)
        {
            Drawable unlockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                               .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                                                               .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                               .setSizeDp(36)
                                                               .build();

            finalize.setIcon(unlockedDrawable);
            finalize.setVisible(true);
        }

        isUnfinalizeOptionVisible = true;
    }


    @Override
    public void displayEventLockedError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), R.string.cannot_edit_event_locked, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void displayError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), R.string.messed_up, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void navigateToDetailsScreen(List<Event> events, int activePosition)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getFragmentManager(), eventDetailsContainerFragment);
    }

    @Override
    public void navigateToHomeScreen()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        FragmentUtils.changeFragment(getFragmentManager(), new HomeFragment());
    }

    @Override
    public void onSuggestionClicked(Suggestion suggestion)
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.SUGGESTION_CLICKED_EDIT, userService.getActiveUserId());

        presenter.selectSuggestion(suggestion);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
    {
        startTime = new LocalTime(hourOfDay, minute);
        time.setText(dateTimeUtils.formatTime(startTime));

        DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
        presenter.updateTime(start);
    }

    private class ClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {

            SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_edit, menu);

        menu.findItem(R.id.action_edit)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {

                    SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                    edit();
                    return true;
                }
            });

        finalize = menu.findItem(R.id.action_finalize);
        delete = menu.findItem(R.id.action_delete);

        finalize.setVisible(false);
        delete.setVisible(false);

        if(isDeleteOptionVisible)
        {
            Drawable deleteDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                                                             .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                             .setSizeDp(36)
                                                             .build();
            delete.setIcon(deleteDrawable);
            delete.setVisible(true);
        }

        if(isFinalizeOptionVisible)
        {
            Drawable lockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                                                             .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                             .setSizeDp(36)
                                                             .build();

            finalize.setIcon(lockedDrawable);
            finalize.setVisible(true);
        }
        else if(isUnfinalizeOptionVisible)
        {
            Drawable unlockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                               .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                                                               .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                                                               .setSizeDp(36)
                                                               .build();

            finalize.setIcon(unlockedDrawable);
            finalize.setVisible(true);
        }

        finalize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                if (isFinalized)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.event_unlock_heading);
                    builder.setMessage(R.string.event_unlock_message);

                    builder.setPositiveButton(R.string.event_unlock_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            presenter.unfinalizeEvent();

                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_UNFINALIZED, userService.getActiveUserId());
                        }
                    });

                    builder.setNegativeButton(R.string.event_unlock_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.event_lock_heading);
                    builder.setMessage(R.string.event_lock_message);

                    builder.setPositiveButton(R.string.event_lock_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            presenter.finalizeEvent();

                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_FINALIZED, userService.getActiveUserId());
                        }
                    });

                    builder.setNegativeButton(R.string.event_lock_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                return true;
            }
        });

        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setTitle(R.string.event_delete_heading);
                builder.setMessage(R.string.event_delete_message);

                builder.setPositiveButton(R.string.even_delete_positive_button, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        presenter.delete();

                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_DELETED, userService.getActiveUserId());
                    }
                });

                builder.setNegativeButton(R.string.even_delete_negative_button, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            ((MainActivity)getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onBackPressedTrigger(BackPressedTrigger trigger)
    {
        if(trigger.getActiveFragment() == BackstackTags.EDIT)
        {
            presenter.initiateEventDetailsNavigation();
        }
    }

    private void setActionBarTitle(Event event)
    {
        switch (event.getCategory()) {
            case "CAFE":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Cafe");
                break;
            case "MOVIES":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Movie");
                break;
            case "SHOPPING":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Shopping");
                break;
            case "SPORTS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Sports");
                break;
            case "INDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Indoors");
                break;
            case "EAT_OUT":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Eat Out");
                break;
            case "DRINKS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Drinks");
                break;
            case "OUTDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Outdoors");
                break;
            case "GENERAL":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("General");
                break;
        }
    }
}
