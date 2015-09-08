package reaper.android.app.trigger.notifications;

import java.util.List;

import reaper.android.common.notification.Notification;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationsFetchedTrigger
{
    private List<Notification> notifications;

    public NotificationsFetchedTrigger(List<Notification> notifications)
    {
        this.notifications = notifications;
    }

    public List<Notification> getNotifications()
    {
        return notifications;
    }
}
