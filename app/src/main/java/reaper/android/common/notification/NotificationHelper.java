package reaper.android.common.notification;

import java.util.Map;

import reaper.android.R;
import reaper.android.app.config.NotificationConstants;

public class NotificationHelper
{
    public static int getType(String name)
    {
        if (name.equals(NotificationConstants.EVENT_CREATED))
        {
            return Notification.EVENT_CREATED;
        }else if(name.equals(NotificationConstants.EVENT_INVITATION))
        {
            return Notification.EVENT_INVITATION;
        }else if(name.equals(NotificationConstants.RSVP))
        {
            return Notification.RSVP;
        }else if(name.equals(NotificationConstants.EVENT_REMOVED))
        {
            return Notification.EVENT_REMOVED;
        }
        else
        {
            throw new IllegalArgumentException("invalid event type [" + name + "]");
        }
    }

    public static String getMessage(int type, Map<String, String> args)
    {
        switch (type)
        {
            case Notification.EVENT_CREATED:
                return getEventCreatedMessage(args);
            case Notification.EVENT_INVITATION:
                return getEventInvitationMessage(args);
            case Notification.RSVP:
                return getRSVPChangeMessage(args);
            case Notification.EVENT_REMOVED:
                return getEventRemovedMessage(args);
            default:
                return "";
        }
    }

    private static String getEventRemovedMessage(Map<String, String> args)
    {
        return args.get("user_name") + " removed the clan ---- " + args.get("event_name");
    }

    private static String getRSVPChangeMessage(Map<String, String> args)
    {
        return "A new clan is growing ----- " + args.get("event_name");
    }

    private static String getEventInvitationMessage(Map<String, String> args)
    {
        return args.get("user_name") + "has invited you to the clan" + args.get("event_name");
    }

    public static int getIcon(int type)
    {
        return R.drawable.ic_btn_rsvp_going;
    }

    private static String getEventCreatedMessage(Map<String, String> args)
    {
        return args.get("user_name") + " is starting a new clan --- " + args.get("event_name");
    }
}
