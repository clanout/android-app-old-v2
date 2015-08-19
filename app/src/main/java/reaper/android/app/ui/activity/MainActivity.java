package reaper.android.app.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import reaper.android.R;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.service.GCMService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.cache.AppPreferences;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;

public class MainActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;
    private Bus bus;
    private UserService userService;
    private GCMService gcmService;
    private GenericCache genericCache;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        gcmService = new GCMService(bus);

        genericCache = new GenericCache();

        if (AppPreferences.get(this, CacheKeys.GCM_TOKEN) == null)
        {
            if (checkPlayServices())
            {
                gcmService.register();
            }
        }
        else
        {
            ChatHelper.init(userService.getActiveUserId());
            fragmentManager = getSupportFragmentManager();
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getSupportActionBar().setTitle("reap3r");
        bus.register(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Communicator.getInstance().getBus().post(new CacheCommitTrigger());
        bus.unregister(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ChatHelper.disconnectConnection();
    }

    @Override
    public void onBackPressed()
    {
        String activeFragment = AppPreferences.get(this, CacheKeys.ACTIVE_FRAGMENT);

        if (activeFragment == null)
        {
            super.onBackPressed();
        }

        if (activeFragment.equals(BackstackTags.HOME))
        {
            finish();
        }
        else if (activeFragment.equals(BackstackTags.ACCOUNTS))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
        else if (activeFragment.equals(BackstackTags.MANAGE_FRIENDS))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        }
        else if (activeFragment.equals(BackstackTags.FAQ))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        }
        else if (activeFragment.equals(BackstackTags.CREATE))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
        else if (activeFragment.equals(BackstackTags.INVITE_USERS_CONTAINER))
        {
            bus.post(new BackPressedTrigger(BackstackTags.INVITE_USERS_CONTAINER));
        }
        else if (activeFragment.equals(BackstackTags.EVENT_DETAILS_CONTAINER))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
        else if (activeFragment.equals(BackstackTags.EDIT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.EDIT));
        }
        else if (activeFragment.equals(BackstackTags.CHAT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.CHAT));
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "This device does not support Google Play Services.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Subscribe
    public void onGcmRegistrationComplete(GcmRegistrationCompleteTrigger trigger)
    {
        ChatHelper.init(userService.getActiveUserId());

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                fragmentManager = getSupportFragmentManager();
                FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
            }
        });

    }
}
