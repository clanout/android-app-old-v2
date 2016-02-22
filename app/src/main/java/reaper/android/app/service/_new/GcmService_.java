package reaper.android.app.service._new;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

import reaper.android.app.root.Reaper;
import reaper.android.common.gcm.RegistrationIntentService;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GcmService_
{
    private static GcmService_ instance;

    public static void init()
    {
        instance = new GcmService_();
    }

    public static GcmService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[GcmService_ Not Initialized]");
        }

        return instance;
    }

    private static final String TOPIC_BASE_URL = "/topics/";

    private GcmService_()
    {
    }

    public void register()
    {
        Context context = Reaper.getReaperContext();
        Intent intent = new Intent(context, RegistrationIntentService.class);
        context.startService(intent);
    }

    public void subscribeTopic(final String token, final String topic)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        try
                        {
                            GcmPubSub.getInstance(Reaper.getReaperContext())
                                     .subscribe(token, TOPIC_BASE_URL + topic, null);
                            subscriber.onCompleted();
                        }
                        catch (IOException e)
                        {
                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("[Event Subscription Failed] " + e.getMessage());
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void unsubscribeTopic(final String token, final String topic)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        try
                        {
                            GcmPubSub.getInstance(Reaper.getReaperContext())
                                     .unsubscribe(token, TOPIC_BASE_URL + topic);
                            subscriber.onCompleted();
                        }
                        catch (IOException e)
                        {
                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
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
                    public void onNext(Object o)
                    {

                    }
                });
    }
}
