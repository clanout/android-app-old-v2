package reaper.android.app.model;

import org.joda.time.DateTime;

import java.util.List;

public class Event implements Model
{
    public enum Type implements Model
    {
        PUBLIC,
        INVITE_ONLY;
    }

    public enum RSVP implements Model
    {
        YES,
        MAYBE,
        NO
    }

    private String id;
    private String title;
    private Type type;
    private String category;
    private Boolean isFinalized;
    private DateTime startTime;
    private DateTime endTime;
    private String organizerId;
    private Location location;
    private DateTime lastUpdated;
    private String description;
    private String status;

    private RSVP rsvp;
    private int friendCount;
    private int inviterCount;
    private List<String> friends;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public Type getType()
    {
        return type;
    }

    public String getCategory()
    {
        return category;
    }

    public List<String> getFriends()
    {
        return friends;
    }

    public void setFriends(List<String> friends)
    {
        this.friends = friends;
    }

    public Boolean isFinalized()
    {
        if (isFinalized == null)
        {
            return false;
        }
        return isFinalized;
    }

    public DateTime getStartTime()
    {
        return startTime;
    }

    public DateTime getEndTime()
    {
        return endTime;
    }

    public String getOrganizerId()
    {
        return organizerId;
    }

    public Location getLocation()
    {
        return location;
    }

    public DateTime getLastUpdated()
    {
        return lastUpdated;
    }

    public RSVP getRsvp()
    {
        return rsvp;
    }

    public void setRsvp(RSVP rsvp)
    {
        this.rsvp = rsvp;
    }

    public int getFriendCount()
    {
        return friendCount;
    }

    public void setFriendCount(int friendCount)
    {
        this.friendCount = friendCount;
    }

    public int getInviterCount()
    {
        return inviterCount;
    }

    public void setInviterCount(int inviterCount)
    {
        this.inviterCount = inviterCount;
    }

    public void setIsFinalized(Boolean isFinalized)
    {
        this.isFinalized = isFinalized;
    }

    public String getDescription()
    {
        return description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
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

        if (!(o instanceof Event))
        {
            return false;
        }
        else
        {
            Event other = (Event) o;
            return id.equals(other.id);
        }
    }

    @Override
    public String toString()
    {
        return title;
    }

    public boolean isExpired()
    {
        if(DateTime.now().isAfter(endTime))
        {
            return true;
        }else{

            return false;
        }
    }
}
