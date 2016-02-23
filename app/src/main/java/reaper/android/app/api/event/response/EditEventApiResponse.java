package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Event;

/**
 * Created by aditya on 21/07/15.
 */
public class EditEventApiResponse extends ApiResponse
{
    @SerializedName("event")
    private Event event;

    public EditEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
