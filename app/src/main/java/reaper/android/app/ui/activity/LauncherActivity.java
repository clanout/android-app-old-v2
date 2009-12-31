package reaper.android.app.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import reaper.android.R;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.service.AuthService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.SessionValidatedTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.common.cache.AppPreferences;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class LauncherActivity extends AppCompatActivity
{
    private Bus bus;

    private LocationManager locationManager;

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

        authService = new AuthService(bus);
        locationService = new LocationService(bus);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        bus.register(this);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                    .setMessage("Turn on GPS")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            Toast.makeText(LauncherActivity.this, "We don't like people with trust issues", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            Toast.makeText(LauncherActivity.this, "We don't like people with trust issues", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            builder.create().show();
        }
        else
        {
            // Dummy Session initialization
            if (AppPreferences.get(this, "SESSION_COOKIE") == null)
            {
                AppPreferences.set(this, "SESSION_COOKIE", "dummy_session_cookie");
            }

            String sessionCookie = AppPreferences.get(this, "SESSION_COOKIE");
            Log.d("reap3r", "Session Cookie : " + sessionCookie);
            authService.validateSession(sessionCookie);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        bus.unregister(this);
    }

    @Subscribe
    public void onSessionValidatedTrigger(SessionValidatedTrigger trigger)
    {
        Cache.getInstance().put(CacheKeys.SESSION_COOKIE, trigger.getValidatedSessionCookie());

        if (!locationService.locationExists())
        {
            // TODO: Prompt to turn location on
            Log.d("reap3r", "Location does not exist in cache");
            progressDialog = ProgressDialog.show(this, "Welcome", "Fetching your current location...");
            bus.post(new UserLocationRefreshRequestTrigger());
        }
        else
        {
            Log.d("reap3r", "Location exists in cache");
            bus.post(new UserLocationRefreshRequestTrigger());
            gotoMainActivity();
        }
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
        Log.d("reap3r", "onUserLocationRefreshTrigger called");
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }
        gotoMainActivity();
    }

    public void gotoMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
