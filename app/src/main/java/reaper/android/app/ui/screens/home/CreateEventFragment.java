package reaper.android.app.ui.screens.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class CreateEventFragment extends BaseFragment implements View.OnTouchListener {
    /* UI Elements */
    CardView cardView;

    EditText title;
    TextInputLayout titleContainer;

    ImageView icon;
    View iconContainer;

    View dayTimeContainer;
    TextView day;
    TextView time;

    View daySelectorContainer;
    TabLayout daySelector;
    boolean isDaySelectorVisible;

    View timeSelectorContainer;
    TabLayout timeSelector;
    boolean isTimeSelectorVisible;

    View bottomBar;
    boolean isBottomBarVisible;

    TextView eventType;
    View typeSelectorContainer;
    TabLayout typeSelector;
    boolean isTypeSelectorVisible;

    TextView moreDetails;
    TextView create;

    ClickListener clickListener;
    private Bus bus;

    /* Data */
    CreateEventModel createEventModel;
    boolean isEditMode;

    List<DateTime> days;
    int selectedDayIndex;

    List<DateTime> times;
    int selectedTimeIndex;

    Event.Type selectedType;


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
        dayTimeContainer = view.findViewById(R.id.ll_createEvent_dayTimeContainer);
        day = (TextView) view.findViewById(R.id.tv_createEvent_day);
        time = (TextView) view.findViewById(R.id.tv_createEvent_time);
        dayTimeContainer = view.findViewById(R.id.ll_createEvent_dayTimeContainer);
        daySelector = (TabLayout) view.findViewById(R.id.tl_createEvent_daySelector);
        daySelectorContainer = view.findViewById(R.id.ll_createEvent_daySelectorContainer);
        timeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_timeSelector);
        timeSelectorContainer = view.findViewById(R.id.ll_createEvent_timeSelectorContainer);
        bottomBar = view.findViewById(R.id.ll_createEvent_bottomBar);
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

        createEventModel = (CreateEventModel) getArguments().getSerializable(BundleKeys.EVENT_SUGGESTION);

        clickListener = new ClickListener();
        bus = Communicator.getInstance().getBus();

        title.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    enableEditMode();
                }
                else
                {
                    title.clearFocus();
                    titleContainer.setHint(createEventModel.getTitle());
                }
            }
        });

        // Detect Edit Mode if any UI element is clicked
        cardView.setOnClickListener(clickListener);
        title.setOnTouchListener(this);
        day.setOnClickListener(clickListener);
        time.setOnClickListener(clickListener);
        eventType.setOnClickListener(clickListener);
        moreDetails.setOnClickListener(clickListener);
        create.setOnClickListener(clickListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        initView();
        initTypeSelector();
        initDaySelector();
        initTimeSelector();
        initDayTime();
    }

    private void onMoreDetailsClicked()
    {
        Timber.v("More Details");
        // TODO : Fragment transaction to more details fragment
    }

    private void onCreateClicked()
    {
        String eventTitle = title.getText().toString();
        if(eventTitle == null || eventTitle.isEmpty())
        {
            eventTitle = createEventModel.getTitle();
        }

        DateTime startTime = days.get(selectedDayIndex)
                                 .withTime(times.get(selectedTimeIndex).toLocalTime());
        DateTime endTime = startTime.plusDays(1).withTimeAtStartOfDay();

        Timber.v("Title = " + eventTitle + "; Start Time = " + startTime + "; End Time = " + endTime);

        // TODO : Create event
        // TODO : Fragment transaction to invite screen
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

            dayTimeContainer.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
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

        dayTimeContainer.setVisibility(View.VISIBLE);

        if (!isBottomBarVisible)
        {
            VisibilityAnimationUtil.expand(bottomBar, 200);
            isBottomBarVisible = true;
        }
    }

    private void initDayTime()
    {
        day.setText(DayTimeUtil.TODAY);
        time.setText(times.get(selectedTimeIndex).toString(DayTimeUtil.timeFormatter)
                          .toUpperCase());
    }

    private void initTypeSelector()
    {
        typeSelectorContainer.setVisibility(View.GONE);

        selectedType = Event.Type.INVITE_ONLY;

        typeSelector.setTabMode(TabLayout.MODE_FIXED);
        typeSelector.setTabGravity(TabLayout.GRAVITY_CENTER);
        typeSelector.removeAllTabs();

        typeSelector.addTab(typeSelector.newTab().setText("INVITE ONLY"));
        typeSelector.addTab(typeSelector.newTab().setText("PUBLIC"));

        typeSelector.getTabAt(0).select();

        typeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                String selected = tab.getText().toString();
                eventType.setText(selected);

                if (selected.equalsIgnoreCase("PUBLIC"))
                {
                    selectedType = Event.Type.PUBLIC;
                }
                else
                {
                    selectedType = Event.Type.INVITE_ONLY;
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
        daySelector.removeAllTabs();

        days = DayTimeUtil.getDayList();
        selectedDayIndex = 0;

        int i = 0;
        for (DateTime d : days)
        {
            if (i == 0)
            {
                daySelector.addTab(daySelector.newTab().setText(DayTimeUtil.TODAY));
            }
            else if (i == 1)
            {
                daySelector.addTab(daySelector.newTab().setText(DayTimeUtil.TOMORROW));
            }
            else
            {
                daySelector
                        .addTab(daySelector.newTab().setText(d.toString(DayTimeUtil.dayFormatter)
                                                              .toUpperCase()));
            }

            i++;
        }

        daySelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                selectedDayIndex = tab.getPosition();
                day.setText(tab.getText());
                hideDaySelector();

                initTimeSelector();
                time.setText(times.get(selectedTimeIndex).toString(DayTimeUtil.timeFormatter)
                                  .toUpperCase());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                hideDaySelector();
            }
        });
    }

    private void initTimeSelector()
    {
        timeSelectorContainer.setVisibility(View.GONE);

        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
        timeSelector.removeAllTabs();

        times = DayTimeUtil.getTimeList(selectedDayIndex);
        selectedTimeIndex = 0;

        for (DateTime d : times)
        {
            timeSelector.addTab(timeSelector.newTab().setText(d.toString(DayTimeUtil.timeFormatter)
                                                               .toUpperCase()));
        }

        timeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                selectedTimeIndex = tab.getPosition();
                time.setText(tab.getText().toString().toUpperCase());
                hideTimeSelector();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
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
    public boolean onTouch(View v, MotionEvent event) {
        bus.post(new ViewPagerClickedTrigger());
        return false;
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
        }
    }

    private static class DayTimeUtil
    {
        public static final String TODAY = "TODAY";
        public static final String TOMORROW = "TOMORROW";

        public static DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("EEEE");
        public static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm a");

        public static String getInitialDay()
        {
            return TODAY;
        }

        public static String getInitialTime()
        {
            return DateTime.now().toString(timeFormatter).toUpperCase();
        }

        public static List<DateTime> getDayList()
        {
            List<DateTime> days = new ArrayList<>();
            DateTime today = DateTime.now();

            if (today.dayOfWeek().get() == 7)
            {
                days.add(today);
                return days;
            }

            int dayOfWeek = today.dayOfWeek().get();

            while (dayOfWeek <= 7)
            {
                days.add(today);
                today = today.plusDays(1);

                dayOfWeek++;
            }

            return days;
        }

        public static List<DateTime> getTimeList(int dayIndex)
        {
            boolean isToday = dayIndex == 0;
            List<DateTime> times = new ArrayList<>();

            DateTime now = DateTime.now();
            DateTime dateTime = DateTime.now().withTimeAtStartOfDay();
            for (int i = 0; i < 48; i++)
            {
                if (isToday)
                {
                    if (dateTime.isAfter(now))
                    {
                        times.add(dateTime);
                    }
                }
                else
                {
                    times.add(dateTime);
                }

                dateTime = dateTime.plusMinutes(30);
            }

            return times;
        }
    }
}
