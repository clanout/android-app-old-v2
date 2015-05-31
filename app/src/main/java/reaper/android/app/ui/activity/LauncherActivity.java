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

import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.UserLocationRefreshTrigger;
import reaper.android.common.communicator.Communicator;

public class LauncherActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Bus bus;
    private GoogleApiClient googleApiClient;

    // Services
    private LocationService locationService;

    // UI Elements
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        bus.register(this);

        locationService = new LocationService(bus);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        progressDialog = ProgressDialog.show(this, "Welcome", "Fetching your current location...");

        googleApiClient.connect();
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
    public void onUserLocationRefreshTrigger(UserLocationRefreshTrigger trigger)
    {
        Log.d("reap3r", "User Location : " + (new Gson().toJson(trigger.getUserLocation())));

        progressDialog.dismiss();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
