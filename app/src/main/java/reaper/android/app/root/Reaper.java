package reaper.android.app.root;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import reaper.android.BuildConfig;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.gcm.GcmregistrationIntentTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.common.alarm.AlarmReceiver;
import reaper.android.common.alarm.DeviceBootReceiver;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.gcm.RegistrationIntentService;
import timber.log.Timber;

public class Reaper extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static Tracker tracker;
    private Bus bus;
    private GoogleApiClient googleApiClient;

    private static Reaper instance;

    // Services
    private LocationService locationService;
    private UserService userService;
    private EventService eventService;

    private GenericCache genericCache;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    // TODO -- Analytics events -- fetch pending invtes + status
    // TODO -- test fetch pending invites

    private int timesApplicationOpened;

    @Override
    public void onCreate()
    {
        super.onCreate();

        init();
        Stetho.initializeWithDefaults(this);
    }

    protected void init()
    {
        instance = this;

        FacebookSdk.sdkInitialize(this);

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
        eventService = new EventService(bus);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        handleTimesApplicationOpened();
    }

    private void handleTimesApplicationOpened() {

        try {
            timesApplicationOpened = Integer.parseInt(genericCache.get(CacheKeys.TIMES_APPLICATION_OPENED));
        }catch (Exception e)
        {
            timesApplicationOpened = 0;
        }

        timesApplicationOpened++;

        genericCache.put(CacheKeys.TIMES_APPLICATION_OPENED, timesApplicationOpened);

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.APP_LAUNCHED, "user - " + userService.getActiveUserId() + " time - " + DateTime.now(DateTimeZone.UTC).toString());
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
        if (trigger.isPolling())
        {
            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger)
    {
        if (trigger.isPolling())
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
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.GOOGLE_API_CLIENT_CONNECTION_SUSPENDED, userService.getActiveUserId());
        Log.d("reap3r", "Google API client suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.GOOGLE_API_CLIENT_CONNECTION_FAILED, userService.getActiveUserId());
        Timber.v("Has resolution : " + connectionResult.hasResolution());
    }

    public static Reaper getReaperContext()
    {
        return instance;
    }

    synchronized public static Tracker getAnalyticsTracker()
    {
        if(tracker == null)
        {
            tracker = GoogleAnalytics.getInstance(instance).newTracker(AppConstants.GOOGLE_ANALYTICS_TRACKING_KEY);
            tracker.enableExceptionReporting(true);

        }
        return tracker;
    }

}
