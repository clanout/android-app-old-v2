package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.model.EventCategory;

/**
 * Created by aditya on 04/07/15.
 */
public class LocationSuggestionsApiRequest extends ApiRequest
{
    @SerializedName("category")
    private EventCategory eventCategory;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    public LocationSuggestionsApiRequest(EventCategory eventCategory, String latitude, String longitude)
    {
        this.eventCategory = eventCategory;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
