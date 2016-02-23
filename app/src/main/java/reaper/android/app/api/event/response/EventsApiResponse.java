package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Event;

public class EventsApiResponse extends ApiResponse
{
    @SerializedName("events")
    private List<Event> events;

    public List<Event> getEvents()
    {
        return events;
    }
}
