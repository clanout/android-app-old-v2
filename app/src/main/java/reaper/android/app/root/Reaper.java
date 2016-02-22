package reaper.android.app.root;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.concurrent.TimeUnit;

import reaper.android.BuildConfig;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.ui.activity.NoInternetActivity;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
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
    private Bus bus;

    // Services
    private UserService userService;
    private GenericCache genericCache;

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

        init();
        connectivityHandler();
        Stetho.initializeWithDefaults(this);
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
                            startActivity(NoInternetActivity
                                    .callingIntent(getApplicationContext()));
                        }

                        isConnected = isConnectedNow;
                    }
                });

    }

    protected void init()
    {
        instance = this;

        FacebookSdk.sdkInitialize(this);

        Dexter.initialize(this);

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }

        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);
        Communicator.init(bus);

        DatabaseManager.init(this);

        userService = new UserService(bus);
        genericCache = CacheManager.getGenericCache();

        handleTimesApplicationOpened();
    }

    private void handleTimesApplicationOpened()
    {
        int timesApplicationOpened;
        try
        {
            timesApplicationOpened = Integer
                    .parseInt(genericCache.get(CacheKeys.TIMES_APPLICATION_OPENED));
        }
        catch (Exception e)
        {
            timesApplicationOpened = 0;
        }

        timesApplicationOpened++;

        genericCache.put(CacheKeys.TIMES_APPLICATION_OPENED, timesApplicationOpened);

        String userId = userService.getActiveUserId();
        if (userId == null)
        {
            userId = "0";
        }

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.APP_LAUNCHED, "user - " + userId + " time - " + DateTime
                        .now(DateTimeZone.UTC)
                        .toString());
    }

    @Subscribe
    public void onFacebookFriendsIdFetched(FacebookFriendsIdFetchedTrigger trigger)
    {
        if (trigger.isPolling())
        {
            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger)
    {
        if (trigger.isPolling())
        {
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now());
        }
    }
}
