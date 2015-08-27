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
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.internal.PermissionType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;

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

    private static final String PERMISSION_REQUIRED = "This app requires your basic information, email and friends information to function properly. Don\\'t worry, we will not misuse this in any way.";
    private static final String PERMISSION_REQUIRED_TITLE = "Permission Required";
    private static final String PROBLEM_CONTACTING_FACEBOOK_TITLE = "Problem contacting Facebook";
    private static final String PROBLEM_CONTACTING_FACEBOOK = "There was some problem while connecting to Facebook. Please try again.";

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

        facebookLoginButton.setVisibility(View.GONE);
        if (AccessToken.getCurrentAccessToken() == null)
        {
            Log.d("APP", "token is null");
            setUpFacebookLoginButton();
        } else
        {
            if (AccessToken.getCurrentAccessToken().getDeclinedPermissions().size() > 0)
            {
                Log.d("APP", "token is not null and declined permissions ----- " + AccessToken.getCurrentAccessToken().getDeclinedPermissions());
                LoginManager.getInstance().logOut();
                setUpFacebookLoginButton();
            } else
            {
                Log.d("APP", "token is not null and declined permissions ------ " + AccessToken.getCurrentAccessToken().getDeclinedPermissions());
                goToLauncherActivity();
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void goToLauncherActivity()
    {
        Log.d("APP", "going to launcher activity");
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
                        Log.d("APP", "login result on success declined permissions --- " + loginResult.getRecentlyDeniedPermissions());
                        setUpAlertDialog(PERMISSION_REQUIRED, PERMISSION_REQUIRED_TITLE, "Grant Permission");
                    } else
                    {
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
                LoginManager.getInstance().logInWithReadPermissions(FacebookActivity.this, Arrays.asList("email", "user_friends"));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(FacebookActivity.this, R.string.trust_issues, Toast.LENGTH_LONG).show();
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
