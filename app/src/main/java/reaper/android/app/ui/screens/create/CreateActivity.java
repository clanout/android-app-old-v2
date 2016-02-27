package reaper.android.app.ui.screens.create;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import org.joda.time.LocalTime;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.FlowEntry;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.invite.InviteActivity;

public class CreateActivity extends BaseActivity implements CreateScreen
{
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_IS_SECRET = "arg_is_secret";
    private static final String ARG_START_DAY = "arg_start_day";
    private static final String ARG_START_TIME = "arg_start_time";

    public static Intent callingIntent(Context context, String title, EventCategory category,
                                       boolean isSecret, String startDay, LocalTime startTime)
    {
        Intent intent = new Intent(context, CreateActivity.class);

        intent.putExtra(ARG_TITLE, title);
        intent.putExtra(ARG_CATEGORY, category);
        intent.putExtra(ARG_IS_SECRET, isSecret);
        intent.putExtra(ARG_START_DAY, startDay);
        intent.putExtra(ARG_START_TIME, startTime);

        return intent;
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
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_create);
        setActionBarBackVisibility(true);

        /* Create View */
        Intent callingIntent = getIntent();
        String title = callingIntent.getStringExtra(ARG_TITLE);
        EventCategory category = (EventCategory) callingIntent.getSerializableExtra(ARG_CATEGORY);
        boolean isSecret = callingIntent.getBooleanExtra(ARG_IS_SECRET, false);
        String startDay = callingIntent.getStringExtra(ARG_START_DAY);
        LocalTime startTime = (LocalTime) callingIntent.getSerializableExtra(ARG_START_TIME);

        CreateEventDetailsFragment fragment = CreateEventDetailsFragment
                .newInstance(title, category, isSecret, startDay, startTime);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
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

    @Override
    public void onBackPressed()
    {
        navigateToHomeScreen();
    }

    /* Screen Methods */
    @Override
    public void navigateToInviteScreen(String eventId)
    {
        startActivity(InviteActivity.callingIntent(this, true, eventId));
        finish();
    }

    /* Helper Methods */
    private void navigateToHomeScreen()
    {
        startActivity(MainActivity.callingIntent(this, FlowEntry.HOME, null, null));
        finish();
    }
}
