package reaper.android.app.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.Arrays;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.common.analytics.AnalyticsHelper;

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
        genericCache = CacheManager.getGenericCache();
        eventCache = CacheManager.getEventCache();
        userCache = CacheManager.getUserCache();

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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void goToLauncherActivity()
    {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
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
                    Toast.makeText(FacebookActivity.this, R.string.messed_up, Toast.LENGTH_LONG).show();
                    FacebookActivity.this.finish();
                }
            }

            @Override
            public void onCancel()
            {
            }

            @Override
            public void onError(FacebookException e)
            {
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
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
}
