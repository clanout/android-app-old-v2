package reaper.android.app.ui.screens.launch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.service.AuthService_;
import reaper.android.app.service.FacebookService_;
import reaper.android.app.service.GCMService;
import reaper.android.app.service.LocationService_;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenter;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenterImpl;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapView;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenter;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenterImpl;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginView;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.common.communicator.Communicator;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;


public class LauncherActivity extends AppCompatActivity implements
        FacebookCallback<LoginResult>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        FacebookLoginView,
        BootstrapView
{
    FacebookLoginPresenter facebookLoginPresenter;

    BootstrapPresenter bootstrapPresenter;

    /* UI Elements */
    @Bind(R.id.rlBootstrap)
    View rlBootstrap;

    @Bind(R.id.llFb)
    View llFb;

    @Bind(R.id.vpIntro)
    ViewPager vpIntro;

    @Bind(R.id.btnFbLogin)
    LoginButton btnFbLogin;

    @Bind(R.id.ivIntro1)
    ImageView ivIntro1;

    @Bind(R.id.ivIntro2)
    ImageView ivIntro2;

    @Bind(R.id.ivIntro3)
    ImageView ivIntro3;

    @Bind(R.id.ivIntro4)
    ImageView ivIntro4;

    /* Fields */
    Bus bus;
    CallbackManager facebookCallbackManager;
    GoogleApiClient googleApiClient;
    FacebookService_ facebookService;
    boolean introScrolled;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher_);
        ButterKnife.bind(this);

        // Init View
        displayBootstrapView();

        bus = Communicator.getInstance().getBus();
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookService = new FacebookService_();
        facebookLoginPresenter = new FacebookLoginPresenterImpl(facebookService);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setupGooglePlayService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /* View Methods (FacebookLogonView) */
    @Override
    public void displayFacebookLoginButton()
    {
        displayFbLoginView();

        btnFbLogin.setVisibility(View.VISIBLE);
        btnFbLogin.setReadPermissions(FacebookService_.PERMISSIONS);
        btnFbLogin.registerCallback(facebookCallbackManager, this);
    }

    @Override
    public void displayFacebookLoginError()
    {
        SnackbarFactory.create(findViewById(android.R.id.content), "Facebook Login Error");
    }

    @Override
    public void displayFacebookPermissionsMessage()
    {
        displayFacebookPermissionsDialog();
    }

    @Override
    public void proceedToSessionValidation()
    {
        displayBootstrapView();
        facebookLoginPresenter.detachView();
        bootstrapPresenter.attachView(this);
    }

    /* View Methods (BootstrapView) */
    @Override
    public void displayLocationServiceUnavailableMessage()
    {
        displayLocationServiceDialog();
    }

    @Override
    public void displayError()
    {
        SnackbarFactory.create(findViewById(android.R.id.content), "Server Error");
    }

    @Override
    public void proceed()
    {
        UserService userService = new UserService(bus);
        Timber.v("[USER] " + userService.getActiveUser().toString());

        gotoMainActivity();
        finish();
    }

    /* Facebook Login Callbacks */
    @Override
    public void onSuccess(LoginResult loginResult)
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginSuccess();
        }
    }

    @Override
    public void onCancel()
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginCancel();
        }
    }

    @Override
    public void onError(FacebookException error)
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginError();
        }
    }

    /* Google Play Services */
    private void setupGooglePlayService()
    {
        if (googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!googleApiClient.isConnected())
        {
            googleApiClient.connect();

        }
        else
        {
            initBootstrapPresenter();
            facebookLoginPresenter.attachView(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        initBootstrapPresenter();
        facebookLoginPresenter.attachView(this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        displayPlayServicesErrorDialog();
    }

    /* Helper Methods */
    private void initBootstrapPresenter()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationService_.init(getApplicationContext(), locationManager, googleApiClient);
        LocationService_ locationService = LocationService_.getInstance();

        AuthService_ authService = new AuthService_(facebookService, CacheManager
                .getGenericCache());
        GCMService gcmService = new GCMService(bus);

        bootstrapPresenter = new BootstrapPresenterImpl(locationService, facebookService, authService, gcmService);
    }

    private void setupIntroViewPager()
    {
        introScrolled = false;

        vpIntro.setAdapter(new IntroAdapter(getFragmentManager()));
        vpIntro.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                setIntroMarker(position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        vpIntro.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                introScrolled = true;
                return false;
            }
        });

        Observable.interval(2, TimeUnit.SECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Subscriber<Long>()
                             {
                                 @Override
                                 public void onCompleted()
                                 {

                                 }

                                 @Override
                                 public void onError(Throwable e)
                                 {

                                 }

                                 @Override
                                 public void onNext(Long aLong)
                                 {
                                     if (!introScrolled)
                                     {
                                         int position = vpIntro.getCurrentItem();
                                         if (position == 3)
                                         {
                                             position = 0;
                                         }
                                         else
                                         {
                                             position++;
                                         }

                                         setIntroMarker(position);
                                         vpIntro.setCurrentItem(position);
                                     }
                                     else
                                     {
                                         introScrolled = false;
                                     }
                                 }
                             }
                  );
    }

    private void setIntroMarker(int position)
    {
        switch (position)
        {
            case 0:
                ivIntro1.setImageResource(R.drawable.intro_dot_selected);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 1:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot_selected);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 2:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot_selected);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 3:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot_selected);
                break;
        }
    }

    private void displayBootstrapView()
    {
        llFb.setVisibility(View.GONE);
        rlBootstrap.setVisibility(View.VISIBLE);
    }

    private void displayFbLoginView()
    {
        llFb.setVisibility(View.VISIBLE);
        rlBootstrap.setVisibility(View.GONE);

        setupIntroViewPager();
    }

    private void displayPlayServicesErrorDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_default, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

        tvTitle.setText(R.string.play_services_error_dialog_title);
        tvMessage.setText(R.string.play_services_error_dialog_message);

        builder.setCancelable(false);

        builder.setNegativeButton(R.string.play_services_error_dialog_negative_button,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        closeApp();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void displayFacebookPermissionsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_default, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

        tvTitle.setText(R.string.facebook_permission_dialog_title);
        tvMessage.setText(R.string.facebook_permission_dialog_message);

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.facebook_permission_dialog_positive_button,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        LoginManager
                                .getInstance()
                                .logInWithReadPermissions(LauncherActivity.this, FacebookService_.PERMISSIONS);
                    }
                });

        builder.setNegativeButton(R.string.facebook_permission_dialog_negative_button,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        closeApp();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void displayLocationServiceDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_default, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

        tvTitle.setText(R.string.location_permission_title);
        tvMessage.setText(R.string.location_permission_message);

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.location_permission_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.setNegativeButton(R.string.location_permission_negative_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                closeApp();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void closeApp()
    {
        finish();
        System.exit(0);
    }

    // TODO : Refactor
    private void gotoMainActivity()
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
                    CacheManager.getNotificationCache().clearAll();

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

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
