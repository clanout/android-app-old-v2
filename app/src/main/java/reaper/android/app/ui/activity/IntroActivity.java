package reaper.android.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.common.analytics.AnalyticsHelper;

/**
 * Created by harsh on 27/09/15.
 */
public class IntroActivity extends AppIntro
{

    private GenericCache genericCache;

    @Override
    public void init(Bundle bundle)
    {

        genericCache = CacheManager.getGenericCache();

        if (genericCache.get(CacheKeys.IS_FIRST_TIME_USER) == null)
        {
            addSlide(AppIntroFragment
                    .newInstance(getString(R.string.intro_title_1),
                            getString(R.string.intro_description_1),
                            R.drawable.intro_1,
                            ContextCompat.getColor(this, R.color.intro_1)));

            addSlide(AppIntroFragment
                    .newInstance(getString(R.string.intro_title_2),
                            getString(R.string.intro_description_2),
                            R.drawable.intro_2,
                            ContextCompat.getColor(this, R.color.intro_2)));

            addSlide(AppIntroFragment
                    .newInstance(getString(R.string.intro_title_3),
                            getString(R.string.intro_description_3),
                            R.drawable.intro_3,
                            ContextCompat.getColor(this, R.color.intro_3)));

            addSlide(AppIntroFragment
                    .newInstance(getString(R.string.intro_title_4),
                            getString(R.string.intro_description_4),
                            R.drawable.intro_4,
                            ContextCompat.getColor(this, R.color.intro_4)));
        }
        else
        {
            Intent intent = new Intent(this, FacebookActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onSkipPressed()
    {

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INTRO_ACTIVITY_SKIP_PRESSED, null);

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed()
    {

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INTRO_ACTIVITY_DONE_PRESSED, null);

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
        finish();
    }
}
