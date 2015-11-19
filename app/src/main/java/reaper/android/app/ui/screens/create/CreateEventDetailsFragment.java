package reaper.android.app.ui.screens.create;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Bus;
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
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.DateTimeUtils;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class CreateEventDetailsFragment extends BaseFragment implements CreateEventView,
        LocationSuggestionAdapter.SuggestionClickListener,
        TimePickerDialog.OnTimeSetListener {
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_START_DAY = "arg_start_day";
    private static final String ARG_START_TIME = "arg_start_time";

    public static CreateEventDetailsFragment newInstance(String title, EventCategory category,
                                                         Event.Type type, LocalDate startDay, LocalTime startTime) {
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
    TextWatcher locationListener;
    ImageView locationIcon;
    boolean isLocationUpdating;

    RecyclerView suggestionList;
    View suggestionContainer;

    ProgressDialog progressDialog;

    /* Data */
    DateTimeUtils dateTimeUtils;

    Event.Type type;
    EventCategory category;
    LocalTime startTime;
    LocalDate startDate;

    UserService userService;
    Bus bus;

    /* Listeners */
    ClickListener clickListener;

    /* Presenter */
    CreateEventPresenter presenter;

    /* View Methods */
    @Override
    public void displaySuggestions(List<Suggestion> suggestions) {
        suggestionList.setAdapter(new LocationSuggestionAdapter(suggestions, this));

        if (suggestions.isEmpty()) {
            suggestionContainer.setVisibility(View.GONE);
        } else {
            suggestionContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLocation(String loc) {
        isLocationUpdating = true;
        location.setText(loc);
        location.setSelection(location.length());
        isLocationUpdating = false;

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);

        presenter.changeCategory(category);
    }

    @Override
    public void onSuggestionClicked(Suggestion suggestion) {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.SUGGESTION_CLICKED_CREATE_DETAILS, userService.getActiveUserId());

        presenter.selectSuggestion(suggestion);
    }

    @Override
    public void showLoading() {
        progressDialog = ProgressDialog
                .show(getActivity(), "Creating your clan", "Please wait ...");
    }

    @Override
    public void displayEmptyTitleError() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Title cannot be empty", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void displayInvalidTimeError() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Start time cannot be before the current time", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void navigateToInviteScreen(Event event) {
        if (progressDialog != null) {
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
    public void displayError() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Snackbar.make(getView(), "Unable to create clan", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        startTime = new LocalTime(hourOfDay, minute);
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateTimeUtils = new DateTimeUtils();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String inputTitle = getArguments().getString(ARG_TITLE);
        if (inputTitle != null && !inputTitle.isEmpty()) {
            title.setText(inputTitle);
            title.setSelection(title.getText().length());
        }

        type = (Event.Type) getArguments().getSerializable(ARG_TYPE);
        if (type == null) {
            type = Event.Type.INVITE_ONLY;
        }
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_invite_only));
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_public));

        if (type == Event.Type.PUBLIC) {
            typeSelector.getTabAt(0).select();
        } else {
            typeSelector.getTabAt(1).select();
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
        icon.setOnClickListener(clickListener);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);

        initDaySelector();
        initTimeSelector();
        initRecyclerView();

        category = (EventCategory) getArguments().getSerializable(ARG_CATEGORY);
        if (category == null) {
            category = EventCategory.GENERAL;
        }
        presenter = new CreateEventPresenterImpl(Communicator.getInstance().getBus(), category);

        icon.setImageDrawable(DrawableFactory
                .get(category, Dimensions.CREATE_EVENT_ICON_SIZE));
        iconContainer.setBackground(DrawableFactory.randomIconBackground());

        locationListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.scrollTo(0, suggestionContainer.getBottom());

                if (!isLocationUpdating) {
                    if (s.length() == 0) {
                        presenter.changeCategory(category);
                    } else if (s.length() >= 3) {
                        presenter.autocomplete(s.toString());
                    }

                    presenter.setLocationName(s.toString());
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        title.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        title.clearFocus();

        CacheManager.getGenericCache().put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.CREATE);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.CREATE_FRAGMENT);

        presenter.attachView(this);
        initDayTime();

        location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    parent.scrollTo(0, suggestionContainer.getBottom());
                }
            }
        });

        location.addTextChangedListener(locationListener);

        typeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selected = tab.getText().toString();
                if (selected.equalsIgnoreCase(getResources()
                        .getString(R.string.event_details_type_public))) {
                    type = Event.Type.PUBLIC;
                } else {
                    type = Event.Type.INVITE_ONLY;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        location.setOnFocusChangeListener(null);
        typeSelector.setOnTabSelectedListener(null);
        location.removeTextChangedListener(locationListener);
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_create, menu);

        menu.findItem(R.id.action_create)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                        String eventTitle = title.getText().toString();
                        String eventDescription = description.getText().toString();
                        DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
                        DateTime end = DateTimeUtils.getEndTime(start);

                        presenter
                                .create(eventTitle, type, eventDescription, start, end);

                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.CREATE_EVENT_CLICKED_FROM_DETAILS, userService.getActiveUserId());

                        return true;
                    }
                });
    }

    private void initRecyclerView() {
        suggestionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        suggestionList.setAdapter(new LocationSuggestionAdapter(new ArrayList<Suggestion>(), this));

        suggestionContainer.setVisibility(View.GONE);
    }

    private void initDayTime() {
        day.setText(dateTimeUtils.formatDate(startDate));
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    private void initDaySelector() {
        daySelectorContainer.setVisibility(View.GONE);

        daySelector.setOnTabSelectedListener(null);
        daySelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        daySelector.setTabGravity(TabLayout.GRAVITY_FILL);
        daySelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        daySelector.removeAllTabs();

        List<String> days = dateTimeUtils.getDayList();

        startDate = (LocalDate) getArguments().getSerializable(ARG_START_DAY);
        if (startDate == null) {
            startDate = dateTimeUtils.getDate(days.get(0));
        }

        for (String day : days) {
            daySelector.addTab(daySelector.newTab().setText(day));
        }

        daySelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String key = tab.getText().toString();
                day.setText(key);
                startDate = dateTimeUtils.getDate(key);
                hideDaySelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String key = tab.getText().toString();
                day.setText(key);
                startDate = dateTimeUtils.getDate(key);
                hideDaySelector();
            }
        });
    }

    private void initTimeSelector() {
        timeSelectorContainer.setVisibility(View.GONE);

        timeSelector.setOnTabSelectedListener(null);
        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
        timeSelector
                .setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));
        timeSelector.removeAllTabs();

        startTime = (LocalTime) getArguments().getSerializable(ARG_START_TIME);
        if (startTime == null) {
            LocalTime now = LocalTime.now();
            startTime = now.plusHours(1).withField(DateTimeFieldType.minuteOfHour(), 0);
        }

        List<String> times = dateTimeUtils.getTimeList();
        for (String time : times) {
            timeSelector.addTab(timeSelector.newTab().setText(time));
        }

        timeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String key = tab.getText().toString();
                if (key.equalsIgnoreCase(DateTimeUtils.PICK_YOUR_OWN)) {
                    TimePickerDialog dialog = TimePickerDialog
                            .newInstance(CreateEventDetailsFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.dismissOnPause(true);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                } else {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));
                }

                hideTimeSelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String key = tab.getText().toString();
                if (key.equalsIgnoreCase(DateTimeUtils.PICK_YOUR_OWN)) {
                    TimePickerDialog dialog = TimePickerDialog
                            .newInstance(CreateEventDetailsFragment.this, startTime
                                    .getHourOfDay(), startTime
                                    .getMinuteOfHour(), false);
                    dialog.dismissOnPause(true);
                    dialog.vibrate(false);
                    dialog.show(getFragmentManager(), "TimePicker");
                } else {
                    startTime = dateTimeUtils.getTime(key);
                    time.setText(dateTimeUtils.formatTime(startTime));
                }

                hideTimeSelector();
            }
        });
    }

    private void showDaySelector() {
        VisibilityAnimationUtil.expand(daySelectorContainer, 200);
        isDaySelectorVisible = true;
    }

    private void hideDaySelector() {
        VisibilityAnimationUtil.collapse(daySelectorContainer, 200);
        isDaySelectorVisible = false;
    }

    private void showTimeSelector() {
        VisibilityAnimationUtil.expand(timeSelectorContainer, 200);
        isTimeSelectorVisible = true;
    }

    private void hideTimeSelector() {
        VisibilityAnimationUtil.collapse(timeSelectorContainer, 200);
        isTimeSelectorVisible = false;
    }

    private void displayCategoryChangeDialog() {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_CATEGORY_CHANGE_DIALOG_CLICKED_FROM_DETAILS, userService.getActiveUserId());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.alert_dialog_change_category, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final LinearLayout cafe = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_cafe);
        final LinearLayout movies = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_movie);
        final LinearLayout eatOut = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_eat_out);
        final LinearLayout sports = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_sports);
        final LinearLayout outdoors = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_outdoors);
        final LinearLayout indoors = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_indoors);
        final LinearLayout drinks = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_drinks);
        final LinearLayout shopping = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_shopping);
        final LinearLayout general = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_general);

        cafe.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        movies.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        eatOut.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        sports.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        outdoors.setBackground(DrawableFactory
                .getIconBackground(getActivity(), R.color.primary, 4));
        indoors.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        drinks.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));
        shopping.setBackground(DrawableFactory
                .getIconBackground(getActivity(), R.color.primary, 4));
        general.setBackground(DrawableFactory.getIconBackground(getActivity(), R.color.primary, 4));

        cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.CAFE);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.CAFE, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });

        movies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.MOVIES);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.MOVIES, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        eatOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.EAT_OUT);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.EAT_OUT, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        sports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.SPORTS);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.SPORTS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        outdoors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.OUTDOORS);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.OUTDOORS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        indoors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.INDOORS);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.INDOORS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        drinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.DRINKS);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.DRINKS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.SHOPPING);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.SHOPPING, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        general.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(EventCategory.GENERAL);
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.GENERAL, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    private void changeCategory(EventCategory category) {
        this.category = category;
        presenter.changeCategory(category);
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

            if (v == dayContainer) {
                if (isTimeSelectorVisible) {
                    hideTimeSelector();
                }

                if (!isDaySelectorVisible) {

                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.DAY_UPDATED_WHILE_CREATE_FROM_DETAILS, userService.getActiveUserId());

                    showDaySelector();
                } else {
                    hideDaySelector();
                }
            } else if (v == timeContainer) {
                if (isDaySelectorVisible) {
                    hideDaySelector();
                }

                if (!isTimeSelectorVisible) {

                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.TIME_UPDATED_WHILE_CREATE_FROM_DETAILS, userService.getActiveUserId());

                    showTimeSelector();
                } else {
                    hideTimeSelector();
                }
            } else if (v == icon) {



                displayCategoryChangeDialog();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            ((MainActivity)getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
