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

import com.facebook.login.LoginManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.service.AuthService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookProfileFetchedTrigger;
import reaper.android.app.trigger.user.NewSessionCreatedTrigger;
import reaper.android.app.trigger.user.SessionValidatedTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.common.communicator.Communicator;

public class LauncherActivity extends AppCompatActivity
{
    private Bus bus;
    private LocationManager locationManager;

    private GenericCache cache;

    // Services
    private AuthService authService;
    private LocationService locationService;
    private FacebookService facebookService;

    // UI Elements
    private ProgressDialog progressDialog;

    private boolean isBlocking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bus = Communicator.getInstance().getBus();

        authService = new AuthService(bus);
        locationService = new LocationService(bus);
        facebookService = new FacebookService(bus);

        cache = CacheManager.getGenericCache();

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
                            Toast.makeText(LauncherActivity.this, R.string.trust_issues, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            Toast.makeText(LauncherActivity.this, R.string.trust_issues, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            builder.create().show();
        } else
        {
            String sessionCookie = cache.get(CacheKeys.SESSION_ID);
            if (sessionCookie != null)
            {
                Log.d("APP", "session cookie not null");
                authService.validateSession(sessionCookie);
            } else
            {
                Log.d("APP", "session cookie null");
                facebookService.getUserProfile();
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        bus.unregister(this);
    }

    @Subscribe
    public void onNewSessionCreated(NewSessionCreatedTrigger trigger)
    {
        cache.put(CacheKeys.SESSION_ID, trigger.getSessionCookie());
        Log.d("APP", "new session created");
        if (!locationService.locationExists())
        {
            Log.d("APP", "location not exists");
            progressDialog = ProgressDialog.show(this, "Welcome", "Fetching your current location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        } else
        {
            Log.d("APP", "location exists");
            isBlocking = false;
            bus.post(new UserLocationRefreshRequestTrigger());
            gotoMainActivity();
        }
    }

    @Subscribe
    public void onNewSessionNotCreated(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.NEW_SESSION_CREATION_FAILURE)
        {
            Log.d("APP", "new session not created");
            Toast.makeText(this, R.string.messed_up, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Subscribe
    public void onFacebookProfileFetched(FacebookProfileFetchedTrigger trigger)
    {
        Log.d("APP", "facebook profile fetched");
        authService.createNewSession(trigger.getFirstName(), trigger.getLastName(), trigger.getGender(), trigger.getEmail(), trigger.getId());
        cache.put(CacheKeys.USER_ID, trigger.getId());
        cache.put(CacheKeys.USER_NAME, trigger.getFirstName() + " " + trigger.getLastName());
    }

    @Subscribe
    public void onFacebookProfileNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_PROFILE_FETCH_FAILURE)
        {
            Log.d("APP", "facebook profile not fetched");
            Toast.makeText(this, R.string.problem_contacting_facebook, Toast.LENGTH_LONG).show();
            LoginManager.getInstance().logOut();
            finish();
        }
    }

    @Subscribe
    public void onSessionValidated(SessionValidatedTrigger trigger)
    {
        if (!locationService.locationExists())
        {
            Log.d("APP", "location exists");
            progressDialog = ProgressDialog.show(this, "Welcome", "Fetching your current location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        } else
        {
            Log.d("APP", "location not exists");
            isBlocking = false;
            bus.post(new UserLocationRefreshRequestTrigger());
            gotoMainActivity();
        }
    }

    @Subscribe
    public void onGenericErrorTrigger(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.INVALID_SESSION)
        {
            Log.d("APP", "session is invalid");
            facebookService.getUserProfile();
        }
    }

    @Subscribe
    public void onUserLocationRefreshTrigger(UserLocationRefreshTrigger trigger)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        if (isBlocking)
        {
            gotoMainActivity();
        }
    }

    public void gotoMainActivity()
    {
        Log.d("APP", "going to main activity");
        Intent intent = new Intent(this, MainActivity.class);

        String shouldGoToDetailsFragment = getIntent().getStringExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT);
        if (shouldGoToDetailsFragment == null)
        {
            shouldGoToDetailsFragment = "no";
            intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);

        } else
        {
            if (shouldGoToDetailsFragment.equals("yes"))
            {
                String eventId = getIntent().getStringExtra("event_id");
                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);
                intent.putExtra("event_id", eventId);

            } else
            {
                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);
            }
        }
        startActivity(intent);
        finish();
    }
}
