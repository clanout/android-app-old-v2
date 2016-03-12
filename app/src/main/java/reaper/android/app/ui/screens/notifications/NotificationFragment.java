package reaper.android.app.ui.screens.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.NotificationWrapper;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.service.NotificationService;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenter;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenterImpl;
import reaper.android.app.ui.screens.notifications.mvp.NotificationView;
import reaper.android.app.model.Notification;
import reaper.android.common.analytics.AnalyticsHelper;

public class NotificationFragment extends BaseFragment implements
        NotificationView,
        NotificationAdapter.NotificationClickListener
{
    public static NotificationFragment newInstance()
    {
        return new NotificationFragment();
    }

    NotificationScreen screen;

    NotificationPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvNotifications)
    RecyclerView rvNotifications;

    @Bind(R.id.tvNoNotifications)
    TextView tvNoNotifications;

    @Bind(R.id.loading)
    ProgressBar loading;

    /* Lifecycle Methds */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* Services */
        NotificationService notificationService = NotificationService.getInstance();

        /* Presenter */
        presenter = new NotificationPresenterImpl(notificationService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (NotificationScreen) getActivity();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_notification, menu);

        MenuItem clear = menu.findItem(R.id.action_clear);
        clear.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                presenter.onDeleteAll();
                return true;
            }
        });
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
    }

    @Override
    public void displayNotifications(List<NotificationWrapper> notifications)
    {
        rvNotifications.setAdapter(new NotificationAdapter(getActivity(), notifications, this));

        rvNotifications.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
                    {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
                    {
                        int position = viewHolder.getAdapterPosition();
                        presenter.onNotificationDeleted(position);
                        rvNotifications.getAdapter().notifyItemRemoved(position);
                    }
                };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvNotifications);
    }

    @Override
    public void displayNoNotificationsMessage()
    {
        tvNoNotifications.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        screen.navigateToDetailsScreen(eventId);
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        screen.navigateToChatScreen(eventId);
    }

    @Override
    public void navigateToFriendsScreen()
    {
        screen.navigateToFriendsScreen();
    }

    /* Listeners */
    @Override
    public void onNotificationClicked(NotificationWrapper notification)
    {
        presenter.onNotificationSelected(notification);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvNotifications
                .setAdapter(new NotificationAdapter(getActivity(), new ArrayList<NotificationWrapper>(), this));
    }
}
