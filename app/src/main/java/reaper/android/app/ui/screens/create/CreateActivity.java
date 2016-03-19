package reaper.android.app.ui.screens.create;

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
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;
import reaper.android.app.ui.screens.home.HomeActivity;
import reaper.android.app.ui.screens.invite.InviteActivity;
import reaper.android.common.analytics.AnalyticsHelper;

public class CreateActivity extends BaseActivity implements CreateScreen
{
    private static final String ARG_CATEGORY = "arg_category";

    public static Intent callingIntent(Context context, EventCategory category)
    {
        Intent intent = new Intent(context, CreateActivity.class);
        intent.putExtra(ARG_CATEGORY, category);
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

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_CREATE_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_create);

        /* Close Action in toolbar */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Create View */
        EventCategory category = (EventCategory) getIntent().getSerializableExtra(ARG_CATEGORY);

        CreateFragment fragment = CreateFragment.newInstance(category);

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
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_CREATE, GoogleAnalyticsConstants.ACTION_GO_TO_HOME, GoogleAnalyticsConstants.LABEL_BACK);
        /* Analytics */

        navigateToHomeScreen();
        finish();
    }

    /* Screen Methods */
    @Override
    public void navigateToInviteScreen(String eventId)
    {
        startActivity(InviteActivity.callingIntent(this, true, eventId));
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId, false));
        finish();
    }

    /* Helper Methods */
    private void navigateToHomeScreen()
    {
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }
}
