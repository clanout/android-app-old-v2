package reaper.android.app.ui.screens.details.mvp;


public interface EventDetailsPresenter
{
    void attachView(EventDetailsView view);

    void detachView();

    void toggleRsvp();

    void invite();

    void edit();

    void chat();

    void requestEditActionState();

    void setStatus(String status);

    void sendInvitationResponse(String invitationResponse);
}
