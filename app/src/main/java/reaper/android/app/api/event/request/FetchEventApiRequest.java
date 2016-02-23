package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class FetchEventApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    public FetchEventApiRequest(String eventId)
    {
        this.eventId = eventId;
    }
}
