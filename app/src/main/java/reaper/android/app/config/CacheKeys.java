package reaper.android.app.config;

public class CacheKeys
{
    // Session User
    public static final String SESSION_ID = "session_id";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_LOCATION = "user_location";


    // User Cache
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

    public static final String GCM_TOKEN = "gcm_token";
}
