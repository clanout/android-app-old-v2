package reaper.android.app.ui.screens.pending_invites.mvp;

import reaper.android.app.model.Event;

public interface PendingInvitesPresenter
{
    void attachView(PendingInvitesView view);

    void detachView();

    void fetchPendingInvites(String mobileNumber);

    void skip();

    void gotoHome();

    void selectInvite(Event event);
}
