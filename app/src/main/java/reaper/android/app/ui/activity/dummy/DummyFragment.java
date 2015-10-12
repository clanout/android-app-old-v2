package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Suggestion;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.communicator.Communicator;
import rx.Subscriber;
import rx.Subscription;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class DummyFragment extends BaseFragment
{
//    Toolbar toolbar;
//    ImageView icon;
//    View iconContainer;
//    TabLayout typeSelector;
//
//    TextView day;
//    ImageView dayIcon;
//    View dayContainer;
//
//    TextView time;
//    ImageView timeIcon;
//    View timeContainer;
//
//    View daySelectorContainer;
//    TabLayout daySelector;
//    boolean isDaySelectorVisible;
//
//    View timeSelectorContainer;
//    TabLayout timeSelector;
//    boolean isTimeSelectorVisible;
//
////    View locationContainer;
//    ImageView locationIcon;
//    EditText location;
//
//    ScrollView parent;
//
//    RecyclerView suggestionList;
//    View suggestionContainer;
//
//    /* Data */
//    List<DateTime> days;
//    int selectedDayIndex;
//
//    List<DateTime> times;
//    int selectedTimeIndex;
//
//    CompositeSubscription subscriptions;
//
//    ClickListener clickListener;
//
//    CreateEventPresenter presenter;
//
//    @Override
//    public void displaySuggestions(List<Suggestion> suggestions)
//    {
//        suggestionList.setAdapter(new LocationSuggestionAdapter(suggestions));
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        presenter = new CreateEventPresenterImpl(Communicator.getInstance()
//                                                             .getBus(), EventCategory.EAT_OUT);
//        subscriptions = new CompositeSubscription();
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//    {
//        View view = inflater.inflate(R.layout.fragment_create_details, container, false);
//
//        parent = (ScrollView) view.findViewById(R.id.sv_createEvent);
//
//        toolbar = (Toolbar) view.findViewById(R.id.tb_createEvent);
//        icon = (ImageView) view.findViewById(R.id.iv_createEvent_icon);
//        iconContainer = view.findViewById(R.id.ll_create_event_iconContainer);
//        typeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_eventType);
//
//        day = (TextView) view.findViewById(R.id.tv_createEvent_day);
//        dayIcon = (ImageView) view.findViewById(R.id.iv_dayIcon);
//        dayContainer = view.findViewById(R.id.ll_createEvent_dayContainer);
//
//        time = (TextView) view.findViewById(R.id.tv_createEvent_time);
//        timeIcon = (ImageView) view.findViewById(R.id.iv_timeIcon);
//        timeContainer = view.findViewById(R.id.ll_createEvent_timeContainer);
//
//        daySelector = (TabLayout) view.findViewById(R.id.tl_createEvent_daySelector);
//        daySelectorContainer = view.findViewById(R.id.ll_createEvent_daySelectorContainer);
//        timeSelector = (TabLayout) view.findViewById(R.id.tl_createEvent_timeSelector);
//        timeSelectorContainer = view.findViewById(R.id.ll_createEvent_timeSelectorContainer);
//
//        suggestionList = (RecyclerView) view.findViewById(R.id.rv_createEvent_locationSuggestions);
//        suggestionContainer = view.findViewById(R.id.ll_createEvent_locationContainer);
//
////        locationContainer = view.findViewById(R.id.cv_createEvent_locationContainer);
//        location = (EditText) view.findViewById(R.id.et_createEvent_location);
//        locationIcon = (ImageView) view.findViewById(R.id.iv_createEvent_locationIcon);
//
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState)
//    {
//        super.onActivityCreated(savedInstanceState);
//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        setHasOptionsMenu(true);
//
//        icon.setImageDrawable(DrawableFactory
//                .get(EventCategory.EAT_OUT, Dimensions.CREATE_EVENT_ICON_SIZE));
//        iconContainer.setBackground(DrawableFactory.randomIconBackground());
//
//        typeSelector.addTab(typeSelector.newTab().setText("OPEN"));
//        typeSelector.addTab(typeSelector.newTab().setText("SECRET"));
//        typeSelector.getTabAt(1).select();
//
//        dayIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
//                                                        .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR)
//                                                        .setColor(ContextCompat
//                                                                .getColor(getActivity(), R.color.primary))
//                                                        .setSizeDp(24).build());
//
//        timeIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
//                                                         .setIcon(MaterialDrawableBuilder.IconValue.CLOCK)
//                                                         .setColor(ContextCompat
//                                                                 .getColor(getActivity(), R.color.primary))
//                                                         .setSizeDp(24).build());
//
//        locationIcon.setImageDrawable(MaterialDrawableBuilder.with(getActivity())
//                                                             .setIcon(MaterialDrawableBuilder.IconValue.MAP_MARKER)
//                                                             .setColor(ContextCompat
//                                                                     .getColor(getActivity(), R.color.primary))
//                                                             .setSizeDp(24).build());
//
//        clickListener = new ClickListener();
//        dayContainer.setOnClickListener(clickListener);
//        timeContainer.setOnClickListener(clickListener);
//    }
//
//    @Override
//    public void onResume()
//    {
//        super.onResume();
//
//        presenter.attachView(this);
//
//        initDaySelector();
//        initTimeSelector();
//        initDayTime();
//        initRecyclerView();
//
//        location.setOnFocusChangeListener(new View.OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
//                Timber.v("focus");
//                if (hasFocus)
//                {
//                    parent.scrollTo(0, suggestionContainer.getBottom());
//                }
//            }
//        });
//
//        Subscription subscription = WidgetObservable
//                .text(location)
//                .map(new Func1<OnTextChangeEvent, String>()
//                {
//                    @Override
//                    public String call(OnTextChangeEvent onTextChangeEvent)
//                    {
//                        return onTextChangeEvent.text()
//                                                .toString();
//                    }
//                })
//                .subscribe(new Subscriber<String>()
//                {
//                    @Override
//                    public void onCompleted()
//                    {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e)
//                    {
//
//                    }
//
//                    @Override
//                    public void onNext(String s)
//                    {
//                        parent.scrollTo(0, suggestionContainer.getBottom());
//
//                        if (s.length() == 0)
//                        {
//                            presenter.changeCategory(EventCategory.EAT_OUT);
//                        }
//                        else if (s.length() >= 3)
//                        {
//                            presenter.autocomplete(s);
//                        }
//                    }
//                });
//
//        subscriptions.add(subscription);
//    }
//
//    @Override
//    public void onPause()
//    {
//        super.onPause();
//        location.setOnFocusChangeListener(null);
//        subscriptions.clear();
//        presenter.detachView();
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
//    {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.action_create, menu);
//    }
//
//    private void initRecyclerView()
//    {
//        suggestionList.setLayoutManager(new LinearLayoutManager(getActivity()));
//        suggestionList.setAdapter(new LocationSuggestionAdapter(new ArrayList<Suggestion>()));
//    }
//
//    private void initDayTime()
//    {
//        day.setText(DayTimeUtil.formatDate(days.get(selectedDayIndex)));
//        time.setText(DayTimeUtil.getFormattedTime(times.get(selectedTimeIndex)));
//    }
//
//    private void initDaySelector()
//    {
//        daySelectorContainer.setVisibility(View.GONE);
//
//        daySelector.setTabMode(TabLayout.MODE_SCROLLABLE);
//        daySelector.setTabGravity(TabLayout.GRAVITY_FILL);
//        daySelector.removeAllTabs();
//
//        days = DayTimeUtil.getDayList();
//        selectedDayIndex = 0;
//
//        for (DateTime d : days)
//        {
//            daySelector.addTab(daySelector.newTab().setText(DayTimeUtil.formatDate(d)));
//        }
//
//        daySelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//        {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab)
//            {
//                selectedDayIndex = tab.getPosition();
//                day.setText(tab.getText());
//                hideDaySelector();
//
//                initTimeSelector();
//                time.setText(DayTimeUtil.getFormattedTime(times.get(selectedTimeIndex)));
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab)
//            {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab)
//            {
//                hideDaySelector();
//            }
//        });
//    }
//
//    private void initTimeSelector()
//    {
//        timeSelectorContainer.setVisibility(View.GONE);
//
//        timeSelector.setTabMode(TabLayout.MODE_SCROLLABLE);
//        timeSelector.setTabGravity(TabLayout.GRAVITY_FILL);
//        timeSelector.removeAllTabs();
//
//        times = DayTimeUtil.getTimeList(selectedDayIndex);
//        selectedTimeIndex = 0;
//
//        for (DateTime d : times)
//        {
//            timeSelector.addTab(timeSelector.newTab().setText(DayTimeUtil.getFormattedTime(d)));
//        }
//
//        timeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//        {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab)
//            {
//                selectedTimeIndex = tab.getPosition();
//                time.setText(tab.getText().toString());
//                hideTimeSelector();
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab)
//            {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab)
//            {
//                hideTimeSelector();
//            }
//        });
//    }
//
//    private void showDaySelector()
//    {
//        VisibilityAnimationUtil.expand(daySelectorContainer, 200);
//        isDaySelectorVisible = true;
//    }
//
//    private void hideDaySelector()
//    {
//        VisibilityAnimationUtil.collapse(daySelectorContainer, 200);
//        isDaySelectorVisible = false;
//    }
//
//    private void showTimeSelector()
//    {
//        VisibilityAnimationUtil.expand(timeSelectorContainer, 200);
//        isTimeSelectorVisible = true;
//    }
//
//    private void hideTimeSelector()
//    {
//        VisibilityAnimationUtil.collapse(timeSelectorContainer, 200);
//        isTimeSelectorVisible = false;
//    }
//
//    private class ClickListener implements View.OnClickListener
//    {
//        @Override
//        public void onClick(View v)
//        {
//
//            if (v == dayContainer)
//            {
//                if (isTimeSelectorVisible)
//                {
//                    hideTimeSelector();
//                }
//
//                if (!isDaySelectorVisible)
//                {
//                    showDaySelector();
//                }
//                else
//                {
//                    hideDaySelector();
//                }
//            }
//            else if (v == timeContainer)
//            {
//                if (isDaySelectorVisible)
//                {
//                    hideDaySelector();
//                }
//
//                if (!isTimeSelectorVisible)
//                {
//                    showTimeSelector();
//                }
//                else
//                {
//                    hideTimeSelector();
//                }
//            }
//        }
//    }
//
//    private static class DayTimeUtil
//    {
//        private static final String TODAY = "TODAY";
//        private static final String TOMORROW = "TOMORROW";
//
//        private static DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("EEEE");
//        private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm a");
//
//        private static DateTime today = DateTime.now().withTimeAtStartOfDay();
//        private static DateTime tomorrow = today.plusDays(1);
//
//        public static List<DateTime> getDayList()
//        {
//            List<DateTime> days = new ArrayList<>();
//            DateTime today = DateTime.now();
//
//            DateTime lastSlot = today.withTime(23, 29, 0, 0);
//
//            if (today.isAfter(lastSlot))
//            {
//                today = today.plusDays(1).withTimeAtStartOfDay();
//            }
//
//            if (today.dayOfWeek().get() == 7)
//            {
//                days.add(today);
//                return days;
//            }
//
//            int dayOfWeek = today.dayOfWeek().get();
//
//            while (dayOfWeek <= 7)
//            {
//                days.add(today);
//                today = today.plusDays(1);
//
//                dayOfWeek++;
//            }
//
//            return days;
//        }
//
//        public static List<DateTime> getTimeList(int dayIndex)
//        {
//            boolean isToday = dayIndex == 0;
//            List<DateTime> times = new ArrayList<>();
//
//            DateTime now = DateTime.now();
//            DateTime lastSlot = now.withTime(23, 29, 0, 0);
//            if (now.isAfter(lastSlot))
//            {
//                isToday = false;
//            }
//
//            DateTime dateTime = now.withTimeAtStartOfDay();
//            for (int i = 0; i < 48; i++)
//            {
//                if (isToday)
//                {
//                    if (dateTime.isAfter(now))
//                    {
//                        times.add(dateTime);
//                    }
//                }
//                else
//                {
//                    times.add(dateTime);
//                }
//
//                dateTime = dateTime.plusMinutes(30);
//            }
//
//            return times;
//        }
//
//        public static String formatDate(DateTime dateTime)
//        {
//            DateTime input = dateTime.withTimeAtStartOfDay();
//
//            if (today.equals(input))
//            {
//                return TODAY;
//            }
//            else if (tomorrow.equals(input))
//            {
//                return TOMORROW;
//            }
//            else
//            {
//                return dateTime.toString(dayFormatter).toUpperCase();
//            }
//        }
//
//        public static String getFormattedTime(DateTime dateTime)
//        {
//            return dateTime.toString(timeFormatter).toUpperCase();
//        }
//    }
}
