package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by harsh on 19/10/15.
 */
public class UpdateStatusApiRequest extends ApiRequest {

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("status")
    private String status;

    @SerializedName("notification")
    private boolean shouldNotifyFriends;

    public UpdateStatusApiRequest(String eventId, String status, boolean shouldNotifyFriends) {
        this.eventId = eventId;
        this.status = status;
        this.shouldNotifyFriends = shouldNotifyFriends;
    }
}
