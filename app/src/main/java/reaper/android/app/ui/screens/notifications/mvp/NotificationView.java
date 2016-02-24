package reaper.android.app.ui.screens.notifications.mvp;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.common.notification.Notification;

public interface NotificationView
{
    void showLoading();

    void displayNotifications(List<Notification> notifications);

    void displayNoNotificationsMessage();

    void navigateToHomeScreen();

    void navigateToDetailsScreen(List<Event> events, String eventId);

    void navigateToChatScreen(List<Event> events, String eventId);
}
