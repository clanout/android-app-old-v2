package reaper.android.app.ui.screens.home.create;

import org.joda.time.DateTime;

import reaper.android.app.model.EventCategory;

public interface CreateEventPresenter
{
    void attachView(CreateEventView view);

    void detachView();

    void create(String title, EventCategory category, boolean isSecret, DateTime startTime);
}
