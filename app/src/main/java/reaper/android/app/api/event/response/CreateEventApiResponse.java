package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Event;

/**
 * Created by aditya on 04/07/15.
 */
public class CreateEventApiResponse extends ApiResponse
{
    @SerializedName("event")
    private Event event;

    public CreateEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
