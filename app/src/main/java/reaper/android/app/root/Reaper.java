package reaper.android.app.root;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.concurrent.TimeUnit;

import reaper.android.BuildConfig;
import reaper.android.app.cache._core.DatabaseManager;
import reaper.android.app.communication.Communicator;
import reaper.android.app.config.AppConstants;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.ChatService_;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.service._new.PhonebookService_;
import reaper.android.app.service._new.WhatsappService_;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class Reaper extends Application
{
    private static Reaper instance;
    private static Tracker tracker;

    private boolean isConnected;

    public static Reaper getReaperContext()
    {
        return instance;
    }

    synchronized public static Tracker getAnalyticsTracker()
    {
        if (tracker == null)
        {
            tracker = GoogleAnalytics.getInstance(instance)
                                     .newTracker(AppConstants.GOOGLE_ANALYTICS_TRACKING_KEY);
            tracker.enableExceptionReporting(true);

        }
        return tracker;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        /* Static reference */
        instance = this;

        /* Facebook SDK */
        FacebookSdk.sdkInitialize(this);

        /* Stetho (debugging) */
        Stetho.initializeWithDefaults(this);

        /* Logging */
        initLogging();

        /* Communicator (Event Bus) */
        initCommunicator();

        /* SQLite */
        initDb();

        /* Services */
        initServices();
    }

    private void initLogging()
    {
        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initCommunicator()
    {
        Bus bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);
        Communicator.init(bus);
    }

    private void initDb()
    {
        DatabaseManager.init(this);
    }

    private void initServices()
    {
        /* Gcm Service */
        GcmService_ gcmService = GcmService_.getInstance();

        /* Location Service */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationService_
                .init(getApplicationContext(), locationManager, GoogleService_.getInstance());
        LocationService_ locationService = LocationService_.getInstance();

        /* Phonebook Service */
        PhonebookService_.init(getApplicationContext());
        PhonebookService_ phonebookService = PhonebookService_.getInstance();

        /* User Service */
        UserService.init(locationService, phonebookService);
        UserService userService = UserService.getInstance();

        /* Event Service */
        EventService.init(gcmService, locationService, userService);
        EventService eventService = EventService.getInstance();


        /* Chat Service */
        ChatService_.init(userService, eventService);

        /* WhatsApp Service */
        WhatsappService_.init(userService);
    }

    private void connectivityHandler()
    {
        isConnected = true;

        Observable
                .interval(5, TimeUnit.SECONDS)
                .map(new Func1<Long, Boolean>()
                {
                    @Override
                    public Boolean call(Long aLong)
                    {
                        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();

                        return (netInfo != null && netInfo.isConnected());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
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
                    public void onNext(Boolean isConnectedNow)
                    {
                        if (isConnected && !isConnectedNow)
                        {
//                            startActivity(NoInternetActivity
//                                    .callingIntent(getApplicationContext()));
                        }

                        isConnected = isConnectedNow;
                    }
                });

    }
}
