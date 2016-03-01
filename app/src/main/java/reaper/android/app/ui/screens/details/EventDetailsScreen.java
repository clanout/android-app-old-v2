package reaper.android.app.ui.screens.details;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;

public interface EventDetailsScreen
{
    void navigateBack();

    void navigateToChatScreen(String eventId);

    void navigateToInviteScreen(String eventId);

    void navigateToEditScreen(Event event, EventDetails eventDetails);
}
