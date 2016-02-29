package reaper.android.app.ui.screens.notifications.mvp;

import java.util.List;

import reaper.android.app.model.Notification;

public interface NotificationView
{
    void showLoading();

    void displayNotifications(List<Notification> notifications);

    void displayNoNotificationsMessage();

    void navigateToHomeScreen();

    void navigateToDetailsScreen(String eventId);

    void navigateToChatScreen(String eventId);
}
