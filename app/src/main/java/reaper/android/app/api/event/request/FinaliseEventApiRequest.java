package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by Aditya on 08-09-2015.
 */
public class FinaliseEventApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("is_finalized")
    private boolean isFinalised;

    public FinaliseEventApiRequest(String eventId, boolean isFinalised)
    {
        this.eventId = eventId;
        this.isFinalised = isFinalised;
    }
}
