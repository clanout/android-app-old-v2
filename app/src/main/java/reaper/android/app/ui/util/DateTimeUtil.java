package reaper.android.app.ui.util;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateTimeUtil
{
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat
            .forPattern("hh:mm a, dd MMM (EEEE)");

    private static DateTimeFormatter DAY_FORMATTER = DateTimeFormat.forPattern("EEEE");
    private static DateTimeFormatter DAY_DATE_FORMATTER = DateTimeFormat
            .forPattern("EEEE (dd MMM)");
    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd MMM");
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("hh:mm a");

    private static final String TODAY = "Today";
    private static final String TOMORROW = "Tomorrow";

    private DateTime now;
    private Map<String, LocalDate> dayMap;
    private List<String> days;
    private List<String> daysAndDate;

    public DateTimeUtil()
    {
        now = DateTime.now();
        dayMap = new HashMap<>();
        days = new ArrayList<>();
        daysAndDate = new ArrayList<>();

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

                    String todayStr = TODAY + " (" + date.toString(DATE_FORMATTER) + ")";
                    daysAndDate.add(todayStr);
                }
            }
            else if (i == 1)
            {
                dayMap.put(TOMORROW, date);
                days.add(TOMORROW);

                String tomorrowStr = TOMORROW + " (" + date.toString(DATE_FORMATTER) + ")";
                daysAndDate.add(tomorrowStr);
            }
            else
            {
                String name = date.toString(DAY_FORMATTER);
                dayMap.put(name, date);
                days.add(name);

                daysAndDate.add(date.toString(DAY_DATE_FORMATTER));
            }

            date = date.plusDays(1);
            i++;
        }
    }

    public List<String> getDayList()
    {
        return days;
    }

    public List<String> getDayAndDateList()
    {
        return daysAndDate;
    }

    public LocalDate getDate(String name)
    {
        return dayMap.get(name);
    }

    public static String formatTime(LocalTime time)
    {
        return time.toString(TIME_FORMATTER).toUpperCase();
    }

    public static String formatDate(LocalDate date)
    {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate endOfWeek = today.plusDays(7);
        if (today.equals(date))
        {
            return TODAY;
        }
        else if (tomorrow.equals(date))
        {
            return TOMORROW;
        }
        else if (date.isBefore(endOfWeek))
        {
            return date.toString(DAY_FORMATTER);
        }
        else
        {
            return date.toString(DATE_FORMATTER);
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

    public static String getDetailsScreenTitle(DateTime dateTime)
    {
        LocalDate date = dateTime.toLocalDate();
        DateTime now = DateTime.now();

        if (dateTime.isBefore(now))
        {
            return "Started";
        }
        else
        {
            LocalDate today = now.toLocalDate();
            if (today.equals(date))
            {
                int hours = Hours.hoursBetween(now, dateTime).getHours();
                if (hours > 0)
                {
                    return hours + " hours to go";
                }
                else
                {
                    int minutes = Minutes.minutesBetween(now, dateTime).getMinutes();
                    if (minutes > 0)
                    {
                        return minutes + " minutes to go";
                    }
                    else
                    {
                        return "Started";
                    }
                }
            }
            else
            {
                LocalDate tomorrow = today.plusDays(1);
                if (tomorrow.equals(date))
                {
                    return "Tomorrow";
                }
                else
                {
                    int days = Days.daysBetween(today, date).getDays();
                    return days + " days to go";
                }
            }
        }
    }
}
