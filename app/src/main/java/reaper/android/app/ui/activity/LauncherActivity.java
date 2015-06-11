package reaper.android.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.service.AuthService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.GenericErrorTrigger;
import reaper.android.app.trigger.SessionValidatedTrigger;
import reaper.android.app.trigger.UserLocationRefreshTrigger;
import reaper.android.common.cache.AppPreferences;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class LauncherActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Bus bus;
    private GoogleApiClient googleApiClient;

    // Services
    private AuthService authService;
    private LocationService locationService;

    // UI Elements
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        bus.register(this);

        authService = new AuthService(bus);
        locationService = new LocationService(bus);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Dummy Session initialization
        if (AppPreferences.get(this, "SESSION_COOKIE") == null)
        {
            AppPreferences.set(this, "SESSION_COOKIE", "dummy_session_cookie");
        }

        String sessionCookie = AppPreferences.get(this, "SESSION_COOKIE");
        Log.d("reap3r", "Session Cookie : " + sessionCookie);
        authService.validateSession(sessionCookie);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }
        bus.unregister(this);
    }

    @Subscribe
    public void onSessionValidatedTrigger(SessionValidatedTrigger trigger)
    {
        Log.d("reap3r", "Session validated");

        Cache.getInstance().put(CacheKeys.SESSION_COOKIE, trigger.getValidatedSessionCookie());

        progressDialog = ProgressDialog.show(this, "Welcome", "Fetching your current location...");

        googleApiClient.connect();
    }

    @Subscribe
    public void onGenericErrorTrigger(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.INVALID_SESSION)
        {
            Log.d("reap3r", "Session invalid. Proceed to authentication");
        }
    }

    @Subscribe
    public void onUserLocationRefreshTrigger(UserLocationRefreshTrigger trigger)
    {
        Log.d("reap3r", "User Location : " + (new Gson().toJson(trigger.getUserLocation())));

        progressDialog.dismiss();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // GOOGLE API CLIENT CALLBACKS
    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d("reap3r", "Google API client connected");

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
