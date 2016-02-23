package reaper.android.app.ui.util;

import reaper.android.app.model.Event;

public class EventUtils
{
    public static Boolean canDeleteEvent(Event event, String activeUser)
    {
        return activeUser.equals(event.getOrganizerId());
    }

    public static Boolean canFinaliseEvent(Event event, String activeUser)
    {
        return activeUser.equals(event.getOrganizerId());
    }

    public static Boolean isOrganiser(Event event, String activeUser)
    {
        return activeUser.equals(event.getOrganizerId());
    }
}
