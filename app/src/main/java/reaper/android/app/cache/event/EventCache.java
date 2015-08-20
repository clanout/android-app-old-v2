package reaper.android.app.cache.event;

import android.util.Log;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.delete();
                        dataSource.write(events);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event list cache save failed [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
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

    public void save(final Event event)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.write(event);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event(" + event.getId() + ") save failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
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
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.writeDetails(eventDetails);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "EventDetails(" + eventDetails.getId() + ") save failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void evict()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.delete();
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event cache evict failed [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void invalidate(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.delete(eventId, false);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event(" + eventId + ") deletion failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void invalidateCompletely(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.delete(eventId, true);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event(" + eventId + ") deletion failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void markUpdated(final List<String> eventIds)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.markUpdated(eventIds);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event(" + eventIds.toString() + ") mark-updated failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void markRead(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.setUpdatedFalse(eventId);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Event(" + eventId + ") mark-read failed [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

}
