package reaper.android.app.cache.event;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import rx.Observable;

public interface EventCache
{
    Observable<List<Event>> getEvents();

    Observable<Event> getEvent(String eventId);

    Observable<EventDetails> getEventDetails(String eventId);

    void reset(List<Event> events);

    void save(Event event);

    void saveDetails(EventDetails eventDetails);

    void deleteAll();

    void delete(String eventId);

    void deleteCompletely(String eventId);

    void markUpdated(List<String> eventIds);

    void markSeen(String eventId);
}
