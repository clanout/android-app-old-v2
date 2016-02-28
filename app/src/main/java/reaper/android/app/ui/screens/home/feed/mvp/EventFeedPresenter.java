package reaper.android.app.ui.screens.home.feed.mvp;

import reaper.android.app.model.Event;

public interface EventFeedPresenter
{
    void attachView(EventFeedView view);

    void detachView();

    void refreshEvents();

    void selectEvent(Event event);
}
