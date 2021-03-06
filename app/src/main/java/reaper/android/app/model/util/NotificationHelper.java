package reaper.android.app.model.util;

import android.text.Spannable;
import android.text.SpannableString;

import java.util.List;
import java.util.Map;

import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.config.NotificationConstants;
import reaper.android.app.config.NotificationMessages;
import reaper.android.app.model.Notification;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class NotificationHelper
{
    private static NotificationCache notificationCache = CacheManager.getNotificationCache();

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

        return String.format(NotificationMessages.NEW_STATUS_UPDATED, args.get("user_name"), args.get("event_name"));
    }

    private static String newChatMessageReceivedMessage(Map<String, String> args) {

        return String.format(NotificationMessages.NEW_CHAT_MESSAGE, args.get("event_name"));
    }

    private static String newFriendJoinedAppMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.NEW_FRIEND_JOINED_APP, args.get("user_name"));
    }

    private static String getFriendRelocatedMessage(Map<String, String> args)
    {
        return NotificationMessages.FRIEND_RELOCATED;
    }

    private static String getUnblockedMessage(Map<String, String> args)
    {
        return NotificationMessages.UNBLOCKED;
    }

    private static String getBlockedMessage(Map<String, String> args)
    {
        return NotificationMessages.BLOCKED;

    }

    private static String getEventUpdatedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_UPDATED, args.get("event_name"));
    }

    private static String getEventRemovedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_REMOVED, args.get("user_name"), args.get("event_name"));
    }

    private static String getRSVPChangeMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.RSVP_CHANGED, args.get("event_name"));
    }

    private static String getEventInvitationMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_INVITATION, args.get("user_name"), args.get("event_name"));
    }

    private static String getEventCreatedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_CREATED, args.get("user_name"), args.get("event_name"));
    }

}
