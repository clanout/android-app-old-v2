package reaper.android.app.ui.screens.home.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.home.HomeScreen;
import reaper.android.app.ui.screens.home.feed.mvp.EventFeedPresenter;
import reaper.android.app.ui.screens.home.feed.mvp.EventFeedPresenterImpl;
import reaper.android.app.ui.screens.home.feed.mvp.EventFeedView;
import reaper.android.app.ui.util.FriendBubbles;
import reaper.android.common.analytics.AnalyticsHelper;

public class EventFeedFragment extends BaseFragment implements
        EventFeedView,
        EventsAdapter.EventActionListener
{
    public static EventFeedFragment newInstance()
    {
        return new EventFeedFragment();
    }

    HomeScreen screen;

    EventFeedPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvFeed)
    RecyclerView rvFeed;

    @Bind(R.id.llNoEvents)
    View llNoEvents;

    @Bind(R.id.friendBubbles)
    View friendBubbles;

    @Bind(R.id.tvServerError)
    TextView tvServerError;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.srlFeed)
    SwipeRefreshLayout srlFeed;

    /* Lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        presenter = new EventFeedPresenterImpl(eventService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_feed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (HomeScreen) getActivity();

        FriendBubbles.render(getActivity(), friendBubbles, "Create a plan with your %s friends");
        friendBubbles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                screen.navigateToCreateDetailsScreen(null);
            }
        });

        initSwipeRefresh();
        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presenter.detachView();
    }

    /* Listeners */
    @Override
    public void onEventClicked(Event event)
    {
        if (presenter != null)
        {
            presenter.selectEvent(event);
        }
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);

        srlFeed.setRefreshing(false);
        rvFeed.setVisibility(View.GONE);
        llNoEvents.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);
    }

    @Override
    public void showEvents(List<Event> events)
    {
        loading.setVisibility(View.GONE);
        llNoEvents.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);

        srlFeed.setRefreshing(false);
        rvFeed.setAdapter(new EventsAdapter(getActivity(), events, this));
        rvFeed.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoEventsMessage()
    {
        llNoEvents.setVisibility(View.VISIBLE);

        srlFeed.setRefreshing(false);
        rvFeed.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);
    }

    @Override
    public void showError()
    {
        tvServerError.setVisibility(View.VISIBLE);

        srlFeed.setRefreshing(false);
        rvFeed.setVisibility(View.GONE);
        llNoEvents.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    @Override
    public void gotoDetailsView(String eventId)
    {
        screen.navigateToDetailsScreen(eventId);
    }

    /* Helper Methods */
    private void initSwipeRefresh()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME, GoogleAnalyticsConstants.ACTION_SWIPE_TO_REFRESH, null);
        /* Analytics */

        srlFeed.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (presenter != null)
                {
                    presenter.refreshEvents();
                }
            }
        });
        srlFeed.setColorSchemeResources(R.color.category_icon_one, R.color.category_icon_eight,
                R.color.category_icon_two, R.color.category_icon_three);
    }

    private void initRecyclerView()
    {
        rvFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFeed.setAdapter(new EventsAdapter(getActivity(), new ArrayList<Event>(), this));

        rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (rvFeed.getVisibility() == View.VISIBLE)
                {
                    boolean enabled = false;
                    if (rvFeed.getChildCount() > 0)
                    {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvFeed
                                .getLayoutManager();

                        enabled = linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
                    }
                    srlFeed.setEnabled(enabled);
                }
            }
        });
    }
}
