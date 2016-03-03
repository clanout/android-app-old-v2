package reaper.android.app.ui.screens.create.mvp;

import java.util.List;

import reaper.android.app.model.LocationSuggestion;

public interface CreateEventView
{
    void displaySuggestions(List<LocationSuggestion> locationSuggestions);

    void setLocation(String locationName);

    void showLoading();

    void displayEmptyTitleError();

    void displayInvalidTimeError();

    void navigateToInviteScreen(String eventId);

    void displayError();
}