package reaper.android.app.ui.util;

import reaper.android.app.model.Event;

public class EventUtils
{
    public static Boolean canEdit(Event event, String activeUser)
    {
        if ((event.getRsvp() == Event.RSVP.YES && !event.isFinalized()) || (activeUser.equals(event.getOrganizerId())))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean canViewChat(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES || event.getRsvp() == Event.RSVP.MAYBE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean canInviteFriends(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES || event.getRsvp() == Event.RSVP.MAYBE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean canDeleteEvent(Event event, String activeUser)
    {
        if (activeUser.equals(event.getOrganizerId()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean canFinaliseEvent(Event event, String activeUser)
    {
        if (activeUser.equals(event.getOrganizerId()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
