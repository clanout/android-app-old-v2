package reaper.android.app.ui.screens.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.FlowEntry;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenter;
import reaper.android.app.ui.screens.notifications.mvp.NotificationPresenterImpl;
import reaper.android.app.ui.screens.notifications.mvp.NotificationView;
import reaper.android.common.notification.Notification;

public class NotificationActivity extends BaseActivity implements NotificationView, NotificationAdapter.NotificationClickListener
{
    private static final String ARG_FROM_OUTSIDE_APP = "arg_from_outside_app";

    public static Intent callingIntent(Context context, boolean isFromOutsideApp)
    {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(ARG_FROM_OUTSIDE_APP, isFromOutsideApp);
        return intent;
    }

    NotificationPresenter presenter;

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    @Bind(R.id.rvNotifications)
    RecyclerView rvNotifications;

    @Bind(R.id.tvNoNotifications)
    TextView tvNoNotifications;

    @Bind(R.id.loading)
    ProgressBar loading;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_notification);
        setActionBarBackVisibility(true);

        /* Services */
        NotificationService notificationService = NotificationService.getInstance();
        EventService eventService = EventService.getInstance();

        /* Presenter */
        presenter = new NotificationPresenterImpl(notificationService, eventService);

        initRecyclerView();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        navigateToHomeScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        getMenuInflater().inflate(R.menu.action_notification, menu);

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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
        rvNotifications.setAdapter(new NotificationAdapter(this, notifications, this));

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
        boolean isFromOutsideApp = getIntent().getBooleanExtra(ARG_FROM_OUTSIDE_APP, false);
        if (isFromOutsideApp)
        {
            startActivity(MainActivity.callingIntent(this, FlowEntry.HOME, null, null));
        }

        finish();
    }

    @Override
    public void navigateToDetailsScreen(List<Event> events, String eventId)
    {
        startActivity(MainActivity
                .callingIntent(this, FlowEntry.DETAILS, eventId, (ArrayList<Event>) events));
        finish();
    }

    @Override
    public void navigateToChatScreen(List<Event> events, String eventId)
    {
        startActivity(MainActivity
                .callingIntent(this, FlowEntry.CHAT, eventId, (ArrayList<Event>) events));
        finish();
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
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications
                .setAdapter(new NotificationAdapter(this, new ArrayList<Notification>(), this));
    }
}
