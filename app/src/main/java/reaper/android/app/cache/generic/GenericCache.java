package reaper.android.app.cache.generic;

import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import reaper.android.app.api.core.GsonProvider;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GenericCache
{
    private static final String TAG = "GenericCache";

    private static Gson gson = GsonProvider.getGson();
    private static Map<String, String> memoryCache;

    private GenericCacheDataSource dataSource;

    public GenericCache()
    {
        dataSource = GenericCaheDataSourceFactory.create();
        if (memoryCache == null)
        {
            memoryCache = new HashMap<>();
        }
    }

    public void put(final String key, final String value)
    {
        memoryCache.put(key, value);
        Observable.create(new Observable.OnSubscribe<Object>()
        {
            @Override
            public void call(Subscriber<? super Object> subscriber)
            {
                subscriber.onCompleted();
            }
        })
                  .subscribeOn(Schedulers.computation())
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
                          Log.d(TAG, e.getMessage());
                      }

                      @Override
                      public void onNext(Object o)
                      {

                      }
                  });
    }

    public String get(String key)
    {
        String value = memoryCache.get(key);
        if (value == null)
        {
            value = dataSource.read(key);
            memoryCache.put(key, value);
        }
        return value;
    }

    public void delete(final String key)
    {
        Observable.create(new Observable.OnSubscribe<Object>()
        {
            @Override
            public void call(Subscriber<? super Object> subscriber)
            {
                memoryCache.remove(key);
                dataSource.delete(key);
                subscriber.onCompleted();
            }
        });
    }

    public void put(final String key, final Object value)
    {
        put(key, gson.toJson(value));
    }

    public <T> T get(String key, Class<T> clazz)
    {
        try
        {
            String value = get(key);
            return gson.fromJson(value, clazz);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
