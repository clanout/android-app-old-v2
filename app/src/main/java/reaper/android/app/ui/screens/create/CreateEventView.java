package reaper.android.app.ui.screens.create;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;

public interface CreateEventView
{
    void displaySuggestions(List<Suggestion> suggestions);

    void setLocation(String locationName);

    void showLoading();

    void displayEmptyTitleError();

    void displayInvalidTimeError();

    void navigateToInviteScreen(Event event);

    void displayError();
}
