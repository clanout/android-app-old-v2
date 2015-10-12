package reaper.android.app.ui.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateTimeUtils
{
    public static DateTimeFormatter DAY_FORMATTER = DateTimeFormat.forPattern("EEEE");
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("hh:mm a");

    public static final String TODAY = "TODAY";
    public static final String TOMORROW = "TOMORROW";

    public static final String MORNING = "MORNING";
    public static final String NOON = "NOON";
    public static final String EVENING = "EVENING";
    public static final String NIGHT = "NIGHT";
    public static final String PICK_YOUR_OWN = "PICK ONE";

    private DateTime now;
    private Map<String, LocalTime> timeMap;
    private Map<String, LocalDate> dayMap;
    private List<String> days;

    public DateTimeUtils()
    {
        now = DateTime.now();
    }

    public List<String> getDayList()
    {
        if (dayMap == null || days == null)
        {
            dayMap = new HashMap<>();
            days = new ArrayList<>();

            LocalDate date = now.toLocalDate();
            DateTime lastSlot = now.withTime(23, 29, 0, 0);
            boolean includeToday = true;
            if (now.isAfter(lastSlot))
            {
                includeToday = false;
            }

            int i = 0;
            while (i < 7)
            {
                if (i == 0)
                {
                    if (includeToday)
                    {
                        dayMap.put(TODAY, date);
                        days.add(TODAY);
                    }
                }
                else if (i == 1)
                {
                    dayMap.put(TOMORROW, date);
                    days.add(TOMORROW);
                }
                else
                {
                    String name = date.toString(DAY_FORMATTER);
                    dayMap.put(name, date);
                    days.add(name);
                }

                date = date.plusDays(1);
                i++;
            }
        }

        return days;
    }

    public List<String> getTimeList()
    {
        if (timeMap == null)
        {
            timeMap = new HashMap<>();
            LocalTime morning = new LocalTime(10, 0);
            LocalTime noon = new LocalTime(12, 0);
            LocalTime evening = new LocalTime(17, 0);
            LocalTime night = new LocalTime(20, 0);

            timeMap.put(MORNING, morning);
            timeMap.put(NOON, noon);
            timeMap.put(EVENING, evening);
            timeMap.put(NIGHT, night);
        }

        List<String> keys = new ArrayList<>();
        keys.add(PICK_YOUR_OWN);
        keys.add(MORNING);
        keys.add(NOON);
        keys.add(EVENING);
        keys.add(NIGHT);

        return keys;
    }

    public LocalTime getTime(String name)
    {
        return timeMap.get(name);
    }

    public LocalDate getDate(String name)
    {
        return dayMap.get(name);
    }

    public String formatTime(LocalTime time)
    {
        return time.toString(TIME_FORMATTER).toUpperCase();
    }

    public String formatDate(LocalDate date)
    {
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        if (today.equals(date))
        {
            return TODAY;
        }
        else if (tomorrow.equals(date))
        {
            return TOMORROW;
        }
        else
        {
            return date.toString(DAY_FORMATTER).toUpperCase();
        }
    }

    public static DateTime getDateTime(LocalDate date, LocalTime time)
    {
        return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), time
                .getHourOfDay(), time.getMinuteOfHour());
    }

    public static DateTime getEndTime(DateTime startTime)
    {
        return startTime.plusDays(1).withTimeAtStartOfDay();
    }
}
