package reaper.android.app.service;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import reaper.android.app.api.core.GoogleApiManager;
import reaper.android.app.api.google.GooglePlacesApi;
import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;

public class GoogleService
{
    private Bus bus;
    private GooglePlacesApi googlePlacesApi;

    public GoogleService(Bus bus){
        this.bus = bus;
        googlePlacesApi = GoogleApiManager.getInstance().getPlacesApi();
    }

    public ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> autocomplete(Double latitude, Double longitude, String type, String input){
        GooglePlaceAutocompleteApiResponse response = googlePlacesApi.getPlacesAutocomplete(latitude + "," + longitude, type, input);
        return (ArrayList<GooglePlaceAutocompleteApiResponse.Prediction>) response.getPredictions();
    }
}
