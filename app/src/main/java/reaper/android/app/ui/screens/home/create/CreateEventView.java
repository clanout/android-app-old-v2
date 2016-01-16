package reaper.android.app.ui.screens.home.create;

import reaper.android.app.model.Event;

public interface CreateEventView
{
    void displayEmptyTitleErrorMessage();

    void displayInvalidStartTimeErrorMessage();

    void showCreateLoading();

    void displayCreateFailedMessage();

    void navigateToInviteScreen(Event event);
}
