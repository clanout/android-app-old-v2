package reaper.android.common.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class ListenerServiceGcm extends GcmListenerService
{
    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");

        Log.d("APP", "from ------ " + from);
        Log.d("APP", "data ------ " + data);
        Log.d("APP", "message ------ " + message);
    }
}
