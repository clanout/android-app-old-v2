package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

/**
 * Created by harsh on 24/09/15.
 */
public class SendChatNotificationApiRequest extends ApiRequest {

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("event_name")
    private String eventName;

    public SendChatNotificationApiRequest(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }
}
