package reaper.android.app.ui.screens.home;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service._new.LocationService_;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventsPresenterImpl implements EventsPresenter
{
    /* Services */
    private EventService eventService;

    /* Data */
    private GenericCache cache;
    private Location userLocation;
    private List<Event> events;

    /* View */
    private EventsView view;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventsPresenterImpl()
    {
        eventService = EventService.getInstance();
        cache = CacheManager.getGenericCache();
        userLocation = LocationService_.getInstance().getCurrentLocation();
        subscriptions = new CompositeSubscription();
        events = new ArrayList<>();
    }

    @Override
    public void attachView(final EventsView view)
    {
        this.view = view;

        this.view.showLoading();

        if (events.isEmpty())
        {
            Subscription subscription = eventService
                    ._fetchEvents()
                    .subscribeOn(Schedulers.newThread())
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
                            view.showError();
                        }

                        @Override
                        public void onNext(List<Event> events)
                        {
                            EventsPresenterImpl.this.events = events;

                            if (events.isEmpty())
                            {
                                view.showNoEventsMessage();
                            }
                            else
                            {
                                view.showEvents(events);
                            }
                        }
                    });

            subscriptions.add(subscription);
        }
        else
        {
            view.showEvents(events);
        }
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void refreshEvents()
    {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events)
        {
            eventIds.add(event.getId());
        }

        Subscription subscription = eventService
                ._refreshEvents(userLocation.getZone(), eventIds, cache
                        .get(GenericCacheKeys.FEED_LAST_UPDATE_TIMESTAMP, DateTime.class))
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
                        view.showError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        EventsPresenterImpl.this.events = events;

                        if (events.isEmpty())
                        {
                            view.showNoEventsMessage();
                        }
                        else
                        {
                            view.showEvents(events);
                        }
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void selectEvent(Event event)
    {
        int activePosition = events.indexOf(event);
        if (activePosition >= 0)
        {
            view.gotoDetailsView(events, activePosition);
        }
    }
}
