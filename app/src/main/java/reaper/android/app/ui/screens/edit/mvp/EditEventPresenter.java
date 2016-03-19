package reaper.android.app.ui.screens.edit.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.Location;

public interface EditEventPresenter
{
    void attachView(EditEventView view);

    void detachView();

    void edit(DateTime starTime, Location location, String description);
}
