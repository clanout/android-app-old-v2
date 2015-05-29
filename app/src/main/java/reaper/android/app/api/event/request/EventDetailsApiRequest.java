package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class EventDetailsApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    public EventDetailsApiRequest(String eventId)
    {
        super("event/details");
        this.eventId = eventId;
    }
}
