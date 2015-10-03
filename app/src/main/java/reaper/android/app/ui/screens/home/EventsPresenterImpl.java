package reaper.android.app.ui.screens.home;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class EventsPresenterImpl implements EventsPresenter
{
    /* Services */
    private EventService eventService;
    private UserService userService;

    /* Data */
    private GenericCache cache;
    private Location userLocation;
    private List<Event> events;

    /* View */
    private EventsView view;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventsPresenterImpl(Bus bus)
    {
        eventService = new EventService(bus);
        userService = new UserService(bus);
        cache = CacheManager.getGenericCache();
        userLocation = new LocationService(bus).getUserLocation();
        subscriptions = new CompositeSubscription();
        events = new ArrayList<>();
    }

    @Override
    public void attachView(final EventsView view)
    {
        this.view = view;

        this.view.showLoading();
        Subscription subscription = eventService
                ._fetchEvents(userLocation.getZone())
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
                        .get(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.class))
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

    @Override
    public void updateRsvp(final EventsView.EventListItem eventListItem, final Event event, Event.RSVP rsvp)
    {
        final Event.RSVP oldRsvp = event.getRsvp();
        if (oldRsvp == rsvp)
        {
            return;
        }

        if (event.getOrganizerId().equals(userService.getActiveUserId()))
        {
            view.showOrganizerCannotUpdateRsvpError();
            return;
        }

        event.setRsvp(rsvp);
        eventListItem.render(event);

        Subscription subscription = eventService
                ._updateRsvp(event)
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
                        view.showRsvpUpdateError();
                        event.setRsvp(oldRsvp);
                        eventListItem.render(event);
                    }

                    @Override
                    public void onNext(Boolean aBoolean)
                    {

                    }
                });

        subscriptions.add(subscription);
    }
}
