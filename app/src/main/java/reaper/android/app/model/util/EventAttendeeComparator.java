package reaper.android.app.model.util;

import java.util.Comparator;

import reaper.android.app.model.EventDetails;

public class EventAttendeeComparator implements Comparator<EventDetails.Attendee>
{
    private String activeUser;

    public EventAttendeeComparator(String activeUser)
    {
        this.activeUser = activeUser;
    }

    @Override
    public int compare(EventDetails.Attendee attendee, EventDetails.Attendee attendee2)
    {
        if (attendee.getId().equals(activeUser))
        {
            return -1;
        }
        else if (attendee2.getId().equals(activeUser))
        {
            return 1;
        }

        if (attendee.isFriend() && !attendee2.isFriend())
        {
            return -1;
        }
        else if (!attendee.isFriend() && attendee2.isFriend())
        {
            return 1;
        }
        else
        {
            return attendee.getName().compareTo(attendee2.getName());
        }
    }
}
