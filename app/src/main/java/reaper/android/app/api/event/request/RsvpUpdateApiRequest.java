package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.model.Event;

public class RsvpUpdateApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("rsvp_status")
    private Event.RSVP rsvp;

    public RsvpUpdateApiRequest(String eventId, Event.RSVP rsvp)
    {
        this.eventId = eventId;
        this.rsvp = rsvp;
    }
}
