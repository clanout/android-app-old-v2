package reaper.android.app.ui.screens.notifications;

import java.util.List;

import reaper.android.app.model.Event;

public interface NotificationScreen
{
    void navigateToHomeScreen();

    void navigateToDetailsScreen(List<Event> events, String eventId);

    void navigateToChatScreen(List<Event> events, String eventId);
}
