package reaper.android.app.service;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import reaper.android.app.api.core.GoogleApiManager;
import reaper.android.app.api.google.GooglePlacesApi;
import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.api.google.response.GooglePlaceDetailsApiResponse;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Location;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventLocationFetchedTrigger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GoogleService
{
    private Bus bus;
    private GooglePlacesApi googlePlacesApi;

    public GoogleService(Bus bus)
    {
        this.bus = bus;
        googlePlacesApi = GoogleApiManager.getInstance().getPlacesApi();
    }

    public ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> autocomplete(Double latitude, Double longitude, String input)
    {
        GooglePlaceAutocompleteApiResponse response = googlePlacesApi
                .getPlacesAutocomplete(latitude + "," + longitude, input);
        return (ArrayList<GooglePlaceAutocompleteApiResponse.Prediction>) response.getPredictions();
    }

    public void getPlaceDetails(String placeid)
    {
        googlePlacesApi.getPlaceDetails(placeid, new Callback<GooglePlaceDetailsApiResponse>()
        {
            @Override
            public void success(GooglePlaceDetailsApiResponse googlePlaceDetailsApiResponse, Response response)
            {
                if (googlePlaceDetailsApiResponse != null)
                {
                    Location placeLocation = new Location();
                    placeLocation.setLatitude(googlePlaceDetailsApiResponse.getLatitude());
                    placeLocation.setLongitude(googlePlaceDetailsApiResponse.getLongitude());
                    placeLocation.setName(googlePlaceDetailsApiResponse.getName());
                    placeLocation.setZone(new LocationService(bus).getUserLocation().getZone());

                    bus.post(new EventLocationFetchedTrigger(placeLocation));
                }
                else
                {
                    bus.post(new GenericErrorTrigger(ErrorCode.EVENT_LOCATION_FETCH_FAILURE, null));
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.EVENT_LOCATION_FETCH_FAILURE, error));
            }
        });
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
                        placeLocation.setZone(new LocationService(bus).getUserLocation()
                                                                      .getZone());
                        return placeLocation;
                    }
                })
                .observeOn(Schedulers.newThread());
    }
}
