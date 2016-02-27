package reaper.android.app.service;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.api._core.ApiManager;
import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.CreateEventApiRequest;
import reaper.android.app.api.event.request.DeleteEventApiRequest;
import reaper.android.app.api.event.request.EditEventApiRequest;
import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventSuggestionsApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.FetchNewEventsAndUpdatesApiRequest;
import reaper.android.app.api.event.request.FinaliseEventApiRequest;
import reaper.android.app.api.event.request.GetEventSuggestionsApiRequest;
import reaper.android.app.api.event.request.InviteThroughSMSApiRequest;
import reaper.android.app.api.event.request.InviteUsersApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.request.SendInvitaionResponseApiRequest;
import reaper.android.app.api.event.request.UpdateStatusApiRequest;
import reaper.android.app.api.event.response.CreateEventApiResponse;
import reaper.android.app.api.event.response.EditEventApiResponse;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventSuggestionsApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.api.event.response.FetchNewEventsAndUpdatesApiResponse;
import reaper.android.app.api.event.response.GetEventSuggestionApiResponse;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.ExceptionMessages;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.model.util.EventComparator;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.common.communicator.Communicator;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class EventService
{
    private static EventService instance;

    public static EventService getInstance()
    {
        if (instance == null)
        {
            instance = new EventService();
        }

        return instance;
    }

    private static final String TAG = "EventService";

    private Bus bus;
    private UserService userService;
    private LocationService_ locationService;
    private GcmService_ gcmService;
    private EventApi eventApi;
    private EventCache eventCache;
    private GenericCache genericCache;

    private EventService()
    {
        this.bus = Communicator.getInstance().getBus();
        userService = UserService.getInstance();
        gcmService = GcmService_.getInstance();
        eventApi = ApiManager.getEventApi();
        eventCache = CacheManager.getEventCache();
        genericCache = CacheManager.getGenericCache();
        locationService = LocationService_.getInstance();
    }

    /* Events */
    public Observable<List<Event>> _fetchEvents()
    {
        return _fetchEventsCache()
                .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                {
                    @Override
                    public Observable<List<Event>> call(List<Event> events)
                    {
                        if (events.isEmpty())
                        {
                            return _fetchEventsNetwork();
                        }
                        else
                        {
                            return Observable.just(events);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<List<Event>> _fetchEventsCache()
    {
        return eventCache
                .getEvents()
                .map(new Func1<List<Event>, List<Event>>()
                {
                    @Override
                    public List<Event> call(List<Event> events)
                    {
                        List<Event> filtered = filterExpiredEvents(events);
                        Collections.sort(filtered, new EventComparator.Relevance(userService
                                .getSessionUserId()));
                        return filtered;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<List<Event>> _fetchEventsNetwork()
    {
        String zone = locationService.getCurrentLocation().getZone();
        EventsApiRequest request = new EventsApiRequest(zone);
        return eventApi
                .getEvents(request)
                .map(new Func1<EventsApiResponse, List<Event>>()
                {
                    @Override
                    public List<Event> call(EventsApiResponse response)
                    {
                        List<Event> filtered = filterExpiredEvents(response.getEvents());
                        Collections.sort(filtered, new EventComparator.Relevance(userService
                                .getSessionUserId()));
                        return filtered;
                    }
                })
                .doOnNext(new Action1<List<Event>>()
                {
                    @Override
                    public void call(List<Event> events)
                    {
                        genericCache.put(GenericCacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now());
                        eventCache.reset(events);
                    }
                })
                .subscribeOn(Schedulers.newThread());

    }

    /* Event Details */
    public Observable<EventDetails> _fetchDetails(final String eventId)
    {
        return _fetchDetailsCache(eventId)
                .flatMap(new Func1<EventDetails, Observable<EventDetails>>()
                {
                    @Override
                    public Observable<EventDetails> call(EventDetails eventDetails)
                    {
                        if (eventDetails != null)
                        {
                            return Observable.just(eventDetails);
                        }
                        else
                        {
                            return _fetchDetailsNetwork(eventId);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<EventDetails> _fetchDetailsCache(final String eventId)
    {
        return eventCache
                .getEventDetails(eventId)
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<EventDetails> _fetchDetailsNetwork(final String eventId)
    {
        EventDetailsApiRequest eventDetailsApiRequest = new EventDetailsApiRequest(eventId);

        return eventApi
                .getEventDetails(eventDetailsApiRequest)
                .map(new Func1<EventDetailsApiResponse, EventDetails>()
                {
                    @Override
                    public EventDetails call(EventDetailsApiResponse eventDetailsApiResponse)
                    {
                        return eventDetailsApiResponse.getEventDetails();
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
                .subscribeOn(Schedulers.newThread());
    }

    /* Invite */
    public void inviteAppFriends(String eventId, List<String> friendIds)
    {
        InviteUsersApiRequest request = new InviteUsersApiRequest(eventId, friendIds);

        eventApi.inviteFriends(request)
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
                        }
                    }
                });
    }

    public void invitePhonebookContacts(String eventId, List<String> mobileNumbers)
    {
        InviteThroughSMSApiRequest request = new InviteThroughSMSApiRequest(eventId, mobileNumbers);

        eventApi.inviteThroughSMS(request)
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

                    }
                });
    }

    /* Helper Methods */
    private boolean isExpired(Event event)
    {
        return event.getEndTime().isBeforeNow();
    }

    private List<Event> filterExpiredEvents(List<Event> events)
    {
        List<Event> filteredEvents = new ArrayList<Event>();
        for (Event event : events)
        {
            if (!isExpired(event))
            {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }




    /* Old */
    public Observable<Boolean> updateEventSuggestions()
    {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>()
                {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber)
                    {
                        boolean isSuggestionsAvailable = genericCache
                                .get(GenericCacheKeys.EVENT_SUGGESTIONS) != null;

                        DateTime lastUpdated = genericCache
                                .get(GenericCacheKeys.EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime.class);
                        boolean isExpired = lastUpdated
                                .plusDays(AppConstants.EXPIRY_DAYS_EVENT_SUGGESTIONS)
                                .isBefore(DateTime.now());

                        if (isSuggestionsAvailable && !isExpired)
                        {
                            subscriber.onNext(true);
                        }
                        else
                        {
                            subscriber.onNext(false);
                        }
                        subscriber.onCompleted();
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isAvailable)
                    {
                        if (isAvailable)
                        {
                            return Observable.just(true);
                        }
                        else
                        {
                            return eventApi
                                    .getEventSuggestions(new GetEventSuggestionsApiRequest())
                                    .onErrorReturn(new Func1<Throwable, GetEventSuggestionApiResponse>()
                                    {
                                        @Override
                                        public GetEventSuggestionApiResponse call(Throwable throwable)
                                        {
                                            return null;
                                        }
                                    })
                                    .map(new Func1<GetEventSuggestionApiResponse, Boolean>()
                                    {
                                        @Override
                                        public Boolean call(GetEventSuggestionApiResponse response)
                                        {
                                            if (response == null)
                                            {
                                                return false;
                                            }
                                            else
                                            {
                                                genericCache
                                                        .put(GenericCacheKeys.EVENT_SUGGESTIONS, response
                                                                .getEventSuggestions());
                                                genericCache
                                                        .put(GenericCacheKeys.EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime
                                                                .now());
                                                return true;
                                            }
                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
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
                                                             List<Event> filteredEvents = new ArrayList<Event>();
                                                             for (Event event : eventsApiResponse
                                                                     .getEvents())
                                                             {
                                                                 if (event.getEndTime()
                                                                          .isAfterNow())
                                                                 {
                                                                     filteredEvents.add(event);
                                                                 }
                                                             }

                                                             return filteredEvents;
                                                         }
                                                     })
                                                     .doOnNext(new Action1<List<Event>>()
                                                     {
                                                         @Override
                                                         public void call(List<Event> events)
                                                         {
                                                             genericCache
                                                                     .put(GenericCacheKeys.LAST_UPDATE_TIMESTAMP, DateTime
                                                                             .now());
                                                             eventCache.reset(events);
                                                         }
                                                     })
                                                     .subscribeOn(Schedulers.newThread());
                                  }
                                  else
                                  {
                                      List<Event> filteredEvents = new ArrayList<Event>();
                                      for (Event event : events)
                                      {
                                          if (event.getEndTime().isAfterNow())
                                          {
                                              filteredEvents.add(event);
                                          }
                                      }
                                      return Observable.just(filteredEvents);
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

    private void handleTopicSubscription(Event updatedEvent)
    {
        if (updatedEvent.getRsvp() == Event.RSVP.NO)
        {
            gcmService
                    .unsubscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), updatedEvent
                            .getId());
        }
        else
        {
            gcmService.subscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), updatedEvent
                    .getId());
        }
    }

    public Observable<List<Event>> _refreshEvents(String zone, final List<String> eventIdList, DateTime lastUpdateTimestamp)
    {

        Observable<FetchNewEventsAndUpdatesApiResponse> updatesObservable = eventApi
                .fetchNewEventsAndUpdates(new FetchNewEventsAndUpdatesApiRequest(zone, eventIdList, lastUpdateTimestamp));
        Observable<List<Event>> cachedEventsObservable = eventCache.getEvents();

        return Observable
                .zip(updatesObservable, cachedEventsObservable, new Func2<FetchNewEventsAndUpdatesApiResponse, List<Event>, List<Event>>()
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
                                    }
                                    else
                                    {
                                    }
                                }
                            }
                        }

                        events.addAll(newEventList);

                        List<Event> filteredEvents = new ArrayList<Event>();
                        for (Event event : events)
                        {
                            if (event.getEndTime().isAfterNow())
                            {
                                filteredEvents.add(event);
                            }
                        }

                        return filteredEvents;
                    }
                })
                .doOnNext(new Action1<List<Event>>()
                {
                    @Override
                    public void call(List<Event> events)
                    {
                        genericCache.put(GenericCacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now());
                        eventCache.reset(events);
                    }
                })
                .map(new Func1<List<Event>, List<Event>>()
                {
                    @Override
                    public List<Event> call(List<Event> events)
                    {
                        Collections.sort(events, new EventComparator.Relevance(userService
                                .getSessionUserId()));
                        return events;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Boolean> _updateRsvp(final Event updatedEvent)
    {
        RsvpUpdateApiRequest request = new RsvpUpdateApiRequest(updatedEvent.getId(), updatedEvent
                .getRsvp());

        return eventApi.updateRsvp(request)
                       .map(new Func1<Response, Boolean>()
                       {
                           @Override
                           public Boolean call(Response response)
                           {
                               return (response.getStatus() == 200);
                           }
                       })
                       .doOnNext(new Action1<Boolean>()
                       {
                           @Override
                           public void call(Boolean isSuccess)
                           {
                               if (isSuccess)
                               {
                                   eventCache.deleteCompletely(updatedEvent.getId());
                                   eventCache.save(updatedEvent);
                                   handleTopicSubscription(updatedEvent);
                               }
                           }
                       })
                       .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<LocationSuggestion>> _fetchSuggestions(EventCategory eventCategory, double latitude, double longitude)
    {
        EventSuggestionsApiRequest request = new EventSuggestionsApiRequest(eventCategory,
                String.valueOf(latitude),
                String.valueOf(longitude));
        return eventApi.getEventSuggestions(request)
                       .map(new Func1<EventSuggestionsApiResponse, List<LocationSuggestion>>()
                       {
                           @Override
                           public List<LocationSuggestion> call(EventSuggestionsApiResponse eventSuggestionsApiResponse)
                           {
                               return eventSuggestionsApiResponse.getEventSuggestions();
                           }
                       })
                       .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _create(String title, Event.Type eventType, EventCategory eventCategory, String description, Location placeLocation, DateTime startTime, DateTime endTime)
    {
        CreateEventApiRequest request = new CreateEventApiRequest(title, eventType, eventCategory, description, placeLocation
                .getName(), placeLocation.getZone(), placeLocation.getLatitude(), placeLocation
                .getLongitude(), startTime, endTime);

        return eventApi
                .createEvent(request)
                .map(new Func1<CreateEventApiResponse, Event>()
                {
                    @Override
                    public Event call(CreateEventApiResponse createEventApiResponse)
                    {
                        return createEventApiResponse.getEvent();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        eventCache.save(event);

                        if (genericCache.get(GenericCacheKeys.GCM_TOKEN) != null)
                        {
                            gcmService.subscribeTopic(genericCache
                                    .get(GenericCacheKeys.GCM_TOKEN), event
                                    .getId());
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Response> _finaliseEvent(final Event event, final boolean isFinalised)
    {
        return eventApi
                .finaliseEvent(new FinaliseEventApiRequest(event.getId(), isFinalised))
                .doOnNext(new Action1<Response>()
                {
                    @Override
                    public void call(Response response)
                    {
                        event.setIsFinalized(isFinalised);
                        eventCache.save(event);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Response> _deleteEvent(final String eventId)
    {
        DeleteEventApiRequest request = new DeleteEventApiRequest(eventId);

        return eventApi
                .deleteEvent(request)
                .doOnNext(new Action1<Response>()
                {
                    @Override
                    public void call(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            eventCache.deleteCompletely(eventId);

                            if (genericCache.get(GenericCacheKeys.GCM_TOKEN) != null)
                            {
                                gcmService.unsubscribeTopic(genericCache
                                        .get(GenericCacheKeys.GCM_TOKEN), eventId);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _editEvent(final String eventId, DateTime startTime, DateTime endTime,
                                        Location placeLocation, String description)
    {
        final EditEventApiRequest request = new EditEventApiRequest(placeLocation
                .getLongitude(), description, endTime, eventId, placeLocation
                .getLatitude(), placeLocation.getName(), placeLocation
                .getZone(), startTime);

        return Observable
                .create(new Observable.OnSubscribe<Event>()
                {
                    @Override
                    public void call(Subscriber<? super Event> subscriber)
                    {
                        try
                        {
                            Response response = eventApi.editEvent(request);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response
                                    .getBody().in()));

                            String line;
                            StringBuilder jsonBuilder = new StringBuilder();

                            while ((line = bufferedReader.readLine()) != null)
                            {
                                jsonBuilder.append(line).append("\n");
                            }

                            bufferedReader.close();
                            String json = jsonBuilder.toString();
                            Event event = GsonProvider.getGson()
                                                      .fromJson(json, EditEventApiResponse.class)
                                                      .getEvent();

                            subscriber.onNext(event);

                        }
                        catch (RetrofitError e)
                        {
                            if (e.getResponse().getStatus() == 400)
                            {
                                subscriber.onError(new Throwable(ExceptionMessages.EVENT_LOCKED));
                            }
                            else
                            {
                                subscriber.onError(new Throwable(ExceptionMessages.BAD_REQUEST));
                            }

                        }
                        catch (IOException e)
                        {
                            subscriber.onError(new Throwable(ExceptionMessages.BAD_REQUEST));
                        }
                        subscriber.onCompleted();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        eventCache.save(event);
                        eventCache.deleteDetails(event.getId());
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void sendInvitationResponse(String eventId, String message)
    {

        SendInvitaionResponseApiRequest responseApiRequest = new SendInvitaionResponseApiRequest(eventId, message);

        eventApi.sendInvitationResponse(responseApiRequest).subscribeOn(Schedulers.newThread())
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

                    }
                });
    }

    public void updateStatus(String eventId, String status, boolean shouldNotifyOthers)
    {

        UpdateStatusApiRequest request = new UpdateStatusApiRequest(eventId, status, shouldNotifyOthers);

        eventApi.updateStatus(request).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Response>()
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

            }
        });
    }

    public void getEventSuggestions()
    {

        eventApi.getEventSuggestions(new GetEventSuggestionsApiRequest())
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GetEventSuggestionApiResponse>()
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
                    public void onNext(GetEventSuggestionApiResponse getEventSuggestionApiResponse)
                    {

                        genericCache
                                .put(GenericCacheKeys.EVENT_SUGGESTIONS, getEventSuggestionApiResponse
                                        .getEventSuggestions());
                    }
                });
    }
}
