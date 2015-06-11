package reaper.android.app.config;

public class CacheKeys
{
    public static final String SESSION_COOKIE = "session_cookie";

    public static final String EVENTS = "events";
    public static final String EVENTS_TIMESTAMP = "events_timestamp";
    public static final String EVENTS_UPDATES = "events_updates";

    public static final String EVENT_DETAILS_PREFIX = "event_details_";
    public static String eventDetails(String eventId)
    {
        return EVENT_DETAILS_PREFIX + eventId;
    }

    public static final String LOCATION_LATITUDE = "location_latitude";
    public static final String LOCATION_LONGITUDE = "location_longitude";
    public static final String LOCATION_ZONE = "location_zone";
}
