package reaper.android.app.ui.screens.edit.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.LocationSuggestion;

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

    void selectSuggestion(LocationSuggestion locationSuggestion);

    void setLocationName(String locationName);

    void setDescription(String description);
}
