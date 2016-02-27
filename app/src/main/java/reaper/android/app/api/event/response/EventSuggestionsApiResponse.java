package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.LocationSuggestion;

/**
 * Created by aditya on 04/07/15.
 */
public class EventSuggestionsApiResponse extends ApiResponse
{
    @SerializedName("recommendations")
    private List<LocationSuggestion> recommendations;

    public List<LocationSuggestion> getEventSuggestions(){
        return recommendations;
    }
}
