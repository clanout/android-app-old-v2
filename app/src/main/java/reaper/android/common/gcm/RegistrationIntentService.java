package reaper.android.common.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.otto.Bus;

import reaper.android.app.api.core.NotificationApiManager;
import reaper.android.app.api.gcm.NotificationApi;
import reaper.android.app.api.gcm.request.GCmRegisterUserApiRequest;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.common.cache.AppPreferences;
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
        genericCache = new GenericCache();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            synchronized (TAG)
            {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(AppConstants.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sendTokenToServer(token);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error = " + e.getMessage());
            genericCache.delete(CacheKeys.GCM_TOKEN);
            genericCache.put(CacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);
        }


        Log.d(TAG, "here1");
        bus.post(new GcmRegistrationCompleteTrigger());
        Log.d(TAG, "here2");
    }

    private void sendTokenToServer(final String token)
    {
        Bus bus = Communicator.getInstance().getBus();
        UserService userService = new UserService(bus);

        NotificationApi notificationApi = NotificationApiManager.getInstance().getApi(NotificationApi.class);

        GCmRegisterUserApiRequest request = new GCmRegisterUserApiRequest(token, userService.getActiveUserId());
        notificationApi.registerUser(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d(TAG, "saved in cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        genericCache.delete(CacheKeys.GCM_TOKEN);
                        genericCache.put(CacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);
                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            genericCache.put(CacheKeys.GCM_TOKEN, token);
                            genericCache.put(CacheKeys.GCM_TOKEN_SENT_TO_SERVER, true);
                        }
                        else
                        {
                            genericCache.delete(CacheKeys.GCM_TOKEN);
                            genericCache.put(CacheKeys.GCM_TOKEN_SENT_TO_SERVER, false);
                        }
                    }
                });
    }
}
