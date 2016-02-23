package reaper.android.common.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.otto.Bus;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.gcm.NotificationApi;
import reaper.android.app.api.gcm.request.GCmRegisterUserApiRequest;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import retrofit.client.Response;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class RegistrationIntentService extends IntentService
{
    private static final String TAG = "RegIntentService";
    private Bus bus;
    private GenericCache genericCache;

    public RegistrationIntentService()
    {
        super(TAG);
        this.bus = Communicator.getInstance().getBus();
        genericCache = CacheManager.getGenericCache();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            synchronized (TAG)
            {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID
                        .getToken(AppConstants.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sendTokenToServer(token);
            }
        }
        catch (Exception e)
        {
            genericCache.delete(GenericCacheKeys.GCM_TOKEN);
            genericCache.put(GenericCacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);

        }
        bus.post(new GcmRegistrationCompleteTrigger());
    }

    private void sendTokenToServer(final String token)
    {
        Bus bus = Communicator.getInstance().getBus();
        UserService userService = UserService.getInstance();

        NotificationApi notificationApi = ApiManager.getInstance().getApi(NotificationApi.class);

        GCmRegisterUserApiRequest request = new GCmRegisterUserApiRequest(token);
        notificationApi.registerUser(request)
                       .subscribeOn(Schedulers.newThread())
                       .observeOn(Schedulers.newThread())
                       .subscribe(new Subscriber<Response>()
                       {
                           @Override
                           public void onCompleted()
                           {
                           }

                           @Override
                           public void onError(Throwable e)
                           {
                               genericCache.delete(GenericCacheKeys.GCM_TOKEN);
                               genericCache.put(GenericCacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);
                           }

                           @Override
                           public void onNext(Response response)
                           {
                               if (response.getStatus() == 200)
                               {
                                   AnalyticsHelper
                                           .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.GCM_TOKEN_SENT_TO_SERVER, "");
                                   genericCache.put(GenericCacheKeys.GCM_TOKEN, token);
                                   genericCache
                                           .put(GenericCacheKeys.GCM_TOKEN_SENT_TO_SERVER, true);
                               }
                               else
                               {
                                   genericCache.delete(GenericCacheKeys.GCM_TOKEN);
                                   genericCache
                                           .put(GenericCacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);
                               }
                           }
                       });
    }
}
