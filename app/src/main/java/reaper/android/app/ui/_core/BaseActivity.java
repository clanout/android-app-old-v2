package reaper.android.app.ui._core;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
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

    protected void gotoAppSettings()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    protected void closeApp()
    {
        finish();
        System.exit(0);
    }
}
