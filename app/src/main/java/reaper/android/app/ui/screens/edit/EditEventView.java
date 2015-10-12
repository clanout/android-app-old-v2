package reaper.android.app.ui.screens.edit;

import java.util.List;

import reaper.android.app.model.Event;

public interface EditEventView
{
    void init(Event event, String description);

    void enableDeleteOption();

    void displayFinalizationOption();

    void displayUnfinalizationOption();

    void displayFinalizationError();

    void displayUnfinalizationError();

    void navigateToDetailsScreen(List<Event> events, int activePosition);

    void navigateToHomeScreen();
}
