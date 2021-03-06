package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class EventsApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    public EventsApiRequest(String zone)
    {
        this.zone = zone;
    }
}
