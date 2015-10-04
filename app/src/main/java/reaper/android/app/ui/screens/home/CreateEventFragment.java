package reaper.android.app.ui.screens.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.communicator.Communicator;
import rx.Subscription;
import timber.log.Timber;

public class CreateEventFragment extends BaseFragment {
    public interface CreateEventCycleHandler {
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
    boolean isEditMode;

    List<DateTime> days;
    int selectedDayIndex;

    List<DateTime> times;
    int selectedTimeIndex;

    Event.Type selectedType;

    boolean isCreateClicked;
    Bus bus;

    GenericCache genericCache;

    /* Static Factory */
    public static CreateEventFragment newInstance(CreateEventModel createEventModel) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODEL, createEventModel);

        CreateEventFragment createEventFragment = new CreateEventFragment();
        createEventFragment.setArguments(args);

        return createEventFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createEventModel = (CreateEventModel) getArguments().getSerializable(ARG_MODEL);

        clickListener = new ClickListener();
        bus = Communicator.getInstance().getBus();
        genericCache = CacheManager.getGenericCache();

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!isEditMode && hasFocus) {
                    title.clearFocus();
                    titleContainer.setHint(createEventModel.getTitle());
                } else if (hasFocus) {
                    enableEditMode();
                }
            }
        });

        title.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);

        initView();
        initTypeSelector();
        initDaySelector();
        initTimeSelector();
        initDayTime();
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void onMoreDetailsClicked() {
        Timber.v("More Details");
        // TODO : Fragment transaction to more details fragment
    }

    private void onCreateClicked() {
        String eventTitle = title.getText().toString();
        if (eventTitle == null || eventTitle.isEmpty()) {
            Snackbar.make(getView(), "Title cannot be empty", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);

        progressDialog = ProgressDialog.show(getActivity(), "Creating your clan", "Please wait ...");

        DateTime startTime = days.get(selectedDayIndex)
                .withTime(times.get(selectedTimeIndex).toLocalTime());
        DateTime endTime = startTime.plusDays(1).withTimeAtStartOfDay();


        EventService eventService = new EventService(bus);
        LocationService locationService = new LocationService(bus);

        String zone = locationService.getUserLocation().getZone();
        Location location = new Location();
        location.setZone(zone);

        isCreateClicked = true;
        eventService.createEvent(eventTitle, selectedType, createEventModel
                .getCategory(), "", location, startTime, endTime);

        // TODO : Create event
        // TODO : Fragment transaction to invite screen
    }

    @Subscribe
    public void onCreateSuccess(EventCreatedTrigger trigger) {
        if (isCreateClicked) {
            if (progressDialog != null) {
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
    public void onCreateFailure(GenericErrorTrigger trigger) {
        if (isCreateClicked && trigger.getErrorCode() == ErrorCode.EVENT_CREATION_FAILURE) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            Snackbar.make(getView(), "Unable to create event", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void initView() {
        icon.setImageDrawable(createEventModel.getIcon());
        iconContainer.setBackground(createEventModel.getIconBackground(getActivity()));

        if (isEditMode) {
            enableEditMode();
        } else {
            title.clearFocus();
            titleContainer.setHint(createEventModel.getTitle());
        }
    }

    private void enableEditMode() {
        isEditMode = true;

        title.requestFocus();
        titleContainer.setHint("Title");
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(title, InputMethodManager.SHOW_IMPLICIT);

        bus.post(new ViewPagerClickedTrigger());
    }

    private void initDayTime() {
        day.setText(DayTimeUtil.getDayName(days.get(selectedDayIndex)));
        time.setText(DayTimeUtil.getTime(times.get(selectedTimeIndex)));
    }

    private void initTypeSelector() {
        typeSelectorContainer.setVisibility(View.GONE);

        selectedType = Event.Type.INVITE_ONLY;

        typeSelector.setTabMode(TabLayout.MODE_FIXED);
        typeSelector.setTabGravity(TabLayout.GRAVITY_CENTER);
        typeSelector.removeAllTabs();

        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_invite_only));
        typeSelector.addTab(typeSelector.newTab().setText(R.string.event_details_type_public));

        typeSelector.getTabAt(0).select();

        typeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selected = tab.getText().toString();
                eventType.setText(selected.toUpperCase());

                if (selected.equalsIgnoreCase("OPEN")) {
                    selectedType = Event.Type.PUBLIC;
                } else {
                    selectedType = Event.Type.INVITE_ONLY;
                }

                VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                isTypeSelectorVisible = false;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                isTypeSelectorVisible = false;
            }
        });
    }

    private void initDaySelector() {
        daySelectorContainer.setVisibility(View.GONE);

        daySelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        daySelector.setTabGravity(TabLayout.GRAVITY_FILL);
        daySelector.removeAllTabs();

        days = DayTimeUtil.getDayList();
        selectedDayIndex = 0;

        for (DateTime d : days) {
            daySelector.addTab(daySelector.newTab().setText(DayTimeUtil.getDayName(d)));
        }

        daySelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedDayIndex = tab.getPosition();
                day.setText(tab.getText());
                hideDaySelector();

                initTimeSelector();
                time.setText(DayTimeUtil.getTime(times.get(selectedTimeIndex)));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                hideDaySelector();
            }
        });
    }

    private void initTimeSelector() {
        timeSelectorContainer.setVisibility(View.GONE);

        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
        timeSelector.removeAllTabs();

        times = DayTimeUtil.getTimeList(selectedDayIndex);
        selectedTimeIndex = 0;

        for (DateTime d : times) {
            timeSelector.addTab(timeSelector.newTab().setText(DayTimeUtil.getTime(d)));
        }

        timeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTimeIndex = tab.getPosition();
                time.setText(tab.getText().toString());
                hideTimeSelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            bus.post(new ViewPagerClickedTrigger());
            enableEditMode();

            if (v == day) {
                if (isTimeSelectorVisible) {
                    hideTimeSelector();
                }

                if (!isDaySelectorVisible) {
                    showDaySelector();
                } else {
                    hideDaySelector();
                }
            } else if (v == time) {
                if (isDaySelectorVisible) {
                    hideDaySelector();
                }

                if (!isTimeSelectorVisible) {
                    showTimeSelector();
                } else {
                    hideTimeSelector();
                }
            } else if (v == eventType) {
                if (genericCache.get(CacheKeys.HAS_SEEN_EVENT_TYPE_POP_UP) == null) {
                    displayEventTypePopUp();
                }

                if (isTypeSelectorVisible) {
                    VisibilityAnimationUtil.collapse(typeSelectorContainer, 200);
                    isTypeSelectorVisible = false;
                } else {
                    VisibilityAnimationUtil.expand(typeSelectorContainer, 200);
                    isTypeSelectorVisible = true;
                }
            } else if (v == moreDetails) {
                onMoreDetailsClicked();
            } else if (v == create) {
                onCreateClicked();
            } else if (v == icon) {
                displayCategoryChangeDialog();
            }
        }
    }

    private void displayCategoryChangeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_change_category, null);
        builder.setView(dialogView);

        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void displayEventTypePopUp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_event_type, null);
        builder.setView(dialogView);

        builder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                genericCache.put(CacheKeys.HAS_SEEN_EVENT_TYPE_POP_UP, true);
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static class DayTimeUtil {
        private static final String TODAY = "TODAY";
        private static final String TOMORROW = "TOMORROW";

        private static DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("EEEE");
        private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm a");

        private static DateTime today = DateTime.now().withTimeAtStartOfDay();
        private static DateTime tomorrow = today.plusDays(1);

        public static List<DateTime> getDayList() {
            List<DateTime> days = new ArrayList<>();
            DateTime today = DateTime.now();

            DateTime lastSlot = today.withTime(23, 29, 0, 0);

            if (today.isAfter(lastSlot)) {
                today = today.plusDays(1).withTimeAtStartOfDay();
            }

            if (today.dayOfWeek().get() == 7) {
                days.add(today);
                return days;
            }

            int dayOfWeek = today.dayOfWeek().get();

            while (dayOfWeek <= 7) {
                days.add(today);
                today = today.plusDays(1);

                dayOfWeek++;
            }

            return days;
        }

        public static List<DateTime> getTimeList(int dayIndex) {
            boolean isToday = dayIndex == 0;
            List<DateTime> times = new ArrayList<>();

            DateTime now = DateTime.now();
            DateTime lastSlot = now.withTime(23, 29, 0, 0);
            if (now.isAfter(lastSlot)) {
                isToday = false;
            }

            DateTime dateTime = now.withTimeAtStartOfDay();
            for (int i = 0; i < 48; i++) {
                if (isToday) {
                    if (dateTime.isAfter(now)) {
                        times.add(dateTime);
                    }
                } else {
                    times.add(dateTime);
                }

                dateTime = dateTime.plusMinutes(30);
            }

            return times;
        }

        public static String getDayName(DateTime dateTime) {
            DateTime input = dateTime.withTimeAtStartOfDay();

            if (today.equals(input)) {
                return TODAY;
            } else if (tomorrow.equals(input)) {
                return TOMORROW;
            } else {
                return dateTime.toString(dayFormatter).toUpperCase();
            }
        }

        public static String getTime(DateTime dateTime) {
            return dateTime.toString(timeFormatter).toUpperCase();
        }
    }
}
