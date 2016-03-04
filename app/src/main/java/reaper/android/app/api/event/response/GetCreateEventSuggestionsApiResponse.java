package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.CreateEventSuggestion;

/**
 * Created by harsh on 19/11/15.
 */
public class GetCreateEventSuggestionsApiResponse extends ApiResponse{

    @SerializedName("event_suggestions")
    private List<CreateEventSuggestion> eventSuggestions;

    public GetCreateEventSuggestionsApiResponse(List<CreateEventSuggestion> eventSuggestions) {
        this.eventSuggestions = eventSuggestions;
    }

    public List<CreateEventSuggestion> getEventSuggestions() {
        return eventSuggestions;
    }
}
