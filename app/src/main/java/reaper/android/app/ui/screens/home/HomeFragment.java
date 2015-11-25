package reaper.android.app.ui.screens.home;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.notifications.NewNotificationReceivedTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsAvailableTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.create.CreateEventDetailsFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.notifications.NotificationFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class HomeFragment extends BaseFragment implements EventsView,
        EventsAdapter.EventActionListener, SwipeRefreshLayout.OnRefreshListener
{
    /* UI Elements */
    Toolbar toolbar;

    RecyclerView rvFeed;
    TextView tvNoEvents;
    TextView tvServerError;
    ProgressBar loading;

    SwipeRefreshLayout srlFeed;

    MenuItem notification;
    Drawable notificationIcon;

    View rlCreateOverlay_dummy;

    /* Presenter */
    EventsPresenter presenter;

    /* Data */
    int activePosition;

    /* Event Bus */
    Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        List<Event> events = new ArrayList<>();

        if (getArguments() != null)
        {
            //noinspection unchecked
            events = (ArrayList<Event>) getArguments()
                    .getSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = getArguments()
                    .getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
        }

        bus = Communicator.getInstance().getBus();
        presenter = new EventsPresenterImpl(bus, events);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        rvFeed = (RecyclerView) view.findViewById(R.id.rvFeed);
        tvNoEvents = (TextView) view.findViewById(R.id.tvNoEvents);
        tvServerError = (TextView) view.findViewById(R.id.tvServerError);
        loading = (ProgressBar) view.findViewById(R.id.loading);
        srlFeed = (SwipeRefreshLayout) view.findViewById(R.id.srlFeed);
        rlCreateOverlay_dummy = view.findViewById(R.id.rlCreateOverlay_dummy);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        loading.getIndeterminateDrawable().setColorFilter(ContextCompat
                .getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);

        toolbar.setTitle(R.string.title_home);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        srlFeed.setOnRefreshListener(this);
        srlFeed.setColorSchemeResources(R.color.category_icon_one, R.color.category_icon_eight,
                R.color.category_icon_two, R.color.category_icon_three);

        initRecyclerView();

        // TODO : Replace with create clock
        rlCreateOverlay_dummy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentUtils.changeFragment(getFragmentManager(), CreateEventDetailsFragment
                        .newInstance(null, null, null, null, null));
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.HOME_FRAGMENT);
        CacheManager.getGenericCache().put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.HOME);

        initView();

        presenter.attachView(this);

        if (CacheManager.getGenericCache().get(CacheKeys.HAS_FETCHED_PENDING_INVITES) == null)
        {
            displayUpdatePhoneDialog();
        }

        NotificationService notificationService = new NotificationService(bus);
        notificationService.areNewNotificationsAvailable();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());
        presenter.detachView();
        bus.unregister(this);
    }

    /* Listeners */
    @Override
    public void onEventClicked(Event event)
    {
        presenter.selectEvent(event);
    }

    @Override
    public void onRefresh()
    {
        activePosition = 0;
        presenter.refreshEvents();
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);

        srlFeed.setRefreshing(false);
        rvFeed.setVisibility(View.GONE);
        tvNoEvents.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);
    }

    @Override
    public void showEvents(List<Event> events)
    {
        loading.setVisibility(View.GONE);
        tvNoEvents.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);

        srlFeed.setRefreshing(false);
        rvFeed.setAdapter(new EventsAdapter(getActivity(), events, this));
        rvFeed.scrollToPosition(activePosition);
        rvFeed.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoEventsMessage()
    {
        tvNoEvents.setVisibility(View.VISIBLE);

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
        tvNoEvents.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    @Override
    public void gotoDetailsView(List<Event> events, int activePosition)
    {
        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);

        FragmentUtils.changeFragment(getFragmentManager(), eventDetailsContainerFragment);
    }

    /* Helper Methods */
    private void initView()
    {
        rvFeed.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        tvNoEvents.setVisibility(View.GONE);
        tvServerError.setVisibility(View.GONE);

        srlFeed.setRefreshing(false);
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
                boolean enabled = false;
                if (rvFeed.getChildCount() > 0)
                {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvFeed
                            .getLayoutManager();

                    enabled = linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
                }
                srlFeed.setEnabled(enabled);
            }
        });
    }

    /* Action Bar Menu */
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
    {
        Timber.v("Options menu created");

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_home, menu);

        notification = menu.findItem(R.id.action_notifications);

        if (notificationIcon == null)
        {
            notificationIcon = MaterialDrawableBuilder
                    .with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                    .setColor(ContextCompat
                            .getColor(getActivity(), R.color.white))
                    .setSizeDp(36)
                    .build();
        }
        notification.setIcon(notificationIcon);

        notification
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        FragmentUtils
                                .changeFragment(getFragmentManager(), new NotificationFragment());
                        return true;
                    }
                });

        menu.findItem(R.id.action_account)
            .setIcon(MaterialDrawableBuilder
                    .with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                    .setColor(ContextCompat.getColor(getActivity(), R.color.white))
                    .setSizeDp(36)
                    .build());

        menu.findItem(R.id.action_account)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    FragmentUtils.changeFragment(getFragmentManager(), new AccountsFragment());
                    return true;
                }
            });
    }

    /* Unrefactored */
    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void newNotificationsAvailable(NewNotificationsAvailableTrigger trigger)
    {
        notificationIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.accent))
                .setSizeDp(36)
                .build();

        if (notification != null)
        {
            notification.setIcon(notificationIcon);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void newNotificationReceived(NewNotificationReceivedTrigger trigger)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notificationIcon = MaterialDrawableBuilder
                        .with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                        .setColor(ContextCompat
                                .getColor(getActivity(), R.color.accent))
                        .setSizeDp(36)
                        .build();

                if (notification != null)
                {
                    notification.setIcon(notificationIcon);
                }
            }
        });
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger)
    {
        if (trigger.getEvents().size() == 0)
        {
            showNoEventsMessage();
        }
        else
        {
            showEvents(trigger.getEvents());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void displayUpdatePhoneDialog()
    {
        final GenericCache genericCache = CacheManager.getGenericCache();
        final EventService eventService = new EventService(bus);
        final LocationService locationService = new LocationService(bus);
        final UserService userService = new UserService(bus);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.fetch_pending_invites_title);
        builder.setMessage(R.string.fetch_pending_invites_message);
        builder.setCancelable(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater
                .inflate(R.layout.alert_dialog_add_phone, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView
                .findViewById(R.id.et_alert_dialog_add_phone);
        TextView message = (TextView) dialogView
                .findViewById(R.id.tv_alert_dialog_add_phone_message);
        message.setVisibility(View.GONE);

        builder.setPositiveButton(R.string.fetch_pending_invites_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        builder.setNegativeButton(R.string.fetch_pending_invites_negative_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                genericCache.put(CacheKeys.HAS_FETCHED_PENDING_INVITES, true);

                eventService.fetchEvents(locationService.getUserLocation().getZone());
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                   .setOnClickListener(new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                           Boolean wantToCloseDialog;
                           String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText()
                                                                                 .toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                           if (parsedPhone == null)
                           {
                               Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG)
                                       .show();
                               wantToCloseDialog = false;
                           }
                           else
                           {
                               userService.updatePhoneNumber(parsedPhone);

                               userService.fetchPendingInvites(parsedPhone, locationService
                                       .getUserLocation().getZone());

                               SoftKeyboardHandler.hideKeyboard(getActivity(), dialogView);

                               wantToCloseDialog = true;
                           }

                           if (wantToCloseDialog)
                           {
                               alertDialog.dismiss();

                               showLoading();
                           }
                       }
                   });

    }
}
