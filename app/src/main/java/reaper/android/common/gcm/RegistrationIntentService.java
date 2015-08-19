package reaper.android.common.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.otto.Bus;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.gcm.NotificationApi;
import reaper.android.app.api.gcm.request.GCmRegisterUserApiRequest;
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

    public RegistrationIntentService()
    {
        super(TAG);
        this.bus = Communicator.getInstance().getBus();
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
            Log.d("APP", "Error = " + e.getMessage());
            AppPreferences.set(this, CacheKeys.GCM_TOKEN, null);
            AppPreferences.set(this, CacheKeys.GCM_TOKEN_SENT_TO_SERVER, String.valueOf(false));

        }
        bus.post(new GcmRegistrationCompleteTrigger());
    }

    private void sendTokenToServer(final String token)
    {
        Bus bus = Communicator.getInstance().getBus();
        UserService userService = new UserService(bus);

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
                        AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN, null);
                        AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN_SENT_TO_SERVER, String.valueOf(false));
                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN, token);
                            AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN_SENT_TO_SERVER, String.valueOf(true));
                        }
                        else
                        {
                            AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN, null);
                            AppPreferences.set(RegistrationIntentService.this, CacheKeys.GCM_TOKEN_SENT_TO_SERVER, String.valueOf(false));
                        }
                    }
                });
    }
}
