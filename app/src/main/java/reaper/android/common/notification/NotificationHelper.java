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
        }else if(name.equals(NotificationConstants.EVENT_UPDATED))
        {
            return Notification.EVENT_UPDATED;
        }else if(name.equals(NotificationConstants.BLOCKED))
        {
            return Notification.BLOCKED;
        }else if(name.equals(NotificationConstants.UNBLOCKED))
        {
            return Notification.UNBLOCKED;
        }else if(name.equals(NotificationConstants.FRIEND_RELOCATED))
        {
            return Notification.FRIEND_RELOCATED;
        }else if(name.equals(NotificationConstants.NEW_FRIEND_JOINED))
        {
            return Notification.NEW_FRIEND_ADDED;
        }else if(name.equals(NotificationConstants.CHAT))
        {
            return Notification.CHAT;
        }else if(name.equals(NotificationConstants.STATUS))
        {
            return Notification.STATUS;
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
            case Notification.EVENT_UPDATED:
                return getEventUpdatedMessage(args);
            case Notification.BLOCKED:
                return getBlockedMessage(args);
            case Notification.UNBLOCKED:
                return getUnblockedMessage(args);
            case Notification.FRIEND_RELOCATED:
                return getFriendRelocatedMessage(args);
            case Notification.NEW_FRIEND_ADDED:
                return newFriendJoinedAppMessage(args);
            case Notification.CHAT:
                return newChatMessageReceivedMessage(args);
            case Notification.STATUS:
                return newStatusUpdateReceived(args);
            default:
                return "";
        }
    }

    private static String newStatusUpdateReceived(Map<String, String> args) {

//        return args.get("event_name") + " starting at " + args.get("event_name") + " ----- " + args.get("status");
        return "";
    }

    private static String newChatMessageReceivedMessage(Map<String, String> args) {
        return "New conversation in " + args.get("event_name");
    }

    private static String newFriendJoinedAppMessage(Map<String, String> args)
    {
        return args.get("user_name") + " is now on clanOut";
    }

    private static String getFriendRelocatedMessage(Map<String, String> args)
    {
        return "";
    }

    private static String getUnblockedMessage(Map<String, String> args)
    {
        return "";
    }

    private static String getBlockedMessage(Map<String, String> args)
    {
        return "";
    }

    private static String getEventUpdatedMessage(Map<String, String> args)
    {
        return "New updates in " + args.get("event_name");
    }

    private static String getEventRemovedMessage(Map<String, String> args)
    {
        return args.get("user_name") + " dismissed " + args.get("event_name");
    }

    private static String getRSVPChangeMessage(Map<String, String> args)
    {
        return "New friends joined " + args.get("event_name");
    }

    private static String getEventInvitationMessage(Map<String, String> args)
    {
        return args.get("user_name") + " invited you to " + args.get("event_name");
    }

    public static int getIcon(int type)
    {
        return R.drawable.ic_btn_rsvp_going;
    }

    private static String getEventCreatedMessage(Map<String, String> args)
    {
        return args.get("user_name") + " suggested " + args.get("event_name");
    }

}
