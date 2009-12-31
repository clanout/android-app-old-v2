package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

public class RsvpChangeTrigger
{
    private Event updatedEvent;
    private Event.RSVP oldRsvp;

    public RsvpChangeTrigger(Event updatedEvent, Event.RSVP oldRsvp)
    {
        this.updatedEvent = updatedEvent;
        this.oldRsvp = oldRsvp;
    }

    public Event getUpdatedEvent()
    {
        return updatedEvent;
    }

    public Event.RSVP getOldRsvp()
    {
        return oldRsvp;
    }
}
