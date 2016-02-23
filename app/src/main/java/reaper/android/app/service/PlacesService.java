package reaper.android.app.service;

import java.util.ArrayList;

import reaper.android.app.api._core.ApiManager;
import reaper.android.app.api.google_places.GooglePlacesApi;
import reaper.android.app.api.google_places.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google_places.response.GooglePlaceDetailsApiResponse;
import reaper.android.app.model.Location;
import reaper.android.app.service._new.LocationService_;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PlacesService
{
    private GooglePlacesApi googlePlacesApi;

    public PlacesService()
    {
        googlePlacesApi = ApiManager.getGooglePlacesApi();
    }

    public ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> autocomplete(Double latitude, Double longitude, String input)
    {
        GooglePlaceAutocompleteApiResponse response = googlePlacesApi
                .getPlacesAutocomplete(latitude + "," + longitude, input);
        return (ArrayList<GooglePlaceAutocompleteApiResponse.Prediction>) response.getPredictions();
    }

    public Observable<Location> _getPlaceDetails(String placeid)
    {
        return googlePlacesApi
                .getPlaceDetails(placeid)
                .map(new Func1<GooglePlaceDetailsApiResponse, Location>()
                {
                    @Override
                    public Location call(GooglePlaceDetailsApiResponse googlePlaceDetailsApiResponse)
                    {
                        Location placeLocation = new Location();
                        placeLocation
                                .setLatitude(googlePlaceDetailsApiResponse.getLatitude());
                        placeLocation
                                .setLongitude(googlePlaceDetailsApiResponse.getLongitude());
                        placeLocation.setName(googlePlaceDetailsApiResponse.getName());
                        placeLocation.setZone(LocationService_.getInstance().getCurrentLocation()
                                                              .getZone());
                        return placeLocation;
                    }
                })
                .observeOn(Schedulers.newThread());
    }
}
