package reaper.android.app.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

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
public class IntroActivity extends AppIntro {

    private GenericCache genericCache;
    @Override
    public void init(Bundle bundle) {

        genericCache = CacheManager.getGenericCache();

        if (genericCache.get(CacheKeys.IS_FIRST_TIME_USER) == null) {
            // TODO -- Change messages
            addSlide(AppIntroFragment.newInstance("Explore and Join.", "Check out what your friends are planning and join the one that suits your mood. You'll never walk alone.", R.drawable.ic_btn_rsvp_going, Color.parseColor("#FFEB3B")));
            addSlide(AppIntroFragment.newInstance("Privacy in Check", "Make a plan with all your friends or keep it secret with your close ones.", R.drawable.ic_btn_rsvp_maybe, Color.parseColor("#03A9F4")));
            addSlide(AppIntroFragment.newInstance("All friends at one place", "Invite friends from a pool of facebook and phonebook contacts everytime you make a new plan.", R.drawable.ic_btn_rsvp_no, Color.parseColor("#F44336")));
            addSlide(AppIntroFragment.newInstance("Clan Chat", "Make a plan, discuss it with your friends, finalize details and clan out. Simple.", R.drawable.ic_btn_rsvp_going, Color.parseColor("#03A9F4")));
        } else {
            Intent intent = new Intent(this, FacebookActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onSkipPressed() {

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INTRO_ACTIVITY_SKIP_PRESSED, null);

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INTRO_ACTIVITY_DONE_PRESSED, null);

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
        finish();
    }
}
