package reaper.android.app.ui.screens.launch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.AuthService_;
import reaper.android.app.service._new.FacebookService_;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui._core.PermissionHandler;
import reaper.android.app.ui.screens.FlowEntry;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenter;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenterImpl;
import reaper.android.app.ui.screens.launch.mvp.bootstrap.BootstrapView;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenter;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenterImpl;
import reaper.android.app.ui.screens.launch.mvp.fb_login.FacebookLoginView;
import reaper.android.app.ui.util.SnackbarFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class LauncherActivity extends BaseActivity implements
        FacebookCallback<LoginResult>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        FacebookLoginView,
        BootstrapView
{
    private static final String ARG_FLOW_ENTRY = "arg_flow_entry";
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static Intent callingIntent(Context context, @FlowEntry int flowEntry, String eventId)
    {
        Intent intent = new Intent(context, LauncherActivity.class);

        intent.putExtra(ARG_FLOW_ENTRY, flowEntry);
        intent.putExtra(ARG_EVENT_ID, eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

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

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.llAction)
    View llAction;

    @Bind(R.id.tvActionMessage)
    TextView tvActionMessage;

    @Bind(R.id.tvAction)
    TextView tvAction;

    /* Fields */
    CallbackManager facebookCallbackManager;

    GoogleService_ googleService;
    FacebookService_ facebookService;
    LocationService_ locationService;
    UserService userService;
    AuthService_ authService;
    GcmService_ gcmService;

    boolean introScrolled;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        /* Init View */
        displayBootstrapView();

        facebookCallbackManager = CallbackManager.Factory.create();

        /* Google Service */
        GoogleService_.init();
        googleService = GoogleService_.getInstance();

        /* Facebook Service */
        FacebookService_.init();
        facebookService = FacebookService_.getInstance();

        /* Location Service */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationService_.init(getApplicationContext(), locationManager, googleService);
        locationService = LocationService_.getInstance();

        /* User Service */
        userService = UserService.getInstance();

        /* Auth Service */
        AuthService_.init(facebookService, userService);
        authService = AuthService_.getInstance();

        /* Gcm Service */
        GcmService_.init();
        gcmService = GcmService_.getInstance();

        /* Presenters */
        EventService eventService = EventService.getInstance();
        facebookLoginPresenter = new FacebookLoginPresenterImpl(authService, facebookService);
        bootstrapPresenter = new BootstrapPresenterImpl(locationService, authService, gcmService, eventService);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setupGooglePlayService();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        facebookLoginPresenter.detachView();
        bootstrapPresenter.detachView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHandler.Permissions.LOCATION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                if (PermissionHandler
                        .isRationalRequired(this, PermissionHandler.Permissions.LOCATION))
                {
                    showBootstrapAction();

                    tvActionMessage.setText(R.string.permission_location_message);
                    tvAction.setText(R.string.permission_request_again);
                    tvAction.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            PermissionHandler
                                    .requestPermission(LauncherActivity.this, PermissionHandler.Permissions.LOCATION);
                        }
                    });
                }
                else
                {
                    showBootstrapAction();

                    tvActionMessage.setText(R.string.permission_location_message);
                    tvAction.setText(R.string.permission_goto_settings);
                    tvAction.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            LauncherActivity.this.gotoAppSettings();
                        }
                    });
                }
            }
            else
            {
                bootstrapPresenter.attachView(this);
            }
        }
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
        SnackbarFactory.create(this, R.string.error_facebook_login);
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
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        llAction.setVisibility(View.GONE);
    }

    @Override
    public void handleLocationPermissions()
    {
        if (PermissionHandler.isRationalRequired(this, PermissionHandler.Permissions.LOCATION))
        {
            showBootstrapAction();

            tvActionMessage.setText(R.string.permission_location_message);
            tvAction.setText(R.string.permission_request_again);
            tvAction.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PermissionHandler
                            .requestPermission(LauncherActivity.this, PermissionHandler.Permissions.LOCATION);
                }
            });
        }
        else
        {
            // Location permission has not been granted yet. Request it directly.
            PermissionHandler.requestPermission(this, PermissionHandler.Permissions.LOCATION);
        }
    }

    @Override
    public void displayLocationServiceUnavailableMessage()
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.location_off_message);
        tvAction.setText(R.string.location_off_action);
        tvAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    @Override
    public void displayError()
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.error_default);
        tvAction.setText(R.string.bootstrap_error_action);
        tvAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bootstrapPresenter.attachView(LauncherActivity.this);
            }
        });
    }

    @Override
    public void proceed(List<Event> events)
    {
        gotoMainActivity(events);
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
        if (!googleService.isGoogleApiClientSet())
        {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            googleService.setGoogleApiClient(googleApiClient);
        }

        if (!googleService.isConnected())
        {
            googleService.connect();

        }
        else
        {
            facebookLoginPresenter.attachView(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
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

    private void showBootstrapAction()
    {
        loading.setVisibility(View.GONE);
        llAction.setVisibility(View.VISIBLE);
    }

    private void displayPlayServicesErrorDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_default, null);
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_default, null);
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

    private void gotoMainActivity(List<Event> events)
    {
        Intent sourceIntent = getIntent();

        @FlowEntry int flowEntry = sourceIntent.getIntExtra(ARG_FLOW_ENTRY, FlowEntry.HOME);
        String eventId = sourceIntent.getStringExtra(ARG_EVENT_ID);
        ArrayList<Event> eventList = (ArrayList<Event>) events;

        startActivity(MainActivity.callingIntent(this, flowEntry, eventId, eventList));
    }
}
