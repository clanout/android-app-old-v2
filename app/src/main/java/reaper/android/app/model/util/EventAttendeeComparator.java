package reaper.android.app.model.util;

import java.util.Comparator;

import reaper.android.app.model.EventDetails;

public class EventAttendeeComparator implements Comparator<EventDetails.Attendee>
{
    @Override
    public int compare(EventDetails.Attendee first, EventDetails.Attendee second)
    {
        if (first.isInviter() && second.isInviter())
        {
            if (first.isFriend() && !second.isFriend())
            {
                return -1;
            }
            else if (!first.isFriend() && second.isFriend())
            {
                return 1;
            }
            else
            {
                return first.getName().compareTo(second.getName());
            }
        }
        else if (first.isInviter() && !second.isInviter())
        {
            return -1;
        }
        else if (!first.isInviter() && second.isInviter())
        {
            return 1;
        }
        else
        {
            return first.getName().compareTo(second.getName());
        }
    }
}
