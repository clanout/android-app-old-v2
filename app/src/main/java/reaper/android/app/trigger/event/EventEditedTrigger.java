package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

/**
 * Created by aditya on 21/07/15.
 */
public class EventEditedTrigger
{
    private Event event;

    public EventEditedTrigger(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
