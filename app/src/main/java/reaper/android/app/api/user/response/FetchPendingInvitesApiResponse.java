package reaper.android.app.api.user.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Event;

/**
 * Created by harsh on 25/09/15.
 */
public class FetchPendingInvitesApiResponse extends ApiResponse {

    @SerializedName("events")
    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }
}
