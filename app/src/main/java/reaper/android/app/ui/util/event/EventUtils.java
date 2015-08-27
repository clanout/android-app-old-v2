package reaper.android.app.ui.util.event;

import reaper.android.app.model.Event;

public class EventUtils
{
    public static int canEdit(Event event, String activeUser)
    {
        if (isOrganiser(event, activeUser))
        {
            return EventUtilsConstants.CAN_EDIT;
        } else
        {
            if (!isGoing(event))
            {
                return EventUtilsConstants.CANNOT_EDIT_NOT_GOING;
            } else
            {
                if (isEventFinalised(event))
                {
                    return EventUtilsConstants.CANNOT_EDIT_LOCKED;
                } else
                {
                   return EventUtilsConstants.CAN_EDIT;
                }
            }
        }
    }

    public static Boolean canViewChat(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES || event.getRsvp() == Event.RSVP.MAYBE)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static Boolean canInviteFriends(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES || event.getRsvp() == Event.RSVP.MAYBE)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static Boolean canDeleteEvent(Event event, String activeUser)
    {
        if (activeUser.equals(event.getOrganizerId()))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static Boolean canFinaliseEvent(Event event, String activeUser)
    {
        if (activeUser.equals(event.getOrganizerId()))
        {
            return true;
        } else
        {
            return false;
        }
    }

    private static Boolean isOrganiser(Event event, String activeUser)
    {
        if (activeUser.equals(event.getOrganizerId()))
        {
            return true;
        } else
        {
            return false;
        }
    }

    private static Boolean isGoing(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES)
        {
            return true;
        } else
        {
            return false;
        }
    }

    private static Boolean isEventFinalised(Event event)
    {
        if (event.isFinalized())
        {
            return true;
        } else
        {
            return false;
        }
    }
}
