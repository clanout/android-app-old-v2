package reaper.android.app.root;

import android.app.Application;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import reaper.android.app.config.AppConstants;
import reaper.android.app.trigger.CacheCommitTrigger;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class Reaper extends Application
{
    private Bus bus;

    @Override
    public void onCreate()
    {
        super.onCreate();
        init();
    }

    protected void init()
    {
        bus = new Bus(ThreadEnforcer.ANY);
        Communicator.init(bus);
        Cache.init(this, AppConstants.CACHE_FILE);

        bus.register(this);
    }

    @Subscribe
    public void onCacheCommitTrigger(CacheCommitTrigger trigger)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cache.commit(Reaper.this, AppConstants.CACHE_FILE);
            }
        }).start();
    }
}
