package reaper.android.app.service;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api._core.ApiManager;
import reaper.android.app.api.google_places.GooglePlacesApi;
import reaper.android.app.api.google_places.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google_places.response.GooglePlaceDetailsApiResponse;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service._new.LocationService_;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PlacesService
{
    private static PlacesService instance;

    public static PlacesService getInstance()
    {
        if (instance == null)
        {
            instance = new PlacesService();
        }

        return instance;
    }

    private GooglePlacesApi googlePlacesApi;

    private PlacesService()
    {
        googlePlacesApi = ApiManager.getGooglePlacesApi();
    }

    public ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> autocomplete(Double latitude, Double longitude, String input)
    {
        GooglePlaceAutocompleteApiResponse response = googlePlacesApi
                .getPlacesAutocomplete(latitude + "," + longitude, input);
        return (ArrayList<GooglePlaceAutocompleteApiResponse.Prediction>) response.getPredictions();
    }

    public Observable<List<LocationSuggestion>> _autocomplete(final Double latitude, final Double longitude, final String input)
    {
        return Observable
                .create(new Observable.OnSubscribe<GooglePlaceAutocompleteApiResponse>()
                {
                    @Override
                    public void call(Subscriber<? super GooglePlaceAutocompleteApiResponse> subscriber)
                    {
                        subscriber.onNext(googlePlacesApi
                                .getPlacesAutocomplete(latitude + "," + longitude, input));
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<GooglePlaceAutocompleteApiResponse, List<LocationSuggestion>>()
                {
                    @Override
                    public List<LocationSuggestion> call(GooglePlaceAutocompleteApiResponse response)
                    {
                        List<LocationSuggestion> locationSuggestions = new ArrayList<LocationSuggestion>();
                        int count = 0;
                        for (GooglePlaceAutocompleteApiResponse.Prediction prediction : response
                                .getPredictions())
                        {
                            if (count == 5)
                            {
                                break;
                            }

                            LocationSuggestion locationSuggestion = new LocationSuggestion();
                            locationSuggestion.setId(prediction.getPlaceId());
                            locationSuggestion.setName(prediction.getDescription());
                            locationSuggestions.add(locationSuggestion);
                            count++;
                        }

                        return locationSuggestions;
                    }
                })
                .onErrorReturn(new Func1<Throwable, List<LocationSuggestion>>()
                {
                    @Override
                    public List<LocationSuggestion> call(Throwable throwable)
                    {
                        return new ArrayList<>();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Location> _getPlaceDetails(String placeId)
    {
        return googlePlacesApi
                .getPlaceDetails(placeId)
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
