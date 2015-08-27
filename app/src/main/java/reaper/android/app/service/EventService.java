package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.CreateEventApiRequest;
import reaper.android.app.api.event.request.DeleteEventApiRequest;
import reaper.android.app.api.event.request.EditEventApiRequest;
import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventSuggestionsApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.request.FetchNewEventsAndUpdatesApiRequest;
import reaper.android.app.api.event.request.InviteUsersApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.response.CreateEventApiResponse;
import reaper.android.app.api.event.response.EditEventApiResponse;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventSuggestionsApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.api.event.response.FetchNewEventsAndUpdatesApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventAttendeeComparator;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.root.Reaper;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventEditedTrigger;
import reaper.android.app.trigger.event.EventIdsFetchedTrigger;
import reaper.android.app.trigger.event.EventSuggestionsTrigger;
import reaper.android.app.trigger.event.EventsFetchForActivityTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.event.NewEventsAndUpdatesFetchedTrigger;
import retrofit.client.Response;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EventService
{
    private static final String TAG = "EventService";

    private Bus bus;
    private UserService userService;
    private GCMService gcmService;
    private EventApi eventApi;
    private EventCache eventCache;
    private GenericCache genericCache;

    public EventService(Bus bus)
    {
        this.bus = bus;
        userService = new UserService(bus);
        gcmService = new GCMService(bus);
        eventApi = ApiManager.getInstance().getApi(EventApi.class);
        eventCache = CacheManager.getEventCache();
        genericCache = CacheManager.getGenericCache();
    }

    public void fetchEvents(final String zone)
    {
        final long startTime = System.currentTimeMillis();
        Observable<List<Event>> eventObservable =
                eventCache.getEvents()
                        .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                        {
                            @Override
                            public Observable<List<Event>> call(List<Event> events)
                            {
                                if (events.isEmpty())
                                {
                                    EventsApiRequest request = new EventsApiRequest(zone);
                                    return eventApi.getEvents(request)
                                            .map(new Func1<EventsApiResponse, List<Event>>()
                                            {
                                                @Override
                                                public List<Event> call(EventsApiResponse eventsApiResponse)
                                                {
                                                    return eventsApiResponse.getEvents();
                                                }
                                            })
                                            .doOnNext(new Action1<List<Event>>()
                                            {
                                                @Override
                                                public void call(List<Event> events)
                                                {
                                                    genericCache.put(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now().toString());
                                                    eventCache.reset(events);
                                                }
                                            })
                                            .subscribeOn(Schedulers.newThread());
                                } else
                                {
                                    return Observable.just(events);
                                }
                            }
                        });

        eventObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        long endTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENTS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        bus.post(new EventsFetchTrigger(events));
                    }
                });
    }

    public void fetchEventsForActivity(final String zone)
    {
        final long startTime = System.currentTimeMillis();
        Observable<List<Event>> eventObservable =
                eventCache.getEvents()
                        .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                        {
                            @Override
                            public Observable<List<Event>> call(List<Event> events)
                            {
                                if (events.isEmpty())
                                {
                                    EventsApiRequest request = new EventsApiRequest(zone);
                                    return eventApi.getEvents(request)
                                            .map(new Func1<EventsApiResponse, List<Event>>()
                                            {
                                                @Override
                                                public List<Event> call(EventsApiResponse eventsApiResponse)
                                                {
                                                    return eventsApiResponse.getEvents();
                                                }

                                            })
                                            .doOnNext(new Action1<List<Event>>()
                                            {
                                                @Override
                                                public void call(List<Event> events)
                                                {
                                                    genericCache.put(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now().toString());
                                                    eventCache.reset(events);
                                                }
                                            })
                                            .subscribeOn(Schedulers.newThread());
                                } else
                                {
                                    return Observable.just(events);
                                }
                            }
                        });

        eventObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        long endTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENTS_FETCH_FOR_ACTIVITY_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        bus.post(new EventsFetchForActivityTrigger(events));
                    }
                });
    }


    public void fetchEvent(String eventId, final boolean shouldMarkUpdated)
    {
        FetchEventApiRequest request = new FetchEventApiRequest(eventId);
        eventApi.fetchEvent(request)
                .map(new Func1<FetchEventApiResponse, Event>()
                {
                    @Override
                    public Event call(FetchEventApiResponse fetchEventApiResponse)
                    {
                        return fetchEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.d("APP", "Exception while fetching event ------ " + e.getMessage());
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);

                        if (shouldMarkUpdated)
                        {
                            List<String> updatedEvents = Arrays.asList(event.getId());
                            eventCache.markUpdated(updatedEvents);
                        }
                    }
                });
    }

    public void fetchNewEventsAndUpdatesFromNetwork(String zone, final List<String> eventIdList, String lastUpdateTimestamp)
    {
        Observable<FetchNewEventsAndUpdatesApiResponse> updatesObservable = eventApi.fetchNewEventsAndUpdates(new FetchNewEventsAndUpdatesApiRequest(zone, eventIdList, lastUpdateTimestamp));
        Observable<List<Event>> cachedEventsObservable = eventCache.getEvents();

        Observable<List<Event>> updatedObservable =
                Observable.zip(updatesObservable, cachedEventsObservable, new Func2<FetchNewEventsAndUpdatesApiResponse, List<Event>, List<Event>>()
                {
                    @Override
                    public List<Event> call(FetchNewEventsAndUpdatesApiResponse response, List<Event> events)
                    {
                        List<Event> newEventList = response.getNewEventsList();
                        List<String> deletedEventIdList = response.getDeletedEventIdList();
                        List<Event> updatedEventList = response.getUpdatedEventList();

                        for (String deletedEventId : deletedEventIdList)
                        {
                            Event deletedEvent = new Event();
                            deletedEvent.setId(deletedEventId);

                            if (events.contains(deletedEvent))
                            {
                                events.remove(deletedEvent);
                            }
                        }

                        for (Event updatedEvent : updatedEventList)
                        {
                            if (events.contains(updatedEvent))
                            {
                                int index = events.indexOf(updatedEvent);
                                if (index >= 0)
                                {
                                    if (!updatedEvent.isEqualTo(events.get(index)))
                                    {
                                        events.set(index, updatedEvent);
                                    } else
                                    {
                                    }
                                }
                            }
                        }

                        events.addAll(newEventList);

                        return events;
                    }
                });

        updatedObservable
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
                        bus.post(new GenericErrorTrigger(ErrorCode.NEW_EVENTS_AND_UPDATES_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        genericCache.put(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now().toString());
                        eventCache.reset(events);
                        bus.post(new NewEventsAndUpdatesFetchedTrigger(events));
                    }
                });
    }

    public void getAllEventIds()
    {
        eventCache.getEvents()
                .map(new Func1<List<Event>, List<String>>()
                {
                    @Override
                    public List<String> call(List<Event> events)
                    {
                        List<String> eventIds = new ArrayList<String>(events.size());
                        for (Event event : events)
                        {
                            Log.d("APP", "getting event ids on thread ---- " + Thread.currentThread().getName());
                            eventIds.add(event.getId());
                        }
                        return eventIds;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<String>>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.d("APP", "on error while getting event ids" + e.getMessage());
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENT_IDS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<String> strings)
                    {
                        Log.d("APP", "on Next event ids fetched ---- " + strings);
                        bus.post(new EventIdsFetchedTrigger(strings));
                    }
                });
    }

    public void fetchEventDetails(final String eventId)
    {
        final long startTime = System.currentTimeMillis();
        Observable<EventDetails> eventDetailsObservable =
                eventCache.getEventDetails(eventId)
                        .flatMap(new Func1<EventDetails, Observable<EventDetails>>()
                        {
                            @Override
                            public Observable<EventDetails> call(EventDetails eventDetails)
                            {
                                if (eventDetails == null)
                                {
                                    Timber.d("EventDetails null for " + eventId);
                                    EventDetailsApiRequest request = new EventDetailsApiRequest(eventId);
                                    return eventApi.getEventDetails(request)
                                            .map(new Func1<EventDetailsApiResponse, EventDetails>()
                                            {
                                                @Override
                                                public EventDetails call(EventDetailsApiResponse eventDetailsApiResponse)
                                                {
                                                    return eventDetailsApiResponse
                                                            .getEventDetails();
                                                }
                                            })
                                            .doOnNext(new Action1<EventDetails>()
                                            {
                                                @Override
                                                public void call(EventDetails eventDetails)
                                                {
                                                    eventCache.saveDetails(eventDetails);
                                                }
                                            })
                                            .observeOn(Schedulers.newThread());
                                } else
                                {
                                    Timber.d("EventDetails in cache for " + eventId);
                                    return Observable.just(eventDetails);
                                }
                            }
                        });

        eventDetailsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<EventDetails>()
                {
                    @Override
                    public void onCompleted()
                    {
                        long endTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENT_DETAILS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(EventDetails eventDetails)
                    {
                        Collections.sort(eventDetails
                                .getAttendees(), new EventAttendeeComparator(userService
                                .getActiveUserId()));
                        bus.post(new EventDetailsFetchTrigger(eventDetails));
                    }
                });
    }

    public void updateRsvp(final Event updatedEvent, final Event.RSVP oldRsvp)
    {
        RsvpUpdateApiRequest request = new RsvpUpdateApiRequest(updatedEvent.getId(), updatedEvent
                .getRsvp());

        eventApi.updateRsvp(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        updatedEvent.setRsvp(oldRsvp);
                        handleTopicSubscription(updatedEvent);
                        bus.post(new GenericErrorTrigger(ErrorCode.RSVP_UPDATE_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            eventCache.save(updatedEvent);
                            handleTopicSubscription(updatedEvent);
                        } else
                        {
                            updatedEvent.setRsvp(oldRsvp);
                            handleTopicSubscription(updatedEvent);
                            bus.post(new GenericErrorTrigger(ErrorCode.RSVP_UPDATE_FAILURE, null));
                        }
                    }
                });
    }

    private void handleTopicSubscription(Event updatedEvent)
    {
        if (updatedEvent.getRsvp() == Event.RSVP.NO)
        {
            gcmService.unsubscribeTopic(genericCache.get(CacheKeys.GCM_TOKEN), updatedEvent.getId());
        } else
        {
            gcmService.subscribeTopic(genericCache.get(CacheKeys.GCM_TOKEN), updatedEvent.getId());
        }
    }

    public void fetchEventSuggestions(EventCategory eventCategory, String latitude, String longitude)
    {
        EventSuggestionsApiRequest request = new EventSuggestionsApiRequest(eventCategory, latitude, longitude);
        eventApi.getEventSuggestions(request)
                .map(new Func1<EventSuggestionsApiResponse, List<Suggestion>>()
                {
                    @Override
                    public List<Suggestion> call(EventSuggestionsApiResponse eventSuggestionsApiResponse)
                    {
                        return eventSuggestionsApiResponse.getEventSuggestions();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Suggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new EventSuggestionsTrigger(new ArrayList<Suggestion>()));
                    }

                    @Override
                    public void onNext(List<Suggestion> suggestions)
                    {
                        bus.post(new EventSuggestionsTrigger(suggestions));
                    }
                });
    }

    public void createEvent(String title, Event.Type eventType, EventCategory eventCategory, String description, Location placeLocation, DateTime startTime, DateTime endTime)
    {
        CreateEventApiRequest request = new CreateEventApiRequest(title, eventType, eventCategory, description, placeLocation
                .getName(), placeLocation.getZone(), String
                .valueOf(placeLocation.getLatitude()), String
                .valueOf(placeLocation.getLongitude()), startTime, endTime);

        eventApi.createEvent(request)
                .map(new Func1<CreateEventApiResponse, Event>()
                {
                    @Override
                    public Event call(CreateEventApiResponse createEventApiResponse)
                    {
                        return createEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENT_CREATION_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);

                        if (genericCache.get(CacheKeys.GCM_TOKEN) != null)
                        {
                            gcmService.subscribeTopic(genericCache.get(CacheKeys.GCM_TOKEN), event.getId());
                        }

                        bus.post(new EventCreatedTrigger(event));
                    }
                });
    }

    public void editEvent(String eventId, boolean isFinalised, DateTime startTime, DateTime endTime, Location placeLocation, String description)
    {
        EditEventApiRequest request = new EditEventApiRequest(String.valueOf(placeLocation
                .getLongitude()), description, endTime, eventId, isFinalised, String
                .valueOf(placeLocation.getLatitude()), placeLocation.getName(), placeLocation
                .getZone(), startTime);

        eventApi.editEvent(request)
                .map(new Func1<EditEventApiResponse, Event>()
                {
                    @Override
                    public Event call(EditEventApiResponse editEventApiResponse)
                    {
                        return editEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.EVENT_EDIT_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                        bus.post(new EventEditedTrigger(event));
                    }
                });
    }

    public void deleteEvent(final String eventId)
    {
        DeleteEventApiRequest request = new DeleteEventApiRequest(eventId);

        eventApi.deleteEvent(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            eventCache.deleteCompletely(eventId);

                            if (genericCache.get(CacheKeys.GCM_TOKEN) != null)
                            {
                                gcmService.unsubscribeTopic(genericCache.get(CacheKeys.GCM_TOKEN), eventId);
                            }
                        }
                    }
                });
    }

    public void inviteUsers(String eventId, List<String> userIdList)
    {
        InviteUsersApiRequest request = new InviteUsersApiRequest(eventId, userIdList);

        eventApi.inviteFriends(request)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            // TODO: Delete event details or add a new invitee
                        }
                    }
                });
    }

}
