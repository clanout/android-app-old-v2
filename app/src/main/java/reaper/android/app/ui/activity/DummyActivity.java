package reaper.android.app.ui.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.root.Reaper;
import timber.log.Timber;


public class DummyActivity extends AppCompatActivity
{
    ImageView icon;
    View iconContainer;
    View cardView;
    View bottomBar;
    View dateTimeContainer;
    View calendarContainer;
    TabLayout calendar;
    EditText title;
    TextInputLayout titleContainer;
    TextView day;
    TextView time;

    TextView eventType;

    TabLayout eventTypeSelector;
    View eventTypeContainer;

    boolean isEditMode;
    int calendarState;

    CardClickListener clickListener;

    List<String> days;
    int selectedDayPosition;

    List<DateTime> times;
    private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm a");
    int selectedTimePosition;

    boolean isEventTypeVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Timber.v("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_event);

        icon = (ImageView) findViewById(R.id.iv_create_event_icon);
        iconContainer = findViewById(R.id.ll_create_event_iconContainer);
        cardView = findViewById(R.id.cv_createEvent);
        bottomBar = findViewById(R.id.ll_createEvent_bottomBar);
        title = (EditText) findViewById(R.id.et_createEvent_title);
        dateTimeContainer = findViewById(R.id.ll_createEvent_dateTimeContainer);
        titleContainer = (TextInputLayout) findViewById(R.id.til_createEvent_titleContainer);
        calendar = (TabLayout) findViewById(R.id.tl_createEvent_calendar);
        calendarContainer = findViewById(R.id.ll_createEvent_calendarContainer);
        day = (TextView) findViewById(R.id.tv_createEvent_day);
        time = (TextView) findViewById(R.id.tv_createEvent_time);
        eventTypeSelector = (TabLayout) findViewById(R.id.tl_createEvent_eventType);
        eventTypeContainer = findViewById(R.id.ll_createEvent_eventTypeContainer);
        eventType = (TextView) findViewById(R.id.tv_createEvent_eventType);

        isEditMode = false;
        isEventTypeVisible = false;
        calendarState = 0;
        initDayTime();

        clickListener = new CardClickListener();

        cardView.setOnClickListener(clickListener);
        day.setOnClickListener(clickListener);
        time.setOnClickListener(clickListener);
        eventType.setOnClickListener(clickListener);
        title.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                showEditMode();
                if (hasFocus)
                {
                    titleContainer.setHint("Title");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                else
                {
                    titleContainer.setHint("Want to go out for a beer?");
                }
            }
        });

        initView();
        initIcon();
        initEventType();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void expand(final View v, int duration)
    {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds()
            {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }

    private void collapse(final View v, int duration)
    {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {
                if (interpolatedTime == 1)
                {
                    v.setVisibility(View.GONE);
                }
                else
                {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds()
            {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }

    private void initIcon()
    {
        Drawable iconDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                       .setIcon(MaterialDrawableBuilder.IconValue.MARTINI)
                                                       .setColor(Reaper.getReaperContext()
                                                                       .getResources()
                                                                       .getColor(R.color.white))
                                                       .build();


        icon.setImageDrawable(iconDrawable);
        iconContainer.setBackground(getIconBackground(R.color.abc, 4));
    }

    public void initView()
    {
        bottomBar.setVisibility(View.GONE);
        dateTimeContainer.setVisibility(View.GONE);
        calendarContainer.setVisibility(View.GONE);
        eventTypeContainer.setVisibility(View.GONE);
    }

    private void showEditMode()
    {
        isEditMode = true;
        expand(dateTimeContainer, 200);
        expand(bottomBar, 200);
        title.requestFocus();
    }

    private void hideCalendar()
    {
        calendarState = 0;
        collapse(calendarContainer, 200);
    }

    private void showDayCalendar()
    {
        calendarState = 1;

        calendar.setOnTabSelectedListener(null);
        calendar.setTabMode(TabLayout.MODE_SCROLLABLE);
        calendar.setTabGravity(TabLayout.GRAVITY_FILL);

        calendar.removeAllTabs();
        for (String s : days)
        {
            calendar.addTab(calendar.newTab().setText(s));
        }

        TabLayout.Tab selectedTab = calendar.getTabAt(selectedDayPosition);
        if (selectedTab != null)
        {
            selectedTab.select();
        }

        calendar.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                selectedDayPosition = tab.getPosition();
                if (selectedDayPosition < 0)
                {
                    selectedDayPosition = 0;
                }

                String selectedDay = String.valueOf(tab.getText());
                day.setText(selectedDay);

                hideCalendar();
                if (selectedDay.equalsIgnoreCase("TODAY"))
                {
                    initTimeToday();
                }
                else
                {
                    initTime();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                hideCalendar();
            }
        });

        expand(calendarContainer, 200);
    }

    private void showTimeCalendar()
    {
        calendarState = 2;

        calendar.setOnTabSelectedListener(null);
        calendar.setTabMode(TabLayout.MODE_SCROLLABLE);
        calendar.setTabGravity(TabLayout.GRAVITY_FILL);

        calendar.removeAllTabs();
        for (DateTime time1 : times)
        {
            calendar.addTab(calendar.newTab().setText(time1.toString(timeFormatter)));
        }

        TabLayout.Tab selectedTab = calendar.getTabAt(selectedTimePosition);
        if (selectedTab != null)
        {
            selectedTab.select();
        }

        calendar.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {

                selectedTimePosition = tab.getPosition();
                if (selectedTimePosition < 0)
                {
                    selectedTimePosition = 0;
                }

                time.setText(tab.getText());


                hideCalendar();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                hideCalendar();
            }
        });

        expand(calendarContainer, 200);
    }

    private Drawable getIconBackground(int colorResource, int cornerRadius)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, getResources()
                        .getDisplayMetrics()));
        drawable.setColor(Reaper.getReaperContext().getResources()
                                .getColor(colorResource));
        return drawable;
    }

    private class CardClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if (!isEditMode)
            {
                showEditMode();
            }

            if (v == day)
            {
                switch (calendarState)
                {
                    case 0:
                        showDayCalendar();
                        break;
                    case 1:
                        hideCalendar();
                        break;
                    case 2:
                        hideCalendar();
                        showDayCalendar();
                        break;
                }
            }
            else if (v == time)
            {
                switch (calendarState)
                {
                    case 0:
                        showTimeCalendar();
                        break;
                    case 1:
                        hideCalendar();
                        showTimeCalendar();
                        break;
                    case 2:
                        hideCalendar();
                        break;
                }
            }
            else if (v == eventType)
            {
                if (!isEventTypeVisible)
                {
                    expand(eventTypeContainer, 200);
                }
                else
                {
                    collapse(eventTypeContainer, 200);
                }

                isEventTypeVisible = !isEventTypeVisible;
            }
        }
    }

    private void initDayTime()
    {
        days = new ArrayList<>();
        days.add("TODAY");
        days.add("TOMORROW");
        days.add("TUESDAY");
        days.add("WEDNESDAY");
        days.add("THURSDAY");
        days.add("FRIDAY");
        days.add("SATURDAY");
        selectedDayPosition = 0;
        day.setText(days.get(selectedDayPosition));

        initTimeToday();
    }

    private void initEventType()
    {
        eventTypeSelector.addTab(eventTypeSelector.newTab().setText("INVITE ONLY"));
        eventTypeSelector.addTab(eventTypeSelector.newTab().setText("PUBLIC"));

        eventTypeSelector.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                eventType.setText(tab.getText());
                collapse(eventTypeContainer, 200);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                collapse(eventTypeContainer, 200);
            }
        });
    }

    private void initTimeToday()
    {
        times = new ArrayList<>();
        DateTime dateTime = DateTime.now().withTimeAtStartOfDay();
        DateTime now = DateTime.now();
        for (int i = 0; i < 48; i++)
        {
            if (dateTime.isAfter(now))
            {
                times.add(dateTime);
            }

            dateTime = dateTime.plusMinutes(30);
        }

        if (times.size() > 0)
        {
            selectedTimePosition = 0;
            time.setText(times.get(selectedTimePosition).toString(timeFormatter));
        }
    }

    private void initTime()
    {
        times = new ArrayList<>();
        DateTime dateTime = DateTime.now().withTimeAtStartOfDay();
        for (int i = 0; i < 48; i++)
        {
            times.add(dateTime);
            dateTime = dateTime.plusMinutes(30);
        }

        selectedTimePosition = 0;
        time.setText(times.get(selectedTimePosition).toString(timeFormatter));
    }
}
