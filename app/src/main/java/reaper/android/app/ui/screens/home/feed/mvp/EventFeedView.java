package reaper.android.app.ui.screens.home.feed.mvp;

import java.util.List;

import reaper.android.app.model.Event;

public interface EventFeedView
{
    void showLoading();

    void showEvents(List<Event> events);

    void showNoEventsMessage();

    void showError();

    void gotoDetailsView(String eventId);
}
