package reaper.android.app.trigger.event;

import java.util.List;

/**
 * Created by Aditya on 23-08-2015.
 */
public class EventIdsFetchedTrigger
{
    private List<String> eventIdList;

    public EventIdsFetchedTrigger(List<String> eventIdList)
    {
        this.eventIdList = eventIdList;
    }

    public List<String> getEventIdList()
    {
        return eventIdList;
    }
}
