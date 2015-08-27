package reaper.android.app.trigger.event;

import reaper.android.app.model.EventDetails;

/**
 * Created by Aditya on 27-08-2015.
 */
public class EventDetailsFetchedFromNetworkTrigger
{
    private EventDetails eventDetails;

    public EventDetailsFetchedFromNetworkTrigger(EventDetails eventDetails)
    {
        this.eventDetails = eventDetails;
    }

    public EventDetails getEventDetails()
    {
        return eventDetails;
    }
}
