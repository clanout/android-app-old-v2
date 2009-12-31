package reaper.android.common.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.otto.Bus;

import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.common.cache.AppPreferences;
import reaper.android.common.communicator.Communicator;

public class RegistrationIntentService extends IntentService
{
    private static final String TAG = "RegIntentService";
    private Bus bus;
    private String token;


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
                token = instanceID.getToken(AppConstants.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sendTokenToServer();

                AppPreferences.set(this, Constants.TOKEN_SENT_TO_SERVER, String.valueOf(true));
                AppPreferences.set(this, CacheKeys.GCM_TOKEN, token);
            }
        }
        catch (Exception e)
        {
            AppPreferences.set(this, Constants.TOKEN_SENT_TO_SERVER, String.valueOf(false));
            AppPreferences.set(this, CacheKeys.GCM_TOKEN, null);
        }

        bus.post(new GcmRegistrationCompleteTrigger());
    }

    private void sendTokenToServer()
    {

    }
}
