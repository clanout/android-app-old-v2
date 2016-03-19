package reaper.android.app.ui.screens.edit;

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
import reaper.android.app.model.Event;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;
import reaper.android.app.ui.screens.home.HomeActivity;
import reaper.android.common.analytics.AnalyticsHelper;

public class EditEventActivity extends BaseActivity implements EditEventScreen
{
    private static final String ARG_EVENT = "arg_event";

    public static Intent callingIntent(Context context, Event event)
    {
        if (event == null)
        {
            throw new IllegalStateException("eventis null");
        }

        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra(ARG_EVENT, event);
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
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_EVENT_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_edit);

        /* Close Action in toolbar */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Edit View */
        Event event = (Event) getIntent().getSerializableExtra(ARG_EVENT);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, EditEventFragment.newInstance(event));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            String eventId = ((Event) getIntent().getSerializableExtra(ARG_EVENT)).getId();
            navigateToDetailsScreen(eventId);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        String eventId = ((Event) getIntent().getSerializableExtra(ARG_EVENT)).getId();
        navigateToDetailsScreen(eventId);
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        Intent intent = EventDetailsActivity.callingIntent(this, eventId, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
