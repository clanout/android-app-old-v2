package reaper.android.app.ui.screens.details.redesign;

public interface EventDetailsPresenter
{
    void attachView(EventDetailsView view);

    void detachView();

    void toggleRsvp();

    void onStatusClicked();

    void invite();

    void onEdit();

    void chat();

    void requestEditActionState();
}
