package reaper.android.app.cache.event;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class EventCache
{
    private static final String TAG = "EventCache";

    private EventCacheDataSource dataSource;

    public EventCache()
    {
        dataSource = EventCacheDataSourceFactory.create();
    }

    public Observable<List<Event>> getEvents()
    {
        return Observable.create(new Observable.OnSubscribe<List<Event>>()
        {
            @Override
            public void call(Subscriber<? super List<Event>> subscriber)
            {
                subscriber.onNext(dataSource.read());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public void save(final List<Event> events)
    {
        evict();
        dataSource.write(events);
    }

    public Observable<Event> getEvent(final String eventId)
    {
        return Observable.create(new Observable.OnSubscribe<Event>()
        {
            @Override
            public void call(Subscriber<? super Event> subscriber)
            {
                subscriber.onNext(dataSource.read(eventId));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public void save(Event event)
    {
        dataSource.write(event);
    }

    public Observable<EventDetails> getEventDetails(final String eventId)
    {
        return Observable.create(new Observable.OnSubscribe<EventDetails>()
        {
            @Override
            public void call(Subscriber<? super EventDetails> subscriber)
            {
                subscriber.onNext(dataSource.readDetails(eventId));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public void save(final EventDetails eventDetails)
    {
        dataSource.writeDetails(eventDetails);
    }

    public void evict()
    {
        dataSource.delete();
    }

    public void invalidate(final String eventId)
    {
        dataSource.delete(eventId, false);
    }

    public void invalidateCompletely(final String eventId)
    {
        dataSource.delete(eventId, true);
    }

}
