package reaper.android.app.ui.screens.details.mvp;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.User;

public interface EventDetailsView
{
    void init(User sessionUser, Event event, boolean isLastMinute);

    void displayAttendees(List<EventDetails.Attendee> attendees);

    void resetEvent(Event event);

    void showLoading();

    void hideLoading();

    void setEditVisibility(boolean isVisible);

    void setDeleteVisibility(boolean isVisible);

    void displayYayActions();

    void displayNayActions(boolean isInvited);

    void navigateToInvite(String eventId);

    void navigateToChat(String eventId);

    void navigateToEdit(Event event);

    void navigateToHome();
}
