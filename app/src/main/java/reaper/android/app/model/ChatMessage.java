package reaper.android.app.model;

import reaper.android.app.model.core.Model;

/**
 * Created by harsh on 21-05-2015.
 */
public class ChatMessage implements Model
{
    private boolean isMe;
    private String message;
    private String senderName;
    private String senderId;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
