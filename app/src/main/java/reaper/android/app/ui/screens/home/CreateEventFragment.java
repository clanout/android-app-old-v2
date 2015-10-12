package reaper.android.app.ui.screens.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.create.CreateEventDetailsFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.DateTimeUtils;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.communicator.Communicator;
import rx.Subscription;


public class CreateEventFragment extends BaseFragment implements TimePickerDialog.OnTimeSetListener
{
    public interface CreateEventCycleHandler
    {
        void addCycle(Subscription subscription);
    }

    private static final String ARG_MODEL = "arg_model";

    /* UI Elements */
    CardView cardView;

    EditText title;
    TextInputLayout titleContainer;

    ImageView icon;
    View iconContainer;

    TextView day;
    TextView time;

    View daySelectorContainer;
    TabLayout daySelector;
    boolean isDaySelectorVisible;

    View timeSelectorContainer;
    TabLayout timeSelector;
    boolean isTimeSelectorVisible;

    TextView eventType;
    View typeSelectorContainer;
    TabLayout typeSelector;
    boolean isTypeSelectorVisible;

    TextView moreDetails;
    TextView create;

    ProgressDialog progressDialog;

    ClickListener clickListener;

    /* Data */
    CreateEventModel createEventModel;

    DateTimeUtils dateTimeUtils;

    EventCategory eventCategory;
    Event.Type type;
    LocalTime startTime;
    LocalDate startDate;

    boolean isEditMode;
    boolean isCreateClicked;
    Bus bus;

    GenericCache genericCache;

    /* Static Factory */
    public static CreateEventFragment newInstance(CreateEventModel createEventModel)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODEL, createEventModel);

        CreateEventFragment createEventFragment = new CreateEventFragment();
        createEventFragment.setArguments(args);

        return createEventFragment;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
    {
        startTime = new LocalTime(hourOfDay, minute);
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        cardView = (CardView) view.findViewById(R.id.cv_createEvent);
        title = (EditText) view.findViewById(R.id.et_createEvent_title);
        titleContainer = (TextInputLayout) view.findViewById(R.id.til_createEvent_titleContainer);
        icon = (ImageView) view.findViewById(R.id.iv_createEvent_icon);
        iconContainer = view.findViewById(R.id.ll_create_event_iconContainer);
        day = (TextView) view.findViewById(R.id.tv_createEvent_day);
        time = (TextView) view.findViewById(R.id.tv_createEvent_time);
        daySelector = (TabLayout) view.findViewById(R.id.tl_createEvent_daySelector);
        daySelectorContainer = view.findViewById(R.id.ll_createEvent_daySelectorContainer);
        timeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_timeSelector);
        timeSelectorContainer = view.findViewById(R.id.ll_createEvent_timeSelectorContainer);
        eventType = (TextView) view.findViewById(R.id.tv_createEvent_eventType);
        typeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_eventType);
        typeSelectorContainer = view.findViewById(R.id.ll_createEvent_eventTypeContainer);
        moreDetails = (TextView) view.findViewById(R.id.tv_createEvent_moreDetails);
        create = (TextView) view.findViewById(R.id.tv_createEvent_create);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        createEventModel = (CreateEventModel) getArguments().getSerializable(ARG_MODEL);
        if(createEventModel != null)
        {
            eventCategory = createEventModel.getCategory();
        }

        clickListener = new ClickListener();
        bus = Communicator.getInstance().getBus();
        genericCache = CacheManager.getGenericCache();
        dateTimeUtils = new DateTimeUtils();

        title.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!isEditMode && hasFocus)
                {
                    title.clearFocus();
                    titleContainer.setHint(createEventModel.getTitle());
                }
                else if (hasFocus)
                {
                    enableEditMode();
                }
            }
        });

        title.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                enableEditMode();
                return true;
            }
        });

        // Detect Edit Mode if any UI element is clicked
        cardView.setOnClickListener(clickListener);
        day.setOnClickListener(clickListener);
        time.setOnClickListener(clickListener);
        eventType.setOnClickListener(clickListener);
        moreDetails.setOnClickListener(clickListener);
        create.setOnClickListener(clickListener);
        icon.setOnClickListener(clickListener);

        initTypeSelector();
        initDaySelector();
        initTimeSelector();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        bus.register(this);

        initView();
        initDayTime();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);

        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }

    private void onMoreDetailsClicked()
    {
        String eventTitle = title.getText().toString();

        CreateEventDetailsFragment fragment = CreateEventDetailsFragment
                .newInstance(eventTitle, eventCategory, type, startDate, startTime);

        FragmentUtils.changeFragment(getFragmentManager(), fragment);
    }

    private void onCreateClicked()
    {
        isCreateClicked = true;

        String eventTitle = title.getText().toString();
        if (eventTitle.isEmpty())
        {
            Snackbar.make(getView(), "Title cannot be empty", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        title.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);

        DateTime start = DateTimeUtils.getDateTime(startDate, startTime);
        if(start.isBefore(DateTime.now()))
        {
            Snackbar.make(getView(), "Start time cannot be before the current time", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        progressDialog = ProgressDialog
                .show(getActivity(), "Creating your clan", "Please wait ...");

        DateTime end = DateTimeUtils.getEndTime(start);

        EventService eventService = new EventService(bus);
        LocationService locationService = new LocationService(bus);

        String zone = locationService.getUserLocation().getZone();
        Location location = new Location();
        location.setZone(zone);

        eventService
                .createEvent(eventTitle, type, eventCategory, "", location, start, end);
    }

    @Subscribe
    public void onCreateSuccess(EventCreatedTrigger trigger)
    {
        if (isCreateClicked)
        {
            isCreateClicked = false;

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }

            InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, trigger
                    .getEvent());
            bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, true);
            inviteUsersContainerFragment.setArguments(bundle);
            FragmentUtils.changeFragment(getFragmentManager(), inviteUsersContainerFragment);
        }
    }

    @Subscribe
    public void onCreateFailure(GenericErrorTrigger trigger)
    {
        if (isCreateClicked && trigger.getErrorCode() == ErrorCode.EVENT_CREATION_FAILURE)
        {
            isCreateClicked = false;

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }

            Snackbar.make(getView(), "Unable to create event", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void initView()
    {
        icon.setImageDrawable(createEventModel.getIcon());
        iconContainer.setBackground(createEventModel.getIconBackground(getActivity()));

        if (isEditMode)
        {
            enableEditMode();
        }
        else
        {
            title.clearFocus();
            titleContainer.setHint(createEventModel.getTitle());
        }
    }

    private void enableEditMode()
    {
        isEditMode = true;

        title.requestFocus();
        titleContainer.setHint("Title");
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(title, InputMethodManager.SHOW_IMPLICIT);

        bus.post(new ViewPagerClickedTrigger());
    }

    private void initDayTime()
    {
        day.setText(dateTimeUtils.formatDate(startDate));
        time.setText(dateTimeUtils.formatTime(startTime));
    }

    private void initTypeSelector()
    {
        typeSelectorContainer.setVisibility(View.GONE);

        type = Event.Type.INVITE_ONLY;

        typeSelector.setTabMode(TabLayout.MODE_FIXED);
        typeSelector.setTabGravity(TabLayout.GRAVITY_CENTER);
        typeSelector.removeAllTabs();

        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_invite_only));
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_public));

        typeSelector.getTabAt(0).select();

        typeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                String selected = tab.getText().toString();
                eventType.setText(selected.toUpperCase());

                if (selected.equalsIgnoreCase(getResources()
                        .getString(R.string.event_details_type_public)))
                {
                    type = Event.Type.PUBLIC;
                }
                else
                {
                    type = Event.Type.INVITE_ONLY;
                }

                VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                isTypeSelectorVisible = false;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                isTypeSelectorVisible = false;
            }
        });
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

        startDate = dateTimeUtils.getDate(days.get(0));

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

        startTime = LocalTime.now().plusHours(1).withField(DateTimeFieldType.minuteOfHour(), 0);

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
                            .newInstance(CreateEventFragment.this, startTime
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
                            .newInstance(CreateEventFragment.this, startTime
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
            bus.post(new ViewPagerClickedTrigger());
            enableEditMode();

            if (v == day)
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
            else if (v == time)
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
            else if (v == eventType)
            {
                if (genericCache.get(CacheKeys.HAS_SEEN_EVENT_TYPE_POP_UP) == null)
                {
                    displayEventTypePopUp();
                }

                if (isTypeSelectorVisible)
                {
                    VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                    isTypeSelectorVisible = false;
                }
                else
                {
                    VisibilityAnimationUtil.expand(typeSelectorContainer, 200);
                    isTypeSelectorVisible = true;
                }
            }
            else if (v == moreDetails)
            {
                onMoreDetailsClicked();
            }
            else if (v == create)
            {
                onCreateClicked();
            }
            else if (v == icon)
            {
                displayCategoryChangeDialog();
            }
        }
    }

    private void displayCategoryChangeDialog()
    {
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

        cafe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.CAFE;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.CAFE, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });

        movies.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.MOVIES;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.MOVIES, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        eatOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.EAT_OUT;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.EAT_OUT, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        sports.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.SPORTS;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.SPORTS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        outdoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.OUTDOORS;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.OUTDOORS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        indoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.INDOORS;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.INDOORS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        drinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                eventCategory = EventCategory.DRINKS;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.DRINKS, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        shopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.SHOPPING;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.SHOPPING, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();
            }
        });


        general.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.GENERAL;
                icon.setImageDrawable(DrawableFactory
                        .get(EventCategory.GENERAL, Dimensions.CREATE_EVENT_ICON_SIZE));

                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    private void displayEventTypePopUp()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_event_type, null);
        builder.setView(dialogView);

        builder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                genericCache.put(CacheKeys.HAS_SEEN_EVENT_TYPE_POP_UP, true);
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
