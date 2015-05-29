package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.trigger.EventsFetchTrigger;
import reaper.android.app.trigger.GenericErrorTrigger;
import reaper.android.common.cache.Cache;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EventService
{
    private Bus bus;
    private EventApi eventApi;

    public EventService(Bus bus)
    {
        this.bus = bus;
        eventApi = ApiManager.getInstance().getApi(EventApi.class);
    }

    public void fetchEvents(String zone)
    {
        Cache cache = Cache.getInstance();
        Map<String, Event> eventMap = (Map<String, Event>) cache.get(CacheKeys.EVENTS);
        DateTime lastUpdated = (DateTime) cache.get(CacheKeys.EVENTS_TIMESTAMP);
        if (eventMap != null && lastUpdated != null)
        {
            Log.d("reap3r", "From Cache (Last Updated : " + lastUpdated.toString() + ")");
            List<Event> events = new ArrayList<>(eventMap.values());
            bus.post(new EventsFetchTrigger(events));
        }
        else
        {
            fetchEventsFromCloud(zone);
        }
    }

    public void fetchEventsFromCloud(String zone)
    {
        EventsApiRequest request = new EventsApiRequest(zone);
        eventApi.getEvents(request, new Callback<EventsApiResponse>()
        {
            @Override
            public void success(EventsApiResponse eventsApiResponse, Response response)
            {
                List<Event> events = eventsApiResponse.getEvents();
                bus.post(new EventsFetchTrigger(events));
                updateEventsCache(events);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.EVENTS_FETCH_FAILURE, retrofitError));
            }
        });
    }

    public void updateEventsCache(List<Event> events)
    {
        Map<String, Event> eventMap = new HashMap<>();
        for (Event event : events)
        {
            eventMap.put(event.getId(), event);
        }

        DateTime time = DateTime.now().minusSeconds(10);

        Cache cache = Cache.getInstance();
        cache.put(CacheKeys.EVENTS, eventMap);
        cache.put(CacheKeys.EVENTS_TIMESTAMP, time);
    }
}
