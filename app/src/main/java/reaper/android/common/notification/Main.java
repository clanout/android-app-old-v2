package reaper.android.common.notification;

import org.joda.time.DateTime;
import org.joda.time.Instant;

public class Main
{
    public static void main(String[] args)
    {
        DateTime dateTime = DateTime.now();
        DateTime dateTime1 = dateTime.plusSeconds(2);
        System.out.println(dateTime);
//        System.out.println(dateTime1);

        Instant instant = dateTime.toInstant();
        Instant instant1 = dateTime1.toInstant();
//        System.out.println(instant);
//        System.out.println(instant1);

        long timestamp = instant.getMillis();
        long timestamp1 = instant1.getMillis();
//        System.out.println(timestamp);
//        System.out.println(timestamp1);

        Instant instant2 = new Instant(timestamp);
//        System.out.println(instant2);

        DateTime dateTime2 = new DateTime(timestamp1);
        System.out.println(dateTime2);
    }
}
