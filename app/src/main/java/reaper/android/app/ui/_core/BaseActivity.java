package reaper.android.app.ui._core;

import android.support.v7.app.AppCompatActivity;

import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.config.MemoryCacheKeys;

public class BaseActivity extends AppCompatActivity
{
    @Override
    protected void onResume()
    {
        super.onResume();

        CacheManager.getMemoryCache().put(MemoryCacheKeys.IS_APP_IN_FOREGROUND, true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        CacheManager.getMemoryCache().put(MemoryCacheKeys.IS_APP_IN_FOREGROUND, false);
    }

    protected void closeApp()
    {
        finish();
        System.exit(0);
    }
}
