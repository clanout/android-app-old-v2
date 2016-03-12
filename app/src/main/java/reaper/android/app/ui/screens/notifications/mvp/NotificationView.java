package reaper.android.app.ui.screens.notifications.mvp;

import java.util.List;

import reaper.android.app.model.NotificationWrapper;

public interface NotificationView
{
    void showLoading();

    void displayNotifications(List<NotificationWrapper> notifications);

    void displayNoNotificationsMessage();

    void navigateToDetailsScreen(String eventId);

    void navigateToChatScreen(String eventId);

    void navigateToFriendsScreen();
}
