package reaper.android.app.ui.screens.details;

public interface EventDetailsPresenter
{
    void attachView(EventDetailsView view);

    void detachView();

    void toggleRsvp();

    void onStatusClicked();

    void invite();

    void onEdit();

    void chat();

    void setStatus(String status);

    void requestEditActionState();
}
