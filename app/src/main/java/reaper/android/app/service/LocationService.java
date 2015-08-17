package reaper.android.app.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Locale;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.me.MeApi;
import reaper.android.app.api.me.request.UserZoneUpdatedApiRequest;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Location;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.common.cache.Cache;
import retrofit.client.Response;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class LocationService
{
    private static final String TAG = "LocationService";

    private Bus bus;
    private GenericCache cache;
    private MeApi meApi;

    public LocationService(Bus bus)
    {
        this.bus = bus;
        cache = new GenericCache();
        meApi = ApiManager.getInstance().getApi(MeApi.class);
    }

    public Location getUserLocation()
    {
        Location location = cache.get(CacheKeys.USER_LOCATION, Location.class);
        if (location == null)
        {
            Log.e(TAG, "User location is null");
            throw new IllegalStateException("User location cannot be null");
        }
        return location;
    }

    public void refreshUserLocation(Context context, GoogleApiClient apiClient)
    {
        if (apiClient.isConnected())
        {
            android.location.Location googleApiLocation = null;
            String zone = null;
            try
            {
                googleApiLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = null;
                addresses = gcd.getFromLocation(googleApiLocation.getLatitude(), googleApiLocation
                        .getLongitude(), 1);
                if (addresses == null || addresses.size() == 0)
                {
                    throw new IllegalStateException();
                }

                zone = addresses.get(0).getLocality();
            }
            catch (Exception e)
            {
                Log.d(TAG, "Unable to refresh user location (" + e.getMessage() + ")");
                bus.post(new GenericErrorTrigger(ErrorCode.GOOGLE_API_LOCATION_FETCH_FAILURE, null));
                return;
            }

            Location location = new Location();
            location.setLongitude(googleApiLocation.getLongitude());
            location.setLatitude(googleApiLocation.getLatitude());
            location.setZone(zone);

            Log.d(TAG, "Location updated (Zone = " + zone + "; Co-ordinates = " + location
                    .getLongitude() + "," + location.getLatitude() + ")");

            if(locationExists())
            {
                Location oldLocation = getUserLocation();
                if(!oldLocation.getZone().equalsIgnoreCase(location.getZone()))
                {
                    EventCache eventCache = new EventCache();
                    eventCache.evict();

                    UserCache userCache = new UserCache();
                    userCache.evictFriendsCache();
                    userCache.evictContactsCache();
                }
            }

            updateZone(location.getZone());

            cache.put(CacheKeys.USER_LOCATION, location);
            bus.post(new UserLocationRefreshTrigger(location));
        }
        else
        {
            bus.post(new GenericErrorTrigger(ErrorCode.GOOGLE_API_CLIENT_NOT_CONNECTED, null));
        }

        apiClient.disconnect();
    }

    public boolean locationExists()
    {
        Location location = cache.get(CacheKeys.USER_LOCATION, Location.class);
        return (location != null);
    }

    private void updateZone(String zone)
    {
        UserZoneUpdatedApiRequest request = new UserZoneUpdatedApiRequest(zone);
        meApi.updateUserZone(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.d(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(Response response)
                    {

                    }
                });
    }
}
