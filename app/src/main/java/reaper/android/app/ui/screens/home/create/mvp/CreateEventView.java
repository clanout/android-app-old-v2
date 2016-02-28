package reaper.android.app.ui.screens.home.create.mvp;

import reaper.android.app.model.Event;

public interface CreateEventView
{
    void showLoading();

    void displayEmptyTitleErrorMessage();

    void displayInvalidStartTimeErrorMessage();

    void displayError();

    void navigateToInviteScreen(Event event);
}
