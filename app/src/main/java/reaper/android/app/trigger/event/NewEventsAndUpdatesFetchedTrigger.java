package reaper.android.app.trigger.event;

import java.util.List;

import reaper.android.app.model.Event;

/**
 * Created by Aditya on 23-08-2015.
 */
public class NewEventsAndUpdatesFetchedTrigger
{
    private List<Event> eventList;

    public NewEventsAndUpdatesFetchedTrigger(List<Event> eventList)
    {
        this.eventList = eventList;
    }

    public List<Event> getEventList()
    {
        return eventList;
    }
}
