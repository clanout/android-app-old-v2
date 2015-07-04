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

import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Location;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.common.cache.Cache;

public class LocationService
{
    private Bus bus;

    public LocationService(Bus bus)
    {
        this.bus = bus;
    }

    public Location getUserLocation()
    {
//        Cache cache = Cache.getInstance();
//        Double latitude = (Double) cache.get(CacheKeys.USER_LOCATION_LATITUDE);
//        Double longitude = (Double) cache.get(CacheKeys.USER_LOCATION_LONGITUDE);
//        String zone = (String) cache.get(CacheKeys.USER_LOCATION_ZONE);
//
//        if (latitude == null || longitude == null || zone == null)
//        {
//            throw new IllegalStateException();
//        }
//        else
//        {
//            Location location = new Location();
//            location.setLatitude(latitude);
//            location.setLongitude(longitude);
//            location.setZone(zone);
//            return location;
//        }

        Location location = new Location();
        location.setLatitude(12.9259);
        location.setLongitude(77.6229);
        location.setZone("Bengaluru");
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
                addresses = gcd.getFromLocation(googleApiLocation.getLatitude(), googleApiLocation.getLongitude(), 1);
                if (addresses == null || addresses.size() == 0)
                {
                    throw new IllegalStateException();
                }

                zone = addresses.get(0).getLocality();
            }
            catch (Exception e)
            {
                Log.d("reap3r", "Error... " + e.getMessage());
                bus.post(new GenericErrorTrigger(ErrorCode.GOOGLE_API_LOCATION_FETCH_FAILURE, null));
                return;
            }

            Location location = new Location();
            location.setLongitude(googleApiLocation.getLongitude());
            location.setLatitude(googleApiLocation.getLatitude());
            location.setZone(zone);

            Log.d("reap3r", "Location updated (Zone = " + zone + "; Co-ordinates = " + location.getLongitude() + "," + location.getLatitude() + ")");

            Cache cache = Cache.getInstance();

            String oldZone = (String) cache.get(CacheKeys.USER_LOCATION_ZONE);

            // Invalidate Events cache if the zone changes
            if (!zone.equalsIgnoreCase(oldZone))
            {
                cache.remove(CacheKeys.EVENTS);
            }

            cache.put(CacheKeys.USER_LOCATION_LATITUDE, location.getLatitude());
            cache.put(CacheKeys.USER_LOCATION_LONGITUDE, location.getLongitude());
            cache.put(CacheKeys.USER_LOCATION_ZONE, location.getZone());

            bus.post(new UserLocationRefreshTrigger(location));
            bus.post(new CacheCommitTrigger());
        }
        else
        {
            bus.post(new GenericErrorTrigger(ErrorCode.GOOGLE_API_CLIENT_NOT_CONNECTED, null));
        }

        apiClient.disconnect();
    }

    public boolean locationExists()
    {
        Double latitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LATITUDE);
        Double longitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LONGITUDE);
        String zone = (String) Cache.getInstance().get(CacheKeys.USER_LOCATION_ZONE);

        Log.d("reap3r", "" + latitude + "," + longitude + "," + zone);

        if (latitude == null || longitude == null || zone == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
