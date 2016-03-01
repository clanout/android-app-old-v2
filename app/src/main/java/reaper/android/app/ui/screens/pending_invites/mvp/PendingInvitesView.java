package reaper.android.app.ui.screens.pending_invites.mvp;

import java.util.List;

import reaper.android.app.model.Event;

public interface PendingInvitesView
{
    void displayInvalidMobileNumberMessage();

    void showLoading();

    void hideLoading();

    void displayNoActivePendingInvitationsMessage();

    void displayActivePendingInvitation(List<Event> events);

    void displayExpiredEventsMessage(int expiredEventsCount);

    void navigateToHome();

    void navigateToDetails(String eventId);
}
