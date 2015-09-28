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
            addSlide(AppIntroFragment.newInstance("Discover Clans", "Check out what your local friends are up to", R.drawable.ic_btn_rsvp_going, Color.parseColor("#FFEB3B")));
            addSlide(AppIntroFragment.newInstance("Make your own clan", "Invite selected friends to your clan or let everyone know", R.drawable.ic_btn_rsvp_maybe, Color.parseColor("#03A9F4")));
            addSlide(AppIntroFragment.newInstance("Invite Facebook Friends", "Go out with your facebook freinds", R.drawable.ic_btn_rsvp_no, Color.parseColor("#F44336")));
            addSlide(AppIntroFragment.newInstance("Invite Phone Contacts", "Invite Phone Contacts to your clans", R.drawable.ic_btn_rsvp_going, Color.parseColor("#03A9F4")));
        } else {
            Intent intent = new Intent(this, FacebookActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onSkipPressed() {

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed() {

        genericCache.put(CacheKeys.IS_FIRST_TIME_USER, false);
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivity(intent);
    }
}
