package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.model.Event;

public class EventUpdatesApiResponse
{
    @SerializedName("event_updates")
    private List<Event> events;

    public List<Event> getUpdates()
    {
        return events;
    }
}
