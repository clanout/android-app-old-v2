package reaper.android.app.api.google.response;

import com.google.gson.annotations.SerializedName;
import reaper.android.app.api.core.ApiResponse;

import java.util.List;

public class GooglePlaceAutocompleteApiResponse extends ApiResponse
{
    @SerializedName("status")
    private String status;

    @SerializedName("predictions")
    private List<Prediction> predictions;

    public String getStatus()
    {
        return status;
    }

    public List<Prediction> getPredictions()
    {
        return predictions;
    }

    public static class Prediction
    {
        @SerializedName("place_id")
        private String placeId;

        @SerializedName("description")
        private String description;

        public String getPlaceId()
        {
            return placeId;
        }

        public void setPlaceId(String placeId)
        {
            this.placeId = placeId;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }
    }
}
