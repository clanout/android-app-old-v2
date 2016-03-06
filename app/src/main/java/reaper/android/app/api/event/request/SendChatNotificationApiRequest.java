package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by harsh on 24/09/15.
 */
public class SendChatNotificationApiRequest extends ApiRequest
{

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("event_name")
    private String eventName;

    @SerializedName("last_sent_timestamp")
    private DateTime lastSentTimestamp;

    public SendChatNotificationApiRequest(String eventId, String eventName, DateTime lastSentTimestamp)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.lastSentTimestamp = lastSentTimestamp;
    }
}
