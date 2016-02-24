package reaper.android.app.ui.screens.notifications;

import android.content.Context;
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
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenter;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenterImpl;
import reaper.android.app.ui.screens.notifications.mvp.NotificationView;
import reaper.android.common.notification.Notification;

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
        EventService eventService = EventService.getInstance();

        /* Presenter */
        presenter = new NotificationPresenterImpl(notificationService, eventService);
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
    public void onAttach(Context context)
    {
        super.onAttach(context);
        screen = (NotificationScreen) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
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
        inflater.inflate(R.menu.action_notification, menu);

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
    public void displayNotifications(List<Notification> notifications)
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
    public void navigateToHomeScreen()
    {
        screen.navigateToHomeScreen();
    }

    @Override
    public void navigateToDetailsScreen(List<Event> events, String eventId)
    {
        screen.navigateToDetailsScreen(events, eventId);
    }

    @Override
    public void navigateToChatScreen(List<Event> events, String eventId)
    {
        screen.navigateToChatScreen(events, eventId);
    }

    /* Listeners */
    @Override
    public void onNotificationClicked(Notification notification)
    {
        presenter.onNotificationSelected(notification);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvNotifications
                .setAdapter(new NotificationAdapter(getActivity(), new ArrayList<Notification>(), this));
    }
}
