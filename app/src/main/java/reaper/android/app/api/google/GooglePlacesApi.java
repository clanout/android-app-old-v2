package reaper.android.app.api.google;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google.response.GooglePlaceDetailsApiResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface GooglePlacesApi
{
    @GET("/autocomplete/json")
    public GooglePlaceAutocompleteApiResponse getPlacesAutocomplete(@Query("location") String location, @Query("input") String input);

    @GET("/details/json")
    public void getPlaceDetails(@Query("placeid") String placeId, Callback<GooglePlaceDetailsApiResponse> callback);
}
