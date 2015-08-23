package reaper.android.app.config;

public class NotificationConstants
{
    public static final String EVENT_ADDED = "event_added";
    public static final String EVENT_REMOVED = "event_removed";
    public static final String EVENT_UPDATED = "event_updated";
    public static final String FRIEND_RELOCATED = "friend_relocated";
    public static final String EVENT_INVITATION = "event_invitation";

    public static final String EVENT_ADDED_TITLE = "New Event Created";
    public static final String EVENT_UPDATED_TITLE = "Event Updated";
    public static final String FRIEND_RELOCATED_TITLE = "New Friend in Town";
    public static final String INVITE_RECEIVED_TITLE = "Event Invite Received";

    public static final int NOTIFICATION_NOT_RECEIVED_LIMIT = 172800000;
    public static final int FRIEND_RELOCATED_NOTIFICATION_NOT_RECEIVED_LIMIT = 1728000000;

    public static final String NOTIFICATION_RECEIVED_TIMESTAMP = "notification_not_received";
    public static final String FRIEND_RELOCATED_NOTIFICATION_TIMESTAMP = "friend_relocated_notification_not_received";
}
