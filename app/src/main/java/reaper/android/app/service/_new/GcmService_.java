package reaper.android.app.service._new;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.root.Reaper;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.gcm.RegistrationIntentService;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GcmService_
{
    private static GcmService_ instance;

    public static GcmService_ getInstance()
    {
        if (instance == null)
        {
            instance = new GcmService_();
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

                            /* Analytics */
                            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_H,null,false);
                            /* Analytics */

                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_EVENT_SUBSCRIPTION_FAILED,null,false);
                        /* Analytics */

                        Timber.e("[Event Subscription Failed] " + e.getMessage());
                    }

                    @Override
                    public void onNext(Object o) {

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
                            /* Analytics */
                            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_E,null,false);
                            /* Analytics */

                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_I,null,false);
                        /* Analytics */
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }
}
