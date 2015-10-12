package reaper.android.app.api.google;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google.response.GooglePlaceDetailsApiResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface GooglePlacesApi
{
    @GET("/autocomplete/json?radius=20000")
    GooglePlaceAutocompleteApiResponse getPlacesAutocomplete(@Query("location") String location, @Query("input") String input);

    @GET("/details/json")
    void getPlaceDetails(@Query("placeid") String placeId, Callback<GooglePlaceDetailsApiResponse> callback);

    @GET("/details/json")
    Observable<GooglePlaceDetailsApiResponse> getPlaceDetails(@Query("placeid") String placeId);
}
