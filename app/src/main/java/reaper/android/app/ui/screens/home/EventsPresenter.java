package reaper.android.app.ui.screens.home;

import reaper.android.app.model.Event;

public interface EventsPresenter
{
    void attachView(EventsView view);

    void detachView();

    void refreshEvents();

    void selectEvent(Event event);

    void updateRsvp(EventsView.EventListItem eventListItem, Event event, Event.RSVP rsvp);
}
