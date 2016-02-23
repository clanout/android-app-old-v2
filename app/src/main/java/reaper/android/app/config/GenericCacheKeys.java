package reaper.android.app.config;

public final class GenericCacheKeys
{
    // Session User
    public static final String SESSION_USER = "session_user";

    // User Cache
    public static final String MY_PHONE_NUMBER = "my_phone_number";
    public static final String ACTIVE_FRAGMENT = "active_fragment";

    public static final String GCM_TOKEN = "gcm_token";
    public static final String GCM_TOKEN_SENT_TO_SERVER = "gcm_token_sent_to_server";

    public static final String LAST_UPDATE_TIMESTAMP = "last_update_timestamp";

    public static final String HAS_FETCHED_PENDING_INVITES = "has_fetched_pending_invites";
    public static final String TIMES_APPLICATION_OPENED = "times_application_opened";
    public static final String HAS_GIVEN_FEEDBACK = "has_given_Feedback";
    public static final String HAS_SEEN_INVITE_POPUP = "has_seen_invite_popup";
    public static final String READ_CONTACT_PERMISSION_DENIED = "read_contact_permission_denied";

    // Event Suggestions
    public static final String EVENT_SUGGESTIONS = "event_suggestions";
    public static final String EVENT_SUGGESTIONS_UPDATE_TIMESTAMP = "event_suggestions_update_timestamp";

    private GenericCacheKeys()
    {
    }
}
