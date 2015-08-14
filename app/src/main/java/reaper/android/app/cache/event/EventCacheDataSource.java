package reaper.android.app.cache.event;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;

public interface EventCacheDataSource
{
    void write(List<Event> events);

    void write(Event event);

    List<Event> read();

    Event read(final String eventId);

    void writeDetails(EventDetails eventDetails);

    EventDetails readDetails(final String eventId);

    void delete();

    void delete(final String eventId);

    void markUpdated(List<Event> events);

    void setUpdatedFalse(Event event);

    void markChatUpdated(List<String> eventIds);

    void setChatUpdatedFalse(final String eventId);
}
