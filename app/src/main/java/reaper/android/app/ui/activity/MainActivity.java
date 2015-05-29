package reaper.android.app.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.ui.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.cache.Cache;

public class MainActivity extends FragmentActivity
{
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Cache.commit(this, AppConstants.CACHE_FILE);
    }
}
