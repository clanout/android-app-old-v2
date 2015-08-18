package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api.core.ApiResponse;
import reaper.android.app.model.Event;

public class EventUpdatesApiResponse extends ApiResponse
{
    @SerializedName("event_updates")
    private List<Event> events;

    public List<Event> getUpdates()
    {
        return events;
    }
}
