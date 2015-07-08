package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api.core.ApiResponse;
import reaper.android.app.model.Suggestion;

/**
 * Created by aditya on 04/07/15.
 */
public class EventSuggestionsApiResponse extends ApiResponse
{
    @SerializedName("recommendations")
    private List<Suggestion> recommendations;

    public List<Suggestion> getEventSuggestions(){
        return recommendations;
    }
}
