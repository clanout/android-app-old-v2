package reaper.android.app.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.otto.Bus;

import java.util.Arrays;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.service.UserService;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

/**
 * Created by Aditya on 23-08-2015.
 */
public class FacebookActivity extends AppCompatActivity
{
    private LoginButton facebookLoginButton;
    private CallbackManager facebookCallbackManager;
    private FacebookCallback<LoginResult> facebookCallback;
    private ProfileTracker profileTracker;

    private GenericCache genericCache;
    private EventCache eventCache;
    private UserCache userCache;
    private UserService userService;
    private Bus bus;

    private boolean backFromSettingsPage;

    private static final String PERMISSION_REQUIRED = "Allow access to your basic profile information, email and friend list. This is required to connect you with your facebook friends on clanOut";
    private static final String PERMISSION_REQUIRED_TITLE = "Request for permission";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // TODO ---- disable development mode in facebook and enable public mode. Also, turn on deep linking if 'share to facebook' opton is required. Check proguard for facebook sdk.

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_facebook);

        facebookLoginButton = (LoginButton) findViewById(R.id.flb_activity_facebook);
        facebookCallbackManager = CallbackManager.Factory.create();
        setUpFacebookCallback();
        bus = Communicator.getInstance().getBus();
        genericCache = CacheManager.getGenericCache();
        eventCache = CacheManager.getEventCache();
        userCache = CacheManager.getUserCache();
        userService = new UserService(bus);

        facebookLoginButton.setVisibility(View.GONE);

        if (AccessToken.getCurrentAccessToken() == null)
        {
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.FB_ACCESS_TOKEN_NULL, null);
            setUpFacebookLoginButton();
        } else
        {
            if (AccessToken.getCurrentAccessToken().getDeclinedPermissions().size() > 0)
            {
                LoginManager.getInstance().logOut();
                setUpFacebookLoginButton();
            } else
            {
                goToLauncherActivity();
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.FACEBOOK_ACTIVITY);

        if(backFromSettingsPage)
        {
            goToLauncherActivity();
        }

    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void goToLauncherActivity()
    {
        facebookLoginButton.setVisibility(View.GONE);
        handleLocationPermission();
    }

    private void setUpFacebookLoginButton()
    {
        facebookLoginButton.setVisibility(View.VISIBLE);
        facebookLoginButton.setReadPermissions(Arrays.asList("email", "user_friends"));
        facebookLoginButton.registerCallback(facebookCallbackManager, facebookCallback);
    }

    private void setUpFacebookCallback()
    {
        facebookCallback = new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                if (loginResult.getAccessToken() != null)
                {
                    if (loginResult.getRecentlyDeniedPermissions().size() > 0)
                    {
                        setUpAlertDialog(PERMISSION_REQUIRED, PERMISSION_REQUIRED_TITLE, "ALLOW");
                    } else
                    {
                        genericCache.delete(CacheKeys.ACTIVE_FRAGMENT);
                        genericCache.delete(CacheKeys.GCM_TOKEN);
                        genericCache.delete(CacheKeys.GCM_TOKEN_SENT_TO_SERVER);
                        genericCache.delete(CacheKeys.LAST_UPDATE_TIMESTAMP);
                        genericCache.delete(CacheKeys.SESSION_ID);
                        genericCache.delete(CacheKeys.USER_ID);
                        genericCache.delete(CacheKeys.USER_LOCATION);
                        genericCache.delete(CacheKeys.USER_NAME);
                        genericCache.delete(CacheKeys.USER_COVER_PIC);
                        genericCache.delete(CacheKeys.USER_EMAIL);
                        genericCache.delete(CacheKeys.USER_FIRST_NAME);
                        genericCache.delete(CacheKeys.USER_LAST_NAME);
                        genericCache.delete(CacheKeys.USER_GENDER);

                        userCache.deleteFriends();
                        userCache.deleteContacts();

                        eventCache.deleteAll();

                        goToLauncherActivity();
                    }
                } else
                {
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.FACEBOOK_ACCESS_TOKEN_NULL_LOGIN_RESULT, userService.getActiveUserId());
                    Toast.makeText(FacebookActivity.this, R.string.messed_up, Toast.LENGTH_LONG).show();
                    FacebookActivity.this.finish();
                }
            }

            @Override
            public void onCancel()
            {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.ON_CANCEL_FACEBOOK_CALLBACK, userService.getActiveUserId());
            }

            @Override
            public void onError(FacebookException e)
            {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.ON_ERROR_FACEBOOK_CALLBACK, userService.getActiveUserId() + " message - " + e.getMessage());
            }
        };

    }

    private void setUpAlertDialog(String message, String title, String positiveButtonText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(FacebookActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.FACEBOOK_PERMISSION_GRANTED, null);
                LoginManager.getInstance().logInWithReadPermissions(FacebookActivity.this, Arrays.asList("email", "user_friends"));
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FacebookActivity.this, R.string.location_denied, Toast.LENGTH_LONG).show();
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.FACEBOOK_PERMISSION_DENIED, null);
                FacebookActivity.this.finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleLocationPermission() {

        Log.d("APP", "inside handleLocationPermission");

        try {
            Dexter.checkPermission(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    Log.d("APP", "inside handleLocationPermission ---- permission granted");

                    Intent intent = new Intent(FacebookActivity.this, LauncherActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    Log.d("APP", "inside handleLocationPermission ---- permission denied");
                    if (permissionDeniedResponse.isPermanentlyDenied()) {


                        displayLocationRequiredDialogPermanentlyDeclinedCase();

                    } else {

                        displayLocationRequiredDialog();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    Log.d("APP", "inside handleLocationPermission ---- permission rationale shown");
                    permissionToken.continuePermissionRequest();
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        }catch (Exception e)
        {
            Log.d("APP", "Exception in Dexter --- while asking for location permission");
        }
    }

    private void displayLocationRequiredDialogPermanentlyDeclinedCase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.location_required_title);
        builder.setMessage(R.string.location_required_message);
        builder.setPositiveButton("TAKE ME TO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(FacebookActivity.this, R.string.location_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        builder.create().show();
    }

    private void goToSettings() {

        backFromSettingsPage = true;

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void displayLocationRequiredDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.location_required_title);
        builder.setMessage(R.string.location_required_message);
        builder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                try {
                    Dexter.checkPermission(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                            Intent intent = new Intent(FacebookActivity.this, LauncherActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            Toast.makeText(FacebookActivity.this, R.string.location_denied, Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            permissionToken.continuePermissionRequest();
                        }
                    }, Manifest.permission.ACCESS_FINE_LOCATION);
                }catch (Exception e)
                {

                }
            }
        });


        builder.create().show();
    }

}
