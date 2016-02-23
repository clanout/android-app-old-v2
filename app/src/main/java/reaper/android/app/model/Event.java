package reaper.android.app.model;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.List;

import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.model._core.Model;

public class Event implements Model
{
    public static enum Type implements Model
    {
        PUBLIC,
        INVITE_ONLY;
    }

    public static enum RSVP implements Model
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
    private String chatId;
    private DateTime lastUpdated;

    private RSVP rsvp;
    private int friendCount;
    private int inviterCount;
    private List<String> friends;

    private boolean isUpdated;
    private boolean isChatUpdated;

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

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
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
        if(isFinalized == null)
        {
            return false;
        }
        return isFinalized;
    }

    public DateTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime(DateTime startTime)
    {
        this.startTime = startTime;
    }

    public DateTime getEndTime()
    {
        return endTime;
    }

    public void setEndTime(DateTime endTime)
    {
        this.endTime = endTime;
    }

    public String getOrganizerId()
    {
        return organizerId;
    }

    public void setOrganizerId(String organizerId)
    {
        this.organizerId = organizerId;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public String getChatId()
    {
        return chatId;
    }

    public void setChatId(String chatId)
    {
        this.chatId = chatId;
    }

    public DateTime getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(DateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
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

    public boolean isUpdated()
    {
        return isUpdated;
    }

    public void setIsUpdated(boolean isUpdated)
    {
        this.isUpdated = isUpdated;
    }

    public boolean isChatUpdated()
    {
        return isChatUpdated;
    }

    public void setIsChatUpdated(boolean isChatUpdated)
    {
        this.isChatUpdated = isChatUpdated;
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
        } else
        {
            Event other = (Event) o;
            if (id.equals(other.id))
            {
                return true;
            } else
            {
                return false;
            }
        }
    }

    @Override
    public String toString()
    {
        return title;
    }

    public boolean isEqualTo(Event event)
    {
        Gson gson = GsonProvider.getGson();
        if (gson.toJson(event).equals(gson.toJson(this)))
        {
            return true;
        } else
        {
            return false;
        }
    }
}
