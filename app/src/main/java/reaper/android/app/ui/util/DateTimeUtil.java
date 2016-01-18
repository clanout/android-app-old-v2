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

public class DateTimeUtil
{
    public static DateTimeFormatter DAY_FORMATTER = DateTimeFormat.forPattern("EEEE");
    public static DateTimeFormatter PREV_DAY_FORMATTER = DateTimeFormat.forPattern("dd MMM");
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("hh:mm a");

    public static final String TODAY = "Today";
    public static final String TOMORROW = "Tomorrow";

    private DateTime now;
    private Map<String, LocalDate> dayMap;
    private List<String> days;

    public DateTimeUtil()
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
        else if (date.isBefore(today))
        {
            return date.toString(PREV_DAY_FORMATTER);
        }
        else
        {
            return date.toString(DAY_FORMATTER);
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
