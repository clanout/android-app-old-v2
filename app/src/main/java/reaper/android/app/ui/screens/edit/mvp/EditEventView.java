package reaper.android.app.ui.screens.edit.mvp;

import reaper.android.app.model.Event;

public interface EditEventView
{
    void init(Event event);

    void showLoading();

    void displayError();

    void navigateToDetailsScreen(String eventId);
}
