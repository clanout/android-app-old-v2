package reaper.android.app.trigger;

import java.util.List;

import reaper.android.app.model.Event;

public class EventsFetchTrigger
{
    private List<Event> events;

    public EventsFetchTrigger(List<Event> events)
    {
        this.events = events;
    }

    public List<Event> getEvents()
    {
        return events;
    }
}
