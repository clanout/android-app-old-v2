package reaper.android.app.trigger.event;

import reaper.android.app.model.Location;

/**
 * Created by aditya on 20/07/15.
 */
public class EventLocationFetchedTrigger
{
    private Location location;

    public EventLocationFetchedTrigger(Location location)
    {
        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }
}
