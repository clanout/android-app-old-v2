package reaper.android.app.ui.screens.launch.mvp.bootstrap;

import reaper.android.app.model.Location;
import reaper.android.app.service.AuthService_;
import reaper.android.app.service.FacebookService_;
import reaper.android.app.service.GCMService;
import reaper.android.app.service.LocationService_;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class
BootstrapPresenterImpl implements BootstrapPresenter
{
    private BootstrapView view;
    private LocationService_ locationService;
    private FacebookService_ facebookService;
    private AuthService_ authService;
    private GCMService gcmService;

    private CompositeSubscription subscriptions;

    public BootstrapPresenterImpl(LocationService_ locationService, FacebookService_ facebookService,
                                  AuthService_ authService, GCMService gcmService)
    {
        this.locationService = locationService;
        this.facebookService = facebookService;
        this.authService = authService;
        this.gcmService = gcmService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(BootstrapView view)
    {
        this.view = view;

        Timber.v("Auth Token : " + facebookService.getAccessToken());

        if (locationService.isLocationServiceAvailable())
        {
            init();
        }
        else
        {
            this.view.displayLocationServiceUnavailableMessage();
        }
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    private void init()
    {
        Subscription subscription =
                Observable
                        .zip(getSessionObservable(), getLocationObservable(), new Func2<Boolean, Location, Location>()
                        {
                            @Override
                            public Location call(Boolean isSessionInitialized, Location location)
                            {
                                if (isSessionInitialized && location != null)
                                {
                                    return location;
                                }
                                else
                                {
                                    throw new IllegalStateException();
                                }
                            }
                        })
                        .flatMap(new Func1<Location, Observable<Boolean>>()
                        {
                            @Override
                            public Observable<Boolean> call(Location location)
                            {
                                return locationService.pushUserLocation();
                            }
                        })
                        .flatMap(new Func1<Boolean, Observable<Object>>()
                        {
                            @Override
                            public Observable<Object> call(Boolean isLocationPushed)
                            {
                                if (!isLocationPushed)
                                {
                                    throw new IllegalStateException();
                                }
                                else
                                {
                                    return Observable.empty();
                                }
                            }
                        })
                        .doOnCompleted(new Action0()
                        {
                            @Override
                            public void call()
                            {
                                // Register WIth GCM
                                gcmService.register();
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Object>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                view.proceed();
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                view.displayError();
                            }

                            @Override
                            public void onNext(Object o)
                            {
                            }
                        });

        subscriptions.add(subscription);
    }

    private Observable<Location> getLocationObservable()
    {
        return locationService.fetchCurrentLocation();
    }

    private Observable<Boolean> getSessionObservable()
    {
        return authService.initSession();

    }
}
