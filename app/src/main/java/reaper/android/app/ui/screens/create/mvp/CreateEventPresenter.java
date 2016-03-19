package reaper.android.app.ui.screens.create.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;

public interface CreateEventPresenter
{
    void attachView(CreateEventView view);

    void detachView();

    void create(String title, Event.Type type, EventCategory category,
                String description, DateTime startTime, Location location);
}
