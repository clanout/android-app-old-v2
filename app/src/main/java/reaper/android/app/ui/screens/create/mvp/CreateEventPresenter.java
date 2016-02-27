package reaper.android.app.ui.screens.create.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.LocationSuggestion;

public interface CreateEventPresenter
{
    void attachView(CreateEventView view);

    void detachView();

    void changeCategory(EventCategory category);

    void autocomplete(String input);

    void selectSuggestion(LocationSuggestion locationSuggestion);

    void setLocationName(String locationName);

    void create(String title, Event.Type type, String description, DateTime startTime, DateTime endTime);
}
