package reaper.android.app.api.google;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google.response.GooglePlaceDetailsApiResponse;
import retrofit.http.GET;
import retrofit.http.Query;

public interface GooglePlacesApi
{
    @GET("/autocomplete/json")
    public GooglePlaceAutocompleteApiResponse getPlacesAutocomplete(@Query("location") String location, @Query("type") String type, @Query("input") String input);

    @GET("/details/json")
    public GooglePlaceDetailsApiResponse getPlaceDetails(@Query("placeid") String placeId);
}
