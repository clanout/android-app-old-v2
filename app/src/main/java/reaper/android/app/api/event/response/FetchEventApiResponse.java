package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Event;

public class FetchEventApiResponse extends ApiResponse
{
    @SerializedName("event")
    private Event event;

    public FetchEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
