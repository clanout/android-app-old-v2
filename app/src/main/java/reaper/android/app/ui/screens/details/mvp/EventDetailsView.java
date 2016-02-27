package reaper.android.app.ui.screens.details.mvp;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.User;

public interface EventDetailsView
{
    /* Init View */
    void displayEventSummary(Event event);

    void displayUserSummary(User user);

    void displayDescription(String description);

    /* Rsvp */
    void displayRsvp(boolean isGoing);

    void disableRsvp();

    void displayRsvpError();

    /* Status */
    void hideStatus();

    void displayStatus(String status);

    void displayLastMinuteStatus(String status);

    /* Attendees */
    void displayAttendeeList(List<EventDetails.Attendee> attendees);

    void showAttendeeLoading();

    void hideAttendeeLoading();

    /* Edit */
    void setEditActionState(boolean isVisible);

    void displayEventFinalizedMessage();

    /* Navigation */
    void navigateToInviteScreen(String eventId);

    void navigateToChatScreen(String eventId);

    void navigateToEditScreen(Event event, EventDetails eventDetails);
}
