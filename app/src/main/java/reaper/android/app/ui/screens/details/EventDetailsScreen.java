package reaper.android.app.ui.screens.details;

import reaper.android.app.model.Event;

public interface EventDetailsScreen
{
    void navigateToChatScreen(String eventId);

    void navigateToInviteScreen(String eventId);

    void navigateToEditScreen(Event event);

    void navigateToHomeScreen();

    void setTitle(String title);
}
