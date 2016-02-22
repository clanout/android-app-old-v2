package reaper.android.app.ui.activity.obsolete;

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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.service.AuthService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.facebook.FacebookProfileFetchedTrigger;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.app.trigger.user.NewSessionCreatedTrigger;
import reaper.android.app.trigger.user.SessionValidatedTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshRequestTrigger;
import reaper.android.app.trigger.user.UserLocationRefreshTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;


public class LauncherActivity extends AppCompatActivity
{
    private Bus bus;
    private LocationManager locationManager;

    private GenericCache genericCache;

    // Services
    private AuthService authService;
    private LocationService locationService;
    private FacebookService facebookService;
    private UserService userService;
    private GcmService_ gcmService;
    private NotificationCache notificationCache;

    // UI Elements
    private ProgressDialog progressDialog;

    private boolean isBlocking;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.LAUNCHER_ACTIVITY);

        setContentView(R.layout.activity_launcher);
        ShimmerFrameLayout container =
                (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmerAnimation();

        bus = Communicator.getInstance().getBus();

        authService = new AuthService(bus);
        gcmService = GcmService_.getInstance();
        locationService = new LocationService(bus);
        facebookService = new FacebookService(bus);
        userService = new UserService(bus);
        notificationCache = CacheManager.getNotificationCache();

        genericCache = CacheManager.getGenericCache();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d("APP", "onResume launcher");


        bus.register(this);


        proceed();

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d("APP", "onPause");
        bus.unregister(this);

    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Subscribe
    public void onNewSessionCreated(NewSessionCreatedTrigger trigger)
    {
        genericCache.put(CacheKeys.SESSION_ID, trigger.getSessionCookie());
        if (!locationService.locationExists())
        {
//            progressDialog = ProgressDialog.show(this, "Welcome", "Getting your location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        }
        else
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
        genericCache.put(CacheKeys.USER_ID, trigger.getId());
        genericCache.put(CacheKeys.USER_FIRST_NAME, trigger.getFirstName());
        genericCache.put(CacheKeys.USER_LAST_NAME, trigger.getLastName());
        genericCache.put(CacheKeys.USER_GENDER, trigger.getGender());
        genericCache.put(CacheKeys.USER_EMAIL, trigger.getEmail());
        genericCache.put(CacheKeys.USER_NAME, trigger.getFirstName() + " " + trigger.getLastName());
        facebookService.getFacebookFriends(false);
    }

    @Subscribe
    public void onFacebookFriendsFetched(FacebookFriendsIdFetchedTrigger trigger)
    {
        authService.createNewSession(genericCache.get(CacheKeys.USER_FIRST_NAME), genericCache
                .get(CacheKeys.USER_LAST_NAME), genericCache
                .get(CacheKeys.USER_GENDER), genericCache.get(CacheKeys.USER_EMAIL), genericCache
                .get(CacheKeys.USER_ID), trigger.getFriendsIdList());
    }

    @Subscribe
    public void onFacebookFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE)
        {
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.FACEBOOK_FRIENDS_NOT_FETCHED, userService
                            .getActiveUserId());
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
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.FACEBOOK_PROFILE_NOT_FETCHED, userService
                            .getActiveUserId());
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
//            progressDialog = ProgressDialog.show(this, "Welcome", "Getting your location...");
            isBlocking = true;
            bus.post(new UserLocationRefreshRequestTrigger());
        }
        else
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
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.LOCATION_REFRESHED, null);

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

        if (genericCache.get(CacheKeys.GCM_TOKEN) == null)
        {
            if (checkPlayServices())
            {

                Log.d("APP", "starting gcm registeration " + System.currentTimeMillis());

                gcmService.register();

            }
            else
            {
            }
        }
        else
        {


            startActivity(handleIntent());

            Log.d("APP", "going to main activity without gcm registeration " + System
                    .currentTimeMillis());

            finish();
        }
    }


    private Intent handleIntent()
    {

        Intent intent = new Intent(this, MainActivity.class);

        String shouldGoToNotificationFragment = getIntent()
                .getStringExtra(BundleKeys.SHOULD_GO_TO_NOTIFICATION_FRAGMENT);

        if (shouldGoToNotificationFragment != null)
        {
            if (shouldGoToNotificationFragment.equals("yes"))
            {
                intent.putExtra(BundleKeys.SHOULD_GO_TO_NOTIFICATION_FRAGMENT, "yes");
                startActivity(intent);
                finish();
            }
        }
        else
        {

            String shouldGoToDetailsFragment = getIntent()
                    .getStringExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT);
            if (shouldGoToDetailsFragment == null)
            {
                shouldGoToDetailsFragment = "no";
                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);

            }
            else
            {
                if (shouldGoToDetailsFragment.equals("yes"))
                {

                    notificationCache.clearAll();

                    String eventId = getIntent().getStringExtra("event_id");
                    intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);
                    intent.putExtra("event_id", eventId);

                    if (getIntent().getBooleanExtra(BundleKeys.POPUP_STATUS_DIALOG, false))
                    {
                        intent.putExtra(BundleKeys.POPUP_STATUS_DIALOG, true);
                    }
                    else
                    {
                        intent.putExtra(BundleKeys.POPUP_STATUS_DIALOG, false);
                    }

                    String shouldGoToChatFragment = getIntent()
                            .getStringExtra(BundleKeys.SHOULD_GO_TO_CHAT_FRAGMENT);

                    if (shouldGoToChatFragment == null)
                    {
                        shouldGoToChatFragment = "no";
                        intent.putExtra(BundleKeys.SHOULD_GO_TO_CHAT_FRAGMENT, shouldGoToChatFragment);
                    }
                    else
                    {

                        intent.putExtra(BundleKeys.SHOULD_GO_TO_CHAT_FRAGMENT, shouldGoToChatFragment);
                    }

                }
                else
                {
                    intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, shouldGoToDetailsFragment);
                }
            }

        }
        return intent;
    }


    private void proceed()
    {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(R.string.location_permission_heading)
                    .setMessage(R.string.location_permission_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.location_permission_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            AnalyticsHelper
                                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.LOCATION_GRANTED, null);
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.location_permission_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            AnalyticsHelper
                                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.LOCATION_DENIED, null);
                            Toast.makeText(LauncherActivity.this, R.string.location_denied, Toast.LENGTH_LONG)
                                 .show();
                            finish();
                        }
                    });
            builder.create().show();
        }
        else
        {
            String sessionCookie = genericCache.get(CacheKeys.SESSION_ID);
            if (sessionCookie != null)
            {
                authService.validateSession(sessionCookie);
            }
            else
            {
                facebookService.getUserFacebookProfile();
            }
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {

                Toast.makeText(this, "This device does not support Google Play Services.", Toast.LENGTH_LONG)
                     .show();
                finish();
            }

            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.PLAY_SERVICES_NOT_PRESENT, null);

            return false;
        }

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.PLAY_SERVICES_PRESENT, null);

        return true;
    }

    @Subscribe
    public void onGcmRegistrationComplete(GcmRegistrationCompleteTrigger trigger)
    {


        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                Log.d("APP", "going to main activity after gcm registeration" + System
                        .currentTimeMillis());

                startActivity(handleIntent());
                finish();
            }
        });
    }
}
