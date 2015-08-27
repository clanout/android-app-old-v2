package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

/**
 * Created by Aditya on 27-08-2015.
 */
public class EventRsvpNotChangedTrigger
{
    private String eventId;
    private Event.RSVP oldRsvp;

    public EventRsvpNotChangedTrigger(String eventId, Event.RSVP oldRsvp)
    {
        this.eventId = eventId;
        this.oldRsvp = oldRsvp;
    }

    public String getEventId()
    {
        return eventId;
    }

    public Event.RSVP getOldRsvp()
    {
        return oldRsvp;
    }
}
