package reaper.android.app.ui._core;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import reaper.android.R;
import reaper.android.app.cache._core.CacheManager;
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

    /* Action Bar */
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    protected void setActionBar(AppBarLayout _appBarLayout)
    {
        appBarLayout = _appBarLayout;
        toolbar = (Toolbar) appBarLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void showActionBar()
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //Hide Shadow for lower version
            View view = findViewById(R.id.ivShadow);
            if (view != null)
            {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void hideActionBar()
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //Hide Shadow for lower version
            View view = findViewById(R.id.ivShadow);
            if (view != null)
            {
                view.setVisibility(View.GONE);
            }
        }
    }

    protected void setScreenTitle(@StringRes int title)
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().setTitle(title);
    }

    protected void setActionBarBackVisibility(boolean isVisible)
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(isVisible);
    }
    /* Action Bar */
}
