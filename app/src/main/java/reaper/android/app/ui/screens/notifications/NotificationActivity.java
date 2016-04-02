package reaper.android.app.ui.screens.notifications;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.chat.ChatActivity;
import reaper.android.app.ui.screens.create.CreateActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;
import reaper.android.app.ui.screens.friends.FriendsActivity;
import reaper.android.app.ui.screens.home.HomeActivity;
import reaper.android.common.analytics.AnalyticsHelper;

public class NotificationActivity extends BaseActivity implements NotificationScreen
{
    public static Intent callingIntent(Context context)
    {
        return new Intent(context, NotificationActivity.class);
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_NOTIFICATION_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_notification);
        setActionBarBackVisibility(true);

        /* Notification View */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, NotificationFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,
                GoogleAnalyticsConstants.ACTION_BACK, null);
        /* Analytics */

        navigateToHomeScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,
                    GoogleAnalyticsConstants.ACTION_UP, null);
            /* Analytics */

            navigateToHomeScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        if (isTaskRoot()) {
            startActivity(HomeActivity.callingIntent(this));
        }
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId, false));
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        startActivity(ChatActivity.callingIntent(this, eventId));
    }

    @Override
    public void navigateToFriendsScreen()
    {
        Set<String> newFriends = getNewFriends();

        if (newFriends == null) {
            startActivity(FriendsActivity.callingIntent(this, new HashSet<String>()));
        }
        else if (newFriends.isEmpty()) {
            startActivity(FriendsActivity.callingIntent(this, new HashSet<String>()));
        }
        else {

            startActivity(FriendsActivity.callingIntent(this, newFriends));
        }
    }

    @Override
    public void navigateToCreateScreen()
    {
        startActivity(CreateActivity.callingIntent(this, null));
    }

    private static Set<String> getNewFriends()
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> newFriends = GsonProvider.getGson().fromJson(CacheManager.getGenericCache
                ().get
                (GenericCacheKeys.NEW_FRIENDS_LIST), type);

        return newFriends;
    }
}
