package reaper.android.app.trigger;

import reaper.android.app.model.Event;

public class RsvpChangeTrigger
{
    private Event event;
    private Event.RSVP rsvp;

    public RsvpChangeTrigger(Event event, Event.RSVP rsvp)
    {
        this.event = event;
        this.rsvp = rsvp;
    }

    public Event getEvent()
    {
        return event;
    }

    public Event.RSVP getRsvp()
    {
        return rsvp;
    }
}
