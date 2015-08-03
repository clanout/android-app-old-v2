package reaper.android.app.root;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import reaper.android.app.config.AppConstants;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class Reaper extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Bus bus;
    private GoogleApiClient googleApiClient;

    // Services
    private LocationService locationService;

    @Override
    public void onCreate()
    {
        super.onCreate();
        init();
    }

    protected void init()
    {
        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);

        Communicator.init(bus);
        Cache.init(this, AppConstants.CACHE_FILE);

        locationService = new LocationService(bus);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        String cache = new Gson().toJson(Cache.getInstance());
    }

    @Subscribe
    public void onCacheCommitTrigger(CacheCommitTrigger trigger)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cache.commit(Reaper.this, AppConstants.CACHE_FILE);
            }
        }).start();
    }

    @Subscribe
    public void onUserLocationRefreshRequestTrigger(UserLocationRefreshRequestTrigger trigger)
    {
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }


    // GOOGLE API CLIENT CALLBACKS
    @Override
    public void onConnected(Bundle bundle)
    {
        locationService.refreshUserLocation(this, googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d("reap3r", "Google API client suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.d("reap3r", "Unable to connect to Google API client");
    }

}
