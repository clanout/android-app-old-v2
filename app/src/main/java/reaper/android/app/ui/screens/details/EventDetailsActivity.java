package reaper.android.app.ui.screens.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.chat.ChatActivity;
import reaper.android.app.ui.screens.details.mvp.EventDetailsContainerPresenter;
import reaper.android.app.ui.screens.details.mvp.EventDetailsContainerPresenterImpl;
import reaper.android.app.ui.screens.details.mvp.EventDetailsContainerView;
import reaper.android.app.ui.screens.edit.EditEventActivity;
import reaper.android.app.ui.screens.home.HomeActivity;
import reaper.android.app.ui.screens.invite.InviteActivity;

public class EventDetailsActivity extends BaseActivity implements
        EventDetailsContainerView,
        EventDetailsScreen
{
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static Intent callingIntent(Context context, String eventId)
    {
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra(ARG_EVENT_ID, eventId);
        return intent;
    }

    EventDetailsContainerPresenter presenter;

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    @Bind(R.id.vpEventDetails)
    ViewPager vpEventDetails;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Setup UI */
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_details);
        setActionBarBackVisibility(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        String eventId = getIntent().getStringExtra(ARG_EVENT_ID);
        presenter = new EventDetailsContainerPresenterImpl(eventService, eventId);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        presenter.detachView();
    }

    @Override
    public void onBackPressed()
    {
        navigateBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            navigateBack();
        }
        return super.onOptionsItemSelected(item);
    }

    /* View Methods */
    @Override
    public void initView(List<Event> events, int activePosition)
    {
        vpEventDetails.setAdapter(new EventDetailsPagerAdapter(getFragmentManager(), events));
        vpEventDetails.setCurrentItem(activePosition);
        vpEventDetails.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if (presenter != null)
                {
                    presenter.setActivePosition(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    @Override
    public void handleError()
    {
        navigateBack();
    }

    /* Screen Methods */
    @Override
    public void navigateBack()
    {
        if (isTaskRoot())
        {
            startActivity(HomeActivity.callingIntent(this));
        }
        finish();
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        startActivity(ChatActivity.callingIntent(this, eventId));
    }

    @Override
    public void navigateToInviteScreen(String eventId)
    {
        startActivity(InviteActivity.callingIntent(this, false, eventId));
    }

    @Override
    public void navigateToEditScreen(Event event, EventDetails eventDetails)
    {
        startActivity(EditEventActivity.callingIntent(this, event, eventDetails));
        finish();
    }
}
