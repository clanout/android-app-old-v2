package reaper.android.app.service;

import android.util.Pair;

import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
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
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.request.FetchPendingInvitesApiRequest;
import reaper.android.app.api.event.request.FinaliseEventApiRequest;
import reaper.android.app.api.event.request.GetCreateEventSuggestionsApiRequest;
import reaper.android.app.api.event.request.InviteThroughSMSApiRequest;
import reaper.android.app.api.event.request.InviteUsersApiRequest;
import reaper.android.app.api.event.request.LocationSuggestionsApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.request.SendChatNotificationApiRequest;
import reaper.android.app.api.event.request.SendInvitaionResponseApiRequest;
import reaper.android.app.api.event.request.UpdateStatusApiRequest;
import reaper.android.app.api.event.response.CreateEventApiResponse;
import reaper.android.app.api.event.response.EditEventApiResponse;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.api.event.response.FetchPendingInvitesApiResponse;
import reaper.android.app.api.event.response.GetCreateEventSuggestionsApiResponse;
import reaper.android.app.api.event.response.LocationSuggestionsApiResponse;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.CreateEventSuggestion;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.model.User;
import reaper.android.app.model.util.EventComparator;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.LocationService_;
import reaper.android.common.analytics.AnalyticsHelper;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventService
{
    private static EventService instance;

    public static void init(GcmService_ gcmService, LocationService_ locationService,
                            UserService userService, NotificationService notificationService)
    {
        instance = new EventService(gcmService, locationService, userService, notificationService);
    }

    public static EventService getInstance()
    {
        if (instance == null) {
            throw new IllegalStateException("[EventService Not Initialized]");
        }

        return instance;
    }

    private LocationService_ locationService;
    private GcmService_ gcmService;
    private UserService userService;
    private NotificationService notificationService;

    private EventApi eventApi;
    private EventCache eventCache;

    private GenericCache genericCache;

    private EventService(GcmService_ gcmService, LocationService_ locationService,
                         UserService userService, NotificationService notificationService)
    {
        eventApi = ApiManager.getEventApi();
        eventCache = CacheManager.getEventCache();
        genericCache = CacheManager.getGenericCache();

        this.gcmService = gcmService;
        this.locationService = locationService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /* Events */
    public Observable<Event> _fetchEvent(final String eventId)
    {
        return _fetchEventCache(eventId)
                .flatMap(new Func1<Event, Observable<Event>>()
                {
                    @Override
                    public Observable<Event> call(Event event)
                    {
                        if (event != null) {
                            return Observable.just(event);
                        }
                        else {
                            return _fetchEventNetwork(eventId);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _fetchEventCache(final String eventId)
    {
        return eventCache
                .getEvent(eventId)
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _fetchEventNetwork(final String eventId)
    {
        return eventApi.fetchEvent(new FetchEventApiRequest(eventId))
                .map(new Func1<FetchEventApiResponse, Event>()
                {
                    @Override
                    public Event call(FetchEventApiResponse response)
                    {
                        return response.getEvent();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        eventCache.save(event);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEvents()
    {
        return _fetchEventsCache()
                .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                {
                    @Override
                    public Observable<List<Event>> call(List<Event> events)
                    {
                        if (events.isEmpty()) {
                            return _fetchEventsNetwork();
                        }
                        else {
                            return Observable.just(events);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEventsCache()
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
                .doOnNext(new Action1<List<Event>>()
                {
                    @Override
                    public void call(List<Event> events)
                    {
                        eventCache.reset(events);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEventsNetwork()
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
                        eventCache.reset(events);

                        for (Event event : events) {
                            handleTopicSubscription(event);
                        }
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
                        if (eventDetails != null) {
                            return Observable.just(eventDetails);
                        }
                        else {
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
                .flatMap(new Func1<EventDetails, Observable<EventDetails>>()
                {
                    @Override
                    public Observable<EventDetails> call(final EventDetails eventDetails)
                    {
                        return _fetchEvent(eventId)
                                .doOnNext(new Action1<Event>()
                                {
                                    @Override
                                    public void call(Event event)
                                    {
                                        List<String> friends = new ArrayList<>();
                                        int friendCount = 0;
                                        int inviterCount = 0;

                                        for (EventDetails.Attendee attendee : eventDetails
                                                .getAttendees()) {
                                            if (attendee.isFriend()) {
                                                friendCount++;

                                                try {
                                                    String name = attendee.getName();
                                                    String[] tokens = name.split(" ");
                                                    friends.add(tokens[0]);
                                                }
                                                catch (Exception e) {
                                                    /* Analytics */
                                                    AnalyticsHelper
                                                            .sendCaughtExceptions
                                                                    (GoogleAnalyticsConstants
                                                                            .METHOD_P, false);
                                                    /* Analytics */
                                                }
                                            }

                                            if (attendee.isInviter()) {
                                                inviterCount++;
                                            }
                                        }

                                        event.setInviterCount(inviterCount);
                                        event.setFriendCount(friendCount);
                                        event.setFriends(friends);

                                        eventCache.save(event);
                                    }
                                })
                                .flatMap(new Func1<Event, Observable<EventDetails>>()
                                {
                                    @Override
                                    public Observable<EventDetails> call(Event event)
                                    {
                                        return Observable.just(eventDetails);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Invite */
    public void _inviteAppFriends(String eventId, List<String> friendIds)
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
                        /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z9,
                                true);
                        /* Analytics */


                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200) {
                        }
                    }
                });
    }

    public void _invitePhonebookContacts(String eventId, List<String> mobileNumbers)
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
                          /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z10,
                                true);
                          /* Analytics */
                    }

                    @Override
                    public void onNext(Response response)
                    {

                    }
                });
    }

    /* Edit */
    public Observable<Integer> _editEvent(final String eventId, DateTime startTime, DateTime
            endTime, Location placeLocation, String description)
    {
        if (startTime == null && placeLocation.getZone() == null && description == null) {
            return Observable.just(0);
        }

        final EditEventApiRequest request = new EditEventApiRequest(placeLocation
                .getLongitude(), description, endTime, eventId, placeLocation
                .getLatitude(), placeLocation.getName(), placeLocation
                .getZone(), startTime);

        return eventApi.editEvent(request)
                .map(new Func1<EditEventApiResponse, Event>()
                {
                    @Override
                    public Event call(EditEventApiResponse response)
                    {
                        return response.getEvent();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        if (event != null) {
                            eventCache.save(event);
                            eventCache.deleteDetails(event.getId());
                        }
                    }
                })
                .map(new Func1<Event, Integer>()
                {
                    @Override
                    public Integer call(Event event)
                    {
                        if (event != null) {
                            return 0;
                        }
                        else {
                            return -1;
                        }
                    }
                })
                .onErrorReturn(new Func1<Throwable, Integer>()
                {
                    @Override
                    public Integer call(Throwable throwable)
                    {
                        try {
                            RetrofitError error = (RetrofitError) throwable;
                            if (error.getResponse().getStatus() == 400) {
                                return -2;
                            }
                            else {
                                return -1;
                            }
                        }
                        catch (Exception e) {
                                   /* Analytics */
                            AnalyticsHelper
                                    .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Q, false);
                                   /* Analytics */

                            return -1;
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Boolean> _finaliseEvent(final Event event, final boolean isFinalised)
    {
        return eventApi
                .finaliseEvent(new FinaliseEventApiRequest(event.getId(), isFinalised))
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSuccessful)
                    {
                        if (isSuccessful) {
                            event.setIsFinalized(isFinalised);
                            eventCache.save(event);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Boolean> _deleteEvent(final String eventId)
    {
        DeleteEventApiRequest request = new DeleteEventApiRequest(eventId);
        return eventApi
                .deleteEvent(request)
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSuccessful)
                    {
                        if (isSuccessful) {
                            eventCache.deleteCompletely(eventId);

                            if (genericCache.get(GenericCacheKeys.GCM_TOKEN) != null) {
                                gcmService.unsubscribeTopic(genericCache
                                        .get(GenericCacheKeys.GCM_TOKEN), eventId);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Chat Notification */
    public Observable<Boolean> _sendChatNotification(String eventId, String eventName, DateTime
            lastSentTimestamp)
    {
        return eventApi
                .sendChatNotification(new SendChatNotificationApiRequest(eventId, eventName,
                        lastSentTimestamp))
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void updateChatLastSeen(String eventId, DateTime timestamp)
    {
        eventCache.updateChatSeenTimestamp(eventId, timestamp);
    }

    /* Location Suggestions */
    public Observable<List<LocationSuggestion>> _fetchLocationSuggestions(EventCategory
                                                                                  eventCategory,
                                                                          double latitude, double
                                                                                  longitude)
    {
        LocationSuggestionsApiRequest request = new LocationSuggestionsApiRequest(eventCategory,
                String.valueOf(latitude),
                String.valueOf(longitude));
        return eventApi.getLocationSuggestions(request)
                .map(new Func1<LocationSuggestionsApiResponse, List<LocationSuggestion>>()
                {
                    @Override
                    public List<LocationSuggestion> call(LocationSuggestionsApiResponse
                                                                 locationSuggestionsApiResponse)
                    {
                        return locationSuggestionsApiResponse.getEventSuggestions();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Create Suggestions */
    public Observable<Boolean> _fetchCreateSuggestions()
    {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>()
                {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber)
                    {
                        boolean isSuggestionsAvailable = genericCache
                                .get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS) != null;
                        boolean isExpired = true;

                        try {
                            DateTime lastUpdated = genericCache
                                    .get(GenericCacheKeys
                                            .CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime
                                            .class);
                            isExpired = lastUpdated
                                    .plusDays(AppConstants.EXPIRY_DAYS_EVENT_SUGGESTIONS)
                                    .isBefore(DateTime.now());
                        }
                        catch (Exception e) {

                        }

                        if (isSuggestionsAvailable && !isExpired) {
                            subscriber.onNext(true);
                        }
                        else {
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
                        if (isAvailable) {
                            return Observable.just(true);
                        }
                        else {
                            return eventApi
                                    .getCreateEventSuggestion(new
                                            GetCreateEventSuggestionsApiRequest())
                                    .onErrorReturn(new Func1<Throwable,
                                            GetCreateEventSuggestionsApiResponse>()
                                    {
                                        @Override
                                        public GetCreateEventSuggestionsApiResponse call
                                                (Throwable throwable)
                                        {
                                            return null;
                                        }
                                    })
                                    .map(new Func1<GetCreateEventSuggestionsApiResponse, Boolean>()
                                    {
                                        @Override
                                        public Boolean call(GetCreateEventSuggestionsApiResponse
                                                                    response)
                                        {
                                            if (response == null) {
                                                return false;
                                            }
                                            else {
                                                genericCache
                                                        .put(GenericCacheKeys
                                                                .CREATE_EVENT_SUGGESTIONS, response
                                                                .getEventSuggestions());
                                                genericCache
                                                        .put(GenericCacheKeys
                                                                .CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime
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

    public Observable<List<CreateEventSuggestion>> _getCreateSuggestions()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<CreateEventSuggestion>>()
                {
                    @Override
                    public void call(Subscriber<? super List<CreateEventSuggestion>> subscriber)
                    {
                        String createEventSuggestionStr = genericCache
                                .get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS);
                        Type type = new TypeToken<List<CreateEventSuggestion>>()
                        {
                        }.getType();

                        List<CreateEventSuggestion> createEventSuggestions = GsonProvider.getGson()
                                .fromJson(createEventSuggestionStr, type);
                        subscriber.onNext(createEventSuggestions);
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<List<CreateEventSuggestion>, List<CreateEventSuggestion>>()
                {
                    @Override
                    public List<CreateEventSuggestion> call(List<CreateEventSuggestion> suggestions)
                    {
                        Collections.shuffle(suggestions);
                        return suggestions;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Rsvp */
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
                        if (isSuccess) {
                            eventCache.deleteCompletely(updatedEvent.getId());
                            eventCache.save(updatedEvent);
                            handleTopicSubscription(updatedEvent);

                            if (updatedEvent.getRsvp() == Event.RSVP.YES) {
                                notificationService
                                        .deleteInvitationNotification(updatedEvent.getId());
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Status */
    public void updateStatus(final Event updatedEvent, boolean shouldNotifyOthers)
    {

        UpdateStatusApiRequest request = new UpdateStatusApiRequest(updatedEvent.getId(),
                updatedEvent.getStatus(),
                shouldNotifyOthers);

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
                eventCache.deleteCompletely(updatedEvent.getId());
                eventCache.save(updatedEvent);
            }
        });
    }

    /* Create */
    public Observable<Event> _create(String title, Event.Type eventType, EventCategory
            eventCategory,
                                     String description, Location placeLocation, DateTime
                                             startTime, DateTime endTime)
    {
        CreateEventApiRequest request = new CreateEventApiRequest(title, eventType,
                eventCategory, description, placeLocation
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
                        handleTopicSubscription(event);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Pending Invites */
    public Observable<Pair<Integer, List<Event>>> _fetchPendingInvites(final String mobileNumber)
    {
        String zone = locationService.getCurrentLocation().getZone();
        FetchPendingInvitesApiRequest request = new FetchPendingInvitesApiRequest(mobileNumber,
                zone);
        return eventApi
                .fetchPendingInvites(request)
                .doOnNext(new Action1<FetchPendingInvitesApiResponse>()
                {
                    @Override
                    public void call(FetchPendingInvitesApiResponse response)
                    {
                        User sessionUser = userService.getSessionUser();
                        sessionUser.setMobileNumber(mobileNumber);
                        userService.setSessionUser(sessionUser);
                    }
                })
                .flatMap(new Func1<FetchPendingInvitesApiResponse, Observable<Pair<Integer,
                        List<Event>>>>()
                {
                    @Override
                    public Observable<Pair<Integer, List<Event>>> call(final
                                                                       FetchPendingInvitesApiResponse response)
                    {
                        return _fetchEventsCache()
                                .flatMap(new Func1<List<Event>, Observable<Pair<Integer,
                                        List<Event>>>>()
                                {
                                    @Override
                                    public Observable<Pair<Integer, List<Event>>> call
                                            (List<Event> cachedEvents)
                                    {
                                        List<Event> pendingInvites = response.getActiveEvents();
                                        int expiredInvites = response.getTotalCount() -
                                                pendingInvites.size();

                                        for (Event pendingInvite : pendingInvites) {
                                            if (!cachedEvents.contains(pendingInvite)) {
                                                cachedEvents.add(pendingInvite);
                                            }
                                        }

                                        eventCache.reset(cachedEvents);

                                        return Observable
                                                .just(new Pair<>(expiredInvites, pendingInvites));
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Invitation Response */
    public void sendInvitationResponse(String eventId, String message)
    {
        SendInvitaionResponseApiRequest request = new SendInvitaionResponseApiRequest(eventId,
                message);
        eventApi
                .sendInvitationResponse(request)
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
        for (Event event : events) {
            if (!isExpired(event)) {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }

    private void handleTopicSubscription(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES) {
            gcmService.subscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), event
                    .getId());
        }
        else {
            gcmService
                    .unsubscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), event
                            .getId());
        }
    }
}
