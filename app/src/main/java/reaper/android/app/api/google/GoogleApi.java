package reaper.android.app.api.google;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google.response.GooglePlaceDetailsApiResponse;
import retrofit.http.GET;
import retrofit.http.Query;

public interface GoogleApi
{
        @GET("/autocomplete/json")
        public GooglePlaceAutocompleteApiResponse getPlacesAutocomplete(@Query("key") String apiKey, @Query("location") String location, @Query("radius") String radius, @Query("type") String type, @Query("input") String input);

        @GET("/details/json")
        public GooglePlaceDetailsApiResponse getPlaceDetails(@Query("key") String apiKey, @Query("placeid") String placeId);

}
