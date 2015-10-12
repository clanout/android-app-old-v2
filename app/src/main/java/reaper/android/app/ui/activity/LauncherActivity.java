package reaper.android.app.ui.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.AuthService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.facebook.FacebookProfileFetchedTrigger;
import reaper.android.app.trigger.user.NewSessionCreatedTrigger;
import reaper.android.app.trigger.user.SessionValidatedTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.common.analytics.AnalyticsHelper;
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

        setContentView(R.layout.activity_launcher);
        ShimmerFrameLayout container =
                (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmerAnimation();

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

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.LAUNCHER_ACTIVITY);

        bus.register(this);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(R.string.location_permission_heading)
                    .setMessage(R.string.location_permission_message)
                    .setPositiveButton(R.string.location_permission_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.LOCATION_GRANTED, null);
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.location_permission_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.LOCATION_DENIED, null);
                            Toast.makeText(LauncherActivity.this, R.string.location_denied, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.LOCATION_DENIED, null);
                            Toast.makeText(LauncherActivity.this, R.string.location_denied, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            builder.create().show();
        } else
        {
            String sessionCookie = cache.get(CacheKeys.SESSION_ID);
            if (sessionCookie != null)
            {
                authService.validateSession(sessionCookie);
            } else
            {
                facebookService.getUserFacebookProfile();
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
        if (!locationService.locationExists())
        {
            progressDialog = ProgressDialog.show(this, "Welcome", "Getting your location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        } else
        {
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
            Toast.makeText(this, R.string.messed_up, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Subscribe
    public void onFacebookProfileFetched(FacebookProfileFetchedTrigger trigger)
    {
        cache.put(CacheKeys.USER_ID, trigger.getId());
        cache.put(CacheKeys.USER_FIRST_NAME, trigger.getFirstName());
        cache.put(CacheKeys.USER_LAST_NAME, trigger.getLastName());
        cache.put(CacheKeys.USER_GENDER, trigger.getGender());
        cache.put(CacheKeys.USER_EMAIL, trigger.getEmail());
        cache.put(CacheKeys.USER_NAME, trigger.getFirstName() + " " + trigger.getLastName());
        facebookService.getFacebookFriends(false);
    }

    @Subscribe
    public void onFacebookFriendsFetched(FacebookFriendsIdFetchedTrigger trigger)
    {
        authService.createNewSession(cache.get(CacheKeys.USER_FIRST_NAME), cache.get(CacheKeys.USER_LAST_NAME), cache.get(CacheKeys.USER_GENDER), cache.get(CacheKeys.USER_EMAIL), cache.get(CacheKeys.USER_ID), trigger.getFriendsIdList());
    }

    @Subscribe
    public void onFacebookFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE)
        {
            Toast.makeText(this, R.string.problem_contacting_facebook, Toast.LENGTH_LONG).show();
            LoginManager.getInstance().logOut();
            finish();
        }
    }

    @Subscribe
    public void onFacebookProfileNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_PROFILE_FETCH_FAILURE)
        {
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
            progressDialog = ProgressDialog.show(this, "Welcome", "Getting your location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        } else
        {
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
            facebookService.getUserFacebookProfile();
        }
    }

    @Subscribe
    public void onUserLocationRefreshTrigger(UserLocationRefreshTrigger trigger)
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.LOCATION_REFRESHED, null);

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
