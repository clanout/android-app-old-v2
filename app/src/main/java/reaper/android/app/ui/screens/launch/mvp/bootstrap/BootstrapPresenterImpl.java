package reaper.android.app.ui.screens.launch.mvp.bootstrap;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service._new.AuthService_;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.LocationService_;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class BootstrapPresenterImpl implements BootstrapPresenter
{
    private BootstrapView view;
    private LocationService_ locationService;
    private AuthService_ authService;
    private GcmService_ gcmService;
    private EventService eventService;

    private CompositeSubscription subscriptions;

    public BootstrapPresenterImpl(LocationService_ locationService, AuthService_ authService,
                                  GcmService_ gcmService, EventService eventService)
    {
        this.locationService = locationService;
        this.authService = authService;
        this.gcmService = gcmService;
        this.eventService = eventService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(BootstrapView view)
    {
        this.view = view;

        this.view.showLoading();
        if (locationService.isLocationPermissionGranted())
        {
            if (locationService.isLocationServiceAvailable())
            {
                init();
            }
            else
            {
                this.view.displayLocationServiceUnavailableMessage();
            }
        }
        else
        {
            this.view.handleLocationPermissions();
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
                getBootstrapObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Event>>()
                        {
                            @Override
                            public void onCompleted()
                            {
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                view.displayError();
                            }

                            @Override
                            public void onNext(List<Event> events)
                            {
                                view.proceed(events);
                            }
                        });

        subscriptions.add(subscription);
    }

    private Observable<List<Event>> getBootstrapObservable()
    {
        return Observable
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
                            throw new IllegalStateException("[Bootstrap Error] session/fetch_location failed");
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
                .flatMap(new Func1<Boolean, Observable<List<Event>>>()
                {
                    @Override
                    public Observable<List<Event>> call(Boolean isLocationPushed)
                    {
                        if (!isLocationPushed)
                        {
                            throw new IllegalStateException("[Bootstrap Error] location push failed");
                        }
                        else
                        {
                            return eventService
                                    ._fetchEvents(locationService.getCurrentLocation()
                                                                 .getZone());
                        }
                    }
                })
                .doOnCompleted(new Action0()
                {
                    @Override
                    public void call()
                    {
                        // Register With GCM
                        gcmService.register();
                    }
                })
                .subscribeOn(Schedulers.newThread());
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
