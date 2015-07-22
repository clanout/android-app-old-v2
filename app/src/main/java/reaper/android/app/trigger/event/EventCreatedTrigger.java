package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

/**
 * Created by aditya on 04/07/15.
 */
public class EventCreatedTrigger
{
    private Event event;

    public EventCreatedTrigger(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
