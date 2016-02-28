package reaper.android.app.ui.screens.notifications;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.chat.ChatActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;
import reaper.android.app.ui.screens.home.HomeActivity;

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
        navigateToHomeScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            navigateToHomeScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        if (isTaskRoot())
        {
            startActivity(HomeActivity.callingIntent(this));
        }
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId));
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        startActivity(ChatActivity.callingIntent(this, eventId));
    }
}
