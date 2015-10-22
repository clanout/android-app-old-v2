package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

/**
 * Created by harsh on 19/10/15.
 */
public class UpdateStatusApiRequest extends ApiRequest {

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("status")
    private String status;

    public UpdateStatusApiRequest(String eventId, String status) {
        this.eventId = eventId;
        this.status = status;
    }
}
