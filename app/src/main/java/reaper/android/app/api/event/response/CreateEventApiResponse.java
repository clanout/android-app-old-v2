package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiResponse;

/**
 * Created by aditya on 04/07/15.
 */
public class CreateEventApiResponse extends ApiResponse
{
    @SerializedName("event_id")
    private String eventId;

    public CreateEventApiResponse(String eventId)
    {
        this.eventId = eventId;
    }

    public String getEventId()
    {
        return eventId;
    }
}
