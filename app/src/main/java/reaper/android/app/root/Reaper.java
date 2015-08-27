package reaper.android.app.root;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import reaper.android.BuildConfig;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.gcm.GcmregistrationIntentTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.gcm.RegistrationIntentService;
import timber.log.Timber;

public class Reaper extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Bus bus;
    private GoogleApiClient googleApiClient;

    private static Reaper instance;

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
        instance = this;

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }

        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);

        Communicator.init(bus);

        DatabaseManager.init(this);

        // Testing
        GenericCache genericCache = CacheManager.getGenericCache();
        String key = "hello_message";
        String value = genericCache.get(key);
        if (value == null)
        {
            genericCache.put(key, "Hello, World!");
        } else
        {
            Timber.d("[Cache Valid] " + key + " = " + value);
        }

        locationService = new LocationService(bus);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Subscribe
    public void onUserLocationRefreshRequestTrigger(UserLocationRefreshRequestTrigger trigger)
    {
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }

    @Subscribe
    public void onGcmRegistrationIntentTriggerReceived(GcmregistrationIntentTrigger trigger)
    {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
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

    public static Reaper getReaperContext()
    {
        return instance;
    }

}
