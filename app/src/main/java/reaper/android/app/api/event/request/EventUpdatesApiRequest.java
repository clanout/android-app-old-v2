package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import reaper.android.app.api.core.ApiRequest;

public class EventUpdatesApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    @SerializedName("last_updated")
    private DateTime lastUpdated;

    public EventUpdatesApiRequest(String zone, DateTime lastUpdated)
    {
        super("event/updates");
        this.zone = zone;
        this.lastUpdated = lastUpdated;
    }
}
