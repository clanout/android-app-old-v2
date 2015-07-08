package reaper.android.app.service;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.google.GoogleApi;
import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;

public class GoogleService
{
    private Bus bus;
    private GoogleApi googleApi;
    private static final String API_KEY = "AIzaSyDBX362r-1isovteBR3tGN3QQtDcQn-jyg";

    public GoogleService(Bus bus){
        this.bus = bus;
        googleApi = ApiManager.getInstance().getExternalApi(GoogleApi.class, "https://maps.googleapis.com/maps/api/place/");
    }

    public ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> autocomplete(Double latitude, Double longitude, String type, String input){
        GooglePlaceAutocompleteApiResponse response = googleApi.getPlacesAutocomplete(API_KEY, latitude + "," + longitude, "5000", type, input);
        return (ArrayList<GooglePlaceAutocompleteApiResponse.Prediction>) response.getPredictions();
    }
}
