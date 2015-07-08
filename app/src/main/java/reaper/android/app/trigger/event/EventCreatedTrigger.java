package reaper.android.app.trigger.event;

/**
 * Created by aditya on 04/07/15.
 */
public class EventCreatedTrigger
{
    private String eventId;

    public EventCreatedTrigger(String eventId)
    {
        this.eventId = eventId;
    }

    public String getEventId()
    {
        return eventId;
    }
}
