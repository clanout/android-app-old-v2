package reaper.android.app.model;

import org.joda.time.DateTime;

import reaper.android.app.config.AppConstants;

public class ChatMessage implements Model
{
    private String id;
    private String message;
    private String senderName;
    private String senderId;
    private DateTime timestamp;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public void setSenderName(String senderName)
    {
        this.senderName = senderName;
    }

    public DateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public boolean isAdmin()
    {
        return senderId.equalsIgnoreCase(AppConstants.CHAT_ADMIN_ID);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ChatMessage))
        {
            return false;
        }

        ChatMessage that = (ChatMessage) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
