package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

public class EventClickTrigger
{
    private Event event;

    public EventClickTrigger(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}