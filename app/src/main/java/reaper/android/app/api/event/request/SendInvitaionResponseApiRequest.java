package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

/**
 * Created by harsh on 19/10/15.
 */
public class SendInvitaionResponseApiRequest extends ApiRequest {

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("message")
    private String message;

    public SendInvitaionResponseApiRequest(String eventId, String message) {
        this.eventId = eventId;
        this.message = message;
    }
}
