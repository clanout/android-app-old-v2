package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by aditya on 21/07/15.
 */
public class DeleteEventApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    public DeleteEventApiRequest(String eventId)
    {
        this.eventId = eventId;
    }
}
