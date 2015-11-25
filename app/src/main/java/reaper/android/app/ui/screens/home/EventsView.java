package reaper.android.app.ui.screens.home;

import java.util.List;

import reaper.android.app.model.Event;

public interface EventsView
{
    void showLoading();

    void showEvents(List<Event> events);

    void showNoEventsMessage();

    void showError();

    void gotoDetailsView(List<Event> events, int activePosition);
}
