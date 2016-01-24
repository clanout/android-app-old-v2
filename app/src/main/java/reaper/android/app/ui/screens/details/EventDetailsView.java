package reaper.android.app.ui.screens.details;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;

public interface EventDetailsView
{
    void displayEventSummary(Event event);

    void displayUserSummary(String userId, String name);

    void displayRsvp(boolean isGoing);

    void disableRsvp();

    void displayRsvpError();

    void displayDescription(String description);

    void displayStatusMessage(int statusType, String status);

    void displayInvitationResponseDialog(String eventId, String userId);

    void displayUpdateStatusDialog(String eventId, String userId, String oldStatus, boolean isLastMinute);

    void displayAttendeeList(List<EventDetails.Attendee> attendees);

    void showAttendeeLoading();

    void hideAttendeeLoading();

    void navigateToInviteScreen(Event event);

    void navigateToChatScreen(String eventId, String eventTitle);

    void setEditActionState(boolean isVisible);

    void displayEventFinalizedMessage();

    void navigateToEditScreen(Event event, EventDetails eventDetails);
}
