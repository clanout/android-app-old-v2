package reaper.android.app.ui.screens.edit;

import org.joda.time.DateTime;

public interface EditEventPresenter
{
    void attachView(EditEventView view);

    void detachView();

    void finalizeEvent();

    void unfinalizeEvent();

    void updateTime(DateTime newTime);
}
