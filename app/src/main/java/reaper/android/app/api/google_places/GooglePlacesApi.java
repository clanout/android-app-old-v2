package reaper.android.app.api.google_places;

import reaper.android.app.api.google_places.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google_places.response.GooglePlaceDetailsApiResponse;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface GooglePlacesApi
{
    @GET("/autocomplete/json?radius=20000")
    GooglePlaceAutocompleteApiResponse getPlacesAutocomplete(@Query("location") String location, @Query("input") String input);

    @GET("/details/json")
    Observable<GooglePlaceDetailsApiResponse> getPlaceDetails(@Query("placeid") String placeId);
}
