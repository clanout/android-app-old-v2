package reaper.android.common.notification;

import org.joda.time.DateTime;

import reaper.android.app.api.core.GsonProvider;

public class Notification
{
    public static final int EVENT_CREATED = 0;
    public static final int EVENT_INVITATION = 1;
    public static final int RSVP = 2;
    public static final int EVENT_REMOVED = 3;

    private int id;
    private int type;
    private String title;
    private String eventId;
    private String message;
    private DateTime timestamp;
    private DateTime timestampReceived;
    private boolean isNew;

    private Notification(int id)
    {
        this.id = id;
        timestampReceived = DateTime.now();
    }

    public int getId()
    {
        return id;
    }

    public int getType()
    {
        return type;
    }

    public String getEventId()
    {
        return eventId;
    }

    public String getMessage()
    {
        return message;
    }

    public DateTime getTimestamp()
    {
        return timestamp;
    }

    public DateTime getTimestampReceived()
    {
        return timestampReceived;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isNew()
    {
        return isNew;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Notification))
        {
            return false;
        }

        Notification that = (Notification) o;

        return id == that.id;

    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }

    public static class Builder
    {
        private Notification notification;

        public Builder(int id)
        {
            notification = new Notification(id);
        }

        public Notification build()
        {
            return notification;
        }

        public Builder type(int type)
        {
            notification.type = type;
            return this;
        }

        public Builder eventId(String eventId)
        {
            notification.eventId = eventId;
            return this;
        }

        public Builder timestamp(DateTime timestamp)
        {
            notification.timestamp = timestamp;
            return this;
        }

        public Builder message(String message)
        {
            notification.message = message;
            return this;
        }

        public Builder title(String title)
        {
            notification.title = title;
            return this;
        }

        public Builder isNew(boolean isNew)
        {
            notification.isNew = isNew;
            return this;
        }
    }
}
