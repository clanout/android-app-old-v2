package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.EventDetails;

public class EventDetailsApiResponse extends ApiResponse
{
    @SerializedName("event_details")
    private EventDetails eventDetails;

    public EventDetails getEventDetails()
    {
        return eventDetails;
    }
}
