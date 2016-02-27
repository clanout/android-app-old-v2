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
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.FlowEntry;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;

public class EditEventActivity extends BaseActivity implements EditEventScreen
{
    private static final String ARG_EVENT = "arg_event";
    private static final String ARG_EVENT_DETAILS = "arg_event_details";

    public static Intent callingIntent(Context context, Event event, EventDetails eventDetails)
    {
        if (event == null || eventDetails == null)
        {
            throw new IllegalStateException("event/event_details is null");
        }

        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra(ARG_EVENT, event);
        intent.putExtra(ARG_EVENT_DETAILS, eventDetails);
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
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_edit);
        setActionBarBackVisibility(true);

        /* Edit View */
        Event event = (Event) getIntent().getSerializableExtra(ARG_EVENT);
        EventDetails eventDetails = (EventDetails) getIntent()
                .getSerializableExtra(ARG_EVENT_DETAILS);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content,
                EditEventFragment.newInstance(event, eventDetails));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            String eventId = ((Event)getIntent().getSerializableExtra(ARG_EVENT)).getId();
            navigateToDetailsScreen(eventId);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        String eventId = ((Event)getIntent().getSerializableExtra(ARG_EVENT)).getId();
        navigateToDetailsScreen(eventId);
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        startActivity(MainActivity.callingIntent(this, FlowEntry.HOME, null, null));
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId));
        finish();
    }
}
