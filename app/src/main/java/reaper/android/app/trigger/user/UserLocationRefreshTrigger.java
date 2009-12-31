package reaper.android.app.trigger.user;

import reaper.android.app.model.Location;

public class UserLocationRefreshTrigger
{
    private Location location;

    public UserLocationRefreshTrigger(Location location)
    {
        this.location = location;
    }

    public Location getUserLocation()
    {
        return location;
    }
}
