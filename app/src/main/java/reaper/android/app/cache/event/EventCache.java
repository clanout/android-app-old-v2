package reaper.android.app.cache.event;

import android.util.Log;

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
        dataSource.delete();
        dataSource.write(events);
//        Observable.create(new Observable.OnSubscribe<Object>()
//        {
//            @Override
//            public void call(Subscriber<? super Object> subscriber)
//            {
//                dataSource.delete();
//                dataSource.write(events);
//                subscriber.onCompleted();
//            }
//        })
//                  .subscribeOn(Schedulers.io())
//                  .observeOn(Schedulers.io())
//                  .subscribe(new Subscriber<Object>()
//                  {
//                      @Override
//                      public void onCompleted()
//                      {
//                          Log.d(TAG, "Cache save successful");
//                      }
//
//                      @Override
//                      public void onError(Throwable e)
//                      {
//                          Log.d(TAG, "Cache save failed [" + e.getMessage() + "]");
//                      }
//
//                      @Override
//                      public void onNext(Object o)
//                      {
//
//                      }
//                  });
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
//        Observable.create(new Observable.OnSubscribe<Object>()
//        {
//            @Override
//            public void call(Subscriber<? super Object> subscriber)
//            {
//                dataSource.writeDetails(eventDetails);
//                subscriber.onCompleted();
//            }
//        })
//                  .subscribeOn(Schedulers.io())
//                  .observeOn(Schedulers.io())
//                  .subscribe(new Subscriber<Object>()
//                  {
//                      @Override
//                      public void onCompleted()
//                      {
//                          Log.d(TAG, "Cache save successful");
//                      }
//
//                      @Override
//                      public void onError(Throwable e)
//                      {
//                          Log.d(TAG, "Cache save failed [" + e.getMessage() + "]");
//                      }
//
//                      @Override
//                      public void onNext(Object o)
//                      {
//
//                      }
//                  });
    }

    public void invalidate()
    {
        Observable.create(new Observable.OnSubscribe<Object>()
        {
            @Override
            public void call(Subscriber<? super Object> subscriber)
            {
                dataSource.delete();
                subscriber.onCompleted();
            }
        })
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.io())
                  .subscribe(new Subscriber<Object>()
                  {
                      @Override
                      public void onCompleted()
                      {
                          Log.d(TAG, "Cache invalidation successful");
                      }

                      @Override
                      public void onError(Throwable e)
                      {
                          Log.d(TAG, "Cache invalidation failed [" + e.getMessage() + "]");
                      }

                      @Override
                      public void onNext(Object o)
                      {

                      }
                  });
    }

    public void invalidate(final Event event)
    {
        Observable.create(new Observable.OnSubscribe<Object>()
        {
            @Override
            public void call(Subscriber<? super Object> subscriber)
            {
                dataSource.delete(event.getId());
                subscriber.onCompleted();
            }
        })
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.io())
                  .subscribe(new Subscriber<Object>()
                  {
                      @Override
                      public void onCompleted()
                      {
                          Log.d(TAG, "Cache invalidation successful for event = " + event.getId());
                      }

                      @Override
                      public void onError(Throwable e)
                      {
                          Log.d(TAG, "Cache invalidation failed for event = " + event
                                  .getId() + "[" + e.getMessage() + "]");
                      }

                      @Override
                      public void onNext(Object o)
                      {

                      }
                  });
    }

}
