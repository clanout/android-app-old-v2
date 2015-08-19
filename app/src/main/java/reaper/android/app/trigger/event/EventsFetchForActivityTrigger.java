package reaper.android.app.trigger.event;

import java.util.List;

import reaper.android.app.model.Event;

public class EventsFetchForActivityTrigger
{
    private List<Event> events;

    public EventsFetchForActivityTrigger(List<Event> events)
    {
        this.events = events;
    }

    public List<Event> getEvents()
    {
        return events;
    }
}
