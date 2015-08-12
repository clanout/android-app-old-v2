package reaper.android.app.config;

public class CacheKeys
{
    // User Cache
    public static final String SESSION_COOKIE = "session_cookie";
    public static final String ACTIVE_USER = "active_user";
    public static final String USER_LOCATION_LONGITUDE = "user_location_longitude";
    public static final String USER_LOCATION_LATITUDE = "user_location_latitude";
    public static final String USER_LOCATION_ZONE = "user_location_zone";
    public static final String MY_PHONE_NUMBER = "my_phone_number";

    // Event Cache
    public static final String EVENTS = "events";
    public static final String EVENTS_TIMESTAMP = "events_timestamp";
    public static final String EVENTS_UPDATES = "events_updates";
    public static final String EVENT_DETAILS_PREFIX = "event_details_";
    public static String eventDetails(String eventId)
    {
        return EVENT_DETAILS_PREFIX + eventId;
    }

    public static final String ACTIVE_FRAGMENT = "active_fragment";
}
