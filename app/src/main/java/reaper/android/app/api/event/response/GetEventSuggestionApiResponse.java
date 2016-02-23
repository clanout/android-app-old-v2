package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.CreateEventModel;

/**
 * Created by harsh on 19/11/15.
 */
public class GetEventSuggestionApiResponse extends ApiResponse{

    @SerializedName("event_suggestions")
    private List<CreateEventModel> eventSuggestions;

    public GetEventSuggestionApiResponse(List<CreateEventModel> eventSuggestions) {
        this.eventSuggestions = eventSuggestions;
    }

    public List<CreateEventModel> getEventSuggestions() {
        return eventSuggestions;
    }
}
