package reaper.android.app.trigger.event;

import reaper.android.app.model.EventDetails;

public class EventDetailsFetchTrigger
{
    private EventDetails eventDetails;

    public EventDetailsFetchTrigger(EventDetails eventDetails)
    {
        this.eventDetails = eventDetails;
    }

    public EventDetails getEventDetails()
    {
        return eventDetails;
    }
}
