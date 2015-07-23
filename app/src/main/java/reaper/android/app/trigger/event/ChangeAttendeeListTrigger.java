package reaper.android.app.trigger.event;

import reaper.android.app.model.Event;

public class ChangeAttendeeListTrigger
{
    private Event.RSVP rsvp;
    private String eventId;

    public ChangeAttendeeListTrigger(Event.RSVP rsvp, String eventId)
    {

        this.rsvp = rsvp;
        this.eventId = eventId;
    }

    public Event.RSVP getRsvp()
    {
        return rsvp;
    }

    public String getEventId()
    {
        return eventId;
    }
}
