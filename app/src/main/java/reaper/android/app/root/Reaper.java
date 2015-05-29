package reaper.android.app.root;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import reaper.android.app.config.AppConstants;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class Reaper extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        init();
    }

    protected void init()
    {
        Communicator.init(new Bus(ThreadEnforcer.ANY));
        Cache.init(this, AppConstants.CACHE_FILE);
    }
}
