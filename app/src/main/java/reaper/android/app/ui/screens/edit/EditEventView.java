package reaper.android.app.ui.screens.edit;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.LocationSuggestion;

public interface EditEventView
{
    void init(Event event, String description);

    void showLoading();

    void displayDeleteOption();

    void displayFinalizationOption();

    void displayUnfinalizationOption();

    void displaySuggestions(List<LocationSuggestion> locationSuggestions);

    void setLocation(String locationName);

    void displayEventLockedError();

    void displayError();

    void navigateToDetailsScreen(List<Event> events, int activePosition);

    void navigateToHomeScreen();
}
