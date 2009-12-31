package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import reaper.android.app.trigger.gcm.GcmregistrationIntentTrigger;

public class GCMService
{
    private Bus bus;

    public GCMService(Bus bus)
    {
        this.bus = bus;
    }

    public void register()
    {
        bus.post(new GcmregistrationIntentTrigger());
    }
}
