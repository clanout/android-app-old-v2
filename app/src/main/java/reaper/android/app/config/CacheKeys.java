package reaper.android.app.config;

public class CacheKeys
{
    public static final String EVENTS = "events";
    public static final String EVENTS_TIMESTAMP = "events_timestamp";

    public static final String EVENT_DETAILS_PREFIX = "event_details_";

    public static String eventDetails(String eventId)
    {
        return EVENT_DETAILS_PREFIX + eventId;
    }
}
