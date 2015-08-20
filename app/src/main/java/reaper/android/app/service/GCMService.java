package reaper.android.app.service;

import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.squareup.otto.Bus;

import java.io.IOException;

import reaper.android.app.root.Reaper;
import reaper.android.app.trigger.gcm.GcmregistrationIntentTrigger;

public class GCMService
{
    private Bus bus;
    private GcmPubSub gcmPubSub;

    public GCMService(Bus bus)
    {
        this.bus = bus;
        gcmPubSub = GcmPubSub.getInstance(Reaper.getReaperContext());
    }

    public void register()
    {
        bus.post(new GcmregistrationIntentTrigger());
    }

    public void subscribeTopic(final String token, final String topic)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    gcmPubSub.subscribe(token, "/topics/" + topic, null);
                }
                catch (IOException e)
                {
                    Log.d("APP", "exception subscribing topic ----- " + e.getMessage());
                }
            }
        }).start();
    }

    public void unsubscribeTopic(final String token, final String topic)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    gcmPubSub.unsubscribe(token, "/topics/" + topic);
                }
                catch (IOException e)
                {
                    Log.d("APP", "exception unsubscribing topic ----- " + e.getMessage());
                }
            }
        }).start();
    }
}