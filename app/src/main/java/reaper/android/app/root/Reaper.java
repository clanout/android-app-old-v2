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

import org.joda.time.DateTime;

import reaper.android.BuildConfig;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.Timestamps;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.gcm.GcmregistrationIntentTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
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
    private UserService userService;

    private GenericCache genericCache;

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

        locationService = new LocationService(bus);
        userService = new UserService(bus);
        genericCache = CacheManager.getGenericCache();

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

    @Subscribe
    public void onFacebookFriendsIdFetched(FacebookFriendsIdFetchedTrigger trigger)
    {
        if(trigger.isPolling())
        {
            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger)
    {
        if(trigger.isPolling())
        {
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now());
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

    public static Reaper getReaperContext()
    {
        return instance;
    }

}
