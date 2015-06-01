package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventUpdatesApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventUpdatesApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventAttendeeComparator;
import reaper.android.app.model.EventDetails;
import reaper.android.app.trigger.CacheCommitTrigger;
import reaper.android.app.trigger.EventDetailsFetchTrigger;
import reaper.android.app.trigger.EventUpdatesFetchTrigger;
import reaper.android.app.trigger.EventsFetchTrigger;
import reaper.android.app.trigger.GenericErrorTrigger;
import reaper.android.common.cache.Cache;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EventService
{
    private Bus bus;
    private UserService userService;
    private EventApi eventApi;

    public EventService(Bus bus)
    {
        this.bus = bus;
        userService = new UserService(bus);
        eventApi = ApiManager.getInstance().getApi(EventApi.class);
    }

    public void fetchEvents(String zone)
    {
        Cache cache = Cache.getInstance();
        Map<String, Event> eventMap = (Map<String, Event>) cache.get(CacheKeys.EVENTS);
        DateTime lastUpdated = (DateTime) cache.get(CacheKeys.EVENTS_TIMESTAMP);
        if (eventMap != null && lastUpdated != null)
        {
            //Log.d("reap3r", "Events from cache (Last Updated : " + lastUpdated.toString() + ")");
            List<Event> events = new ArrayList<>(eventMap.values());
            bus.post(new EventsFetchTrigger(events));
        }
        else
        {
            EventsApiRequest request = new EventsApiRequest(zone);
            eventApi.getEvents(request, new Callback<EventsApiResponse>()
            {
                @Override
                public void success(EventsApiResponse eventsApiResponse, Response response)
                {
                    List<Event> events = updateEventsCache(eventsApiResponse.getEvents(), false);
                    bus.post(new EventsFetchTrigger(events));
                }

                @Override
                public void failure(RetrofitError retrofitError)
                {
                    bus.post(new GenericErrorTrigger(ErrorCode.EVENTS_FETCH_FAILURE, retrofitError));
                }
            });
        }
    }

    public void fetchEventDetails(String eventId)
    {
        Cache cache = Cache.getInstance();
        EventDetails eventDetails = (EventDetails) cache.get(CacheKeys.eventDetails(eventId));
        List<String> updatedEvents = getUpdatedEvents();

        if (eventDetails != null && !updatedEvents.contains(eventId))
        {
            //Log.d("reap3r", "EventDetails (event_id = " + eventId + ") from cache");
            bus.post(new EventDetailsFetchTrigger(eventDetails));
        }
        else
        {
            EventDetailsApiRequest request = new EventDetailsApiRequest(eventId);
            eventApi.getEventDetails(request, new Callback<EventDetailsApiResponse>()
            {
                @Override
                public void success(EventDetailsApiResponse eventDetailsApiResponse, Response response)
                {
                    EventDetails eventDetails = eventDetailsApiResponse.getEventDetails();
                    Collections.sort(eventDetails.getAttendees(), new EventAttendeeComparator(userService.getActiveUser()));
                    updateCacheFor(eventDetails);
                    bus.post(new EventDetailsFetchTrigger(eventDetails));
                }

                @Override
                public void failure(RetrofitError error)
                {
                    bus.post(new GenericErrorTrigger(ErrorCode.EVENT_DETAILS_FETCH_FAILURE, error));
                }
            });
        }
    }

    public void fetchEventUpdates(String zone, DateTime lastUpdated)
    {
        EventUpdatesApiRequest request = new EventUpdatesApiRequest(zone, lastUpdated);
        eventApi.getEventUpdates(request, new Callback<EventUpdatesApiResponse>()
        {
            @Override
            public void success(EventUpdatesApiResponse eventUpdatesApiResponse, Response response)
            {
                List<Event> updates = eventUpdatesApiResponse.getUpdates();
                if (updates.size() > 0)
                {
                    List<Event> events = updateEventsCache(updates, true);
                    bus.post(new EventUpdatesFetchTrigger(events));
                }
                else
                {
                    bus.post(new EventUpdatesFetchTrigger(null));
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.EVENT_UPDATES_FETCH_FAILURE, error));
            }
        });
    }

    public List<String> getUpdatedEvents()
    {
        Cache cache = Cache.getInstance();
        List<String> updatedEvents = (List<String>) cache.get(CacheKeys.EVENTS_UPDATES);
        if (updatedEvents == null)
        {
            updatedEvents = new ArrayList<>();
        }

        return updatedEvents;
    }

    public void updateRsvp(final Event updatedEvent, final Event.RSVP oldRsvp)
    {
        RsvpUpdateApiRequest request = new RsvpUpdateApiRequest(updatedEvent.getId(), updatedEvent.getRsvp());
        eventApi.updateRsvp(request, new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {
                updateCacheFor(updatedEvent);
            }

            @Override
            public void failure(RetrofitError error)
            {
                updatedEvent.setRsvp(oldRsvp);
                updateCacheFor(updatedEvent);
                bus.post(new GenericErrorTrigger(ErrorCode.RSVP_UPDATE_FAILURE, error));
            }
        });
    }

    public void updateCacheFor(Event event)
    {
        Cache cache = Cache.getInstance();
        Map<String, Event> eventMap = (Map<String, Event>) cache.get(CacheKeys.EVENTS);
        if (eventMap != null)
        {
            eventMap.put(event.getId(), event);
            cache.put(CacheKeys.EVENTS, eventMap);
            bus.post(new CacheCommitTrigger());
        }

    }

    public void updateCacheFor(EventDetails eventDetails)
    {
        Cache cache = Cache.getInstance();

        cache.put(CacheKeys.eventDetails(eventDetails.getId()), eventDetails);

        List<String> updatedEvents = getUpdatedEvents();
        updatedEvents.remove(eventDetails.getId());
        cache.put(CacheKeys.EVENTS_UPDATES, updatedEvents);

        bus.post(new CacheCommitTrigger());
    }

    private List<Event> updateEventsCache(List<Event> events, boolean append)
    {
        Cache cache = Cache.getInstance();

        if (append)
        {
            Map<String, Event> eventMap = (Map<String, Event>) cache.get(CacheKeys.EVENTS);
            List<String> eventUpdates = (List<String>) cache.get(CacheKeys.EVENTS_UPDATES);

            if (eventMap == null)
            {
                eventMap = new HashMap<>();
            }
            if (eventUpdates == null)
            {
                eventUpdates = new ArrayList<>();
            }

            for (Event event : events)
            {
                String eventId = event.getId();
                if (eventMap.containsKey(eventId))
                {
                    eventUpdates.add(eventId);
                }

                eventMap.put(eventId, event);
            }

            DateTime time = DateTime.now();

            cache.put(CacheKeys.EVENTS, eventMap);
            cache.put(CacheKeys.EVENTS_UPDATES, eventUpdates);
            cache.put(CacheKeys.EVENTS_TIMESTAMP, time);

            bus.post(new CacheCommitTrigger());

            return new ArrayList<>(eventMap.values());
        }
        else
        {
            Map<String, Event> eventMap = new HashMap<>();
            for (Event event : events)
            {
                eventMap.put(event.getId(), event);
            }

            DateTime time = DateTime.now();

            cache.put(CacheKeys.EVENTS, eventMap);
            cache.put(CacheKeys.EVENTS_UPDATES, new ArrayList<String>());
            cache.put(CacheKeys.EVENTS_TIMESTAMP, time);

            bus.post(new CacheCommitTrigger());

            return events;
        }
    }
}
