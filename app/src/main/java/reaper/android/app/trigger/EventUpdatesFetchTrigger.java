package reaper.android.app.trigger;

import java.util.List;

import reaper.android.app.model.Event;

public class EventUpdatesFetchTrigger
{
    private List<Event> events;

    public EventUpdatesFetchTrigger(List<Event> events)
    {
        this.events = events;
    }

    public List<Event> getEventUpdates()
    {
        return events;
    }
}
