package reaper.android.app.model;

import java.util.List;

public class EventDetails implements Model
{
    public static class Attendee implements Model
    {
        private String id;
        private String name;
        private String status;
        private Event.RSVP rsvp;
        private boolean isFriend;
        private boolean isInviter;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Event.RSVP getRsvp()
        {
            return rsvp;
        }

        public void setRsvp(Event.RSVP rsvp)
        {
            this.rsvp = rsvp;
        }

        public boolean isFriend()
        {
            return isFriend;
        }

        public void setFriend(boolean isFriend)
        {
            this.isFriend = isFriend;
        }

        public boolean isInviter()
        {
            return isInviter;
        }

        public void setInviter(boolean isInviter)
        {
            this.isInviter = isInviter;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public int hashCode()
        {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }

            if (!(o instanceof Attendee))
            {
                return false;
            }
            else
            {
                Attendee other = (Attendee) o;
                if (id.equals(other.id))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }

    public static class Invitee implements Model
    {
        private String id;
        private String name;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        @Override
        public int hashCode()
        {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }

            if (!(o instanceof Invitee))
            {
                return false;
            }
            else
            {
                Invitee other = (Invitee) o;
                if (id.equals(other.id))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }

    private String id;
    private String description;
    private List<Attendee> attendees;
    private List<Invitee> invitee;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<Attendee> getAttendees()
    {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees)
    {
        this.attendees = attendees;
    }

    public List<Invitee> getInvitee()
    {
        return invitee;
    }

    public void setInvitee(List<Invitee> invitee)
    {
        this.invitee = invitee;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof EventDetails))
        {
            return false;
        }
        else
        {
            EventDetails other = (EventDetails) o;
            if (id.equals(other.id))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
