package reaper.android.app.ui.screens.edit;

import org.joda.time.DateTime;

import reaper.android.app.model.Suggestion;

public interface EditEventPresenter
{
    void attachView(EditEventView view);

    void detachView();

    void finalizeEvent();

    void unfinalizeEvent();

    void delete();

    void edit();

    void updateTime(DateTime newTime);

    void autocomplete(String s);

    void fetchSuggestions();

    void selectSuggestion(Suggestion suggestion);

    void setLocationName(String locationName);

    void setDescription(String description);

    void initiateEventDetailsNavigation();
}
