package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

/**
 * Created by harsh on 27/09/15.
 */
public class InviteThroughSMSApiRequest extends ApiRequest {

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("phone_numbers")
    private String phoneNumbers;

    public InviteThroughSMSApiRequest(String eventId, List<String> phoneNumbers) {
        this.eventId = eventId;
        this.phoneNumbers = GsonProvider.getGson().toJson(phoneNumbers);
    }
}
