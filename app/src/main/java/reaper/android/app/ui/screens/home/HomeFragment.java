package reaper.android.app.ui.screens.home;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.Event;
import reaper.android.app.model.factory.CreateEventSuggestionFactory;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.notifications.NewNotificationReceivedTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsAvailableTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsNotAvailableTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.notifications.NotificationFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeFragment extends BaseFragment implements EventsView,
        SwipeRefreshLayout.OnRefreshListener,
        EventsAdapter.EventActionListener,
        EventsAdapter.PagerSwipeListener,
        CreateEventFragment.CreateEventCycleHandler
{
    /* Event Bus */
    Bus bus;

    /* Presenter */
    EventsPresenter presenter;

    /* Subscriptions */
    CompositeSubscription subscriptions;

    /* UI Elements */
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView eventList;
    ViewPager createEvent;
    View noEvents;
    View error;
    View loading;
    ProgressBar progressBar;

    MenuItem notification;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        presenter = new EventsPresenterImpl(bus);
        subscriptions = new CompositeSubscription();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_home);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_home);
        eventList = (RecyclerView) view.findViewById(R.id.rv_home_events);
        noEvents = view.findViewById(R.id.ll_home_noEvents);
        error = view.findViewById(R.id.ll_home_error);
        loading = view.findViewById(R.id.ll_home_loading);
        createEvent = (ViewPager) view.findViewById(R.id.vp_home_create);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_home);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);

        toolbar.setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.one, R.color.accent, R.color.seven, R.color.five);

        initRecyclerView();
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
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);

        if (presenter != null)
        {
            presenter.detachView();
        }
    }

    /* Listeners */
    @Override
    public void onRefresh()
    {
        presenter.refreshEvents();
    }

    @Override
    public void onEventClicked(Event event)
    {
        presenter.selectEvent(event);
    }

    @Override
    public void onRsvpChanged(EventListItem eventListItem, Event event, Event.RSVP rsvp)
    {
        presenter.updateRsvp(eventListItem, event, rsvp);
    }

    @Override
    public void onPagerSwipe(int state)
    {
        if (state == ViewPager.SCROLL_STATE_DRAGGING)
        {
            swipeRefreshLayout.setEnabled(false);
        }
        else
        {
            swipeRefreshLayout.setEnabled(true);
        }
    }

    @Override
    public void addCycle(Subscription subscription)
    {
        subscriptions.add(subscription);
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        eventList.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        noEvents.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        createEvent.setVisibility(View.GONE);
    }

    @Override
    public void showEvents(List<Event> events)
    {
        eventList.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        noEvents.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        createEvent.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);

        eventList.setAdapter(new EventsAdapter(events, this, this, getFragmentManager(), this));
    }

    @Override
    public void showNoEventsMessage()
    {
        eventList.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        noEvents.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        createEvent.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
        initCreateViewPager();
    }

    @Override
    public void showError()
    {
        eventList.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        noEvents.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        createEvent.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
        initCreateViewPager();
    }

    @Override
    public void showOrganizerCannotUpdateRsvpError()
    {
        Snackbar.make(getView(), R.string.cannot_change_rsvp, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showRsvpUpdateError()
    {
        Snackbar.make(getView(), R.string.message_rsvp_update_failure, Snackbar.LENGTH_LONG)
                .show();
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
        eventList.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        noEvents.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        createEvent.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView()
    {
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventList
                .setAdapter(new EventsAdapter(new ArrayList<Event>(), this, this, getFragmentManager(), this));

        eventList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                boolean enabled = false;
                if (eventList != null && eventList.getChildCount() > 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) eventList
                            .getLayoutManager();

                    enabled = linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
                }

                swipeRefreshLayout.setEnabled(enabled);
            }
        });
    }

    private void initCreateViewPager()
    {
        subscriptions.clear();

        List<CreateEventModel> eventSuggestionList = CreateEventSuggestionFactory
                .getEventSuggestions();

        createEvent
                .setAdapter(new CreateEventPagerAdapter(getFragmentManager(), eventSuggestionList));

        createEvent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                onPagerSwipe(state);
            }
        });

        Subscription subscription =
                Observable.interval(2, TimeUnit.SECONDS)
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(new Subscriber<Long>()
                          {
                              @Override
                              public void onCompleted()
                              {

                              }

                              @Override
                              public void onError(Throwable e)
                              {

                              }

                              @Override
                              public void onNext(Long aLong)
                              {
                                  int position = createEvent.getCurrentItem() + 1;
                                  if (position >= createEvent.getAdapter().getCount())
                                  {
                                      createEvent.setCurrentItem(0, false);
                                  }
                                  else
                                  {
                                      createEvent.setCurrentItem(position);
                                  }
                              }
                          });

        addCycle(subscription);
    }

    /* Unrefactored */
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
        final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView
                .findViewById(R.id.et_alert_dialog_add_phone);
        TextView message = (TextView) dialogView.findViewById(R.id.tv_alert_dialog_add_phone_message);
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
                           Boolean wantToCloseDialog = false;
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

                               InputMethodManager inputManager = (InputMethodManager) getActivity()
                                       .getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.hideSoftInputFromWindow(dialogView
                                       .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        NotificationService notificationService = new NotificationService(bus);
        notificationService.areNewNotificationsAvailable();

        notification = menu.findItem(R.id.action_notifications);

        menu.findItem(R.id.action_account).setVisible(true);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(true);
        menu.findItem(R.id.action_status).setVisible(false);

        menu.findItem(R.id.action_account).setIcon(MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT)
                .setColor(getResources()
                        .getColor(R.color.whity))
                .setSizeDp(36)
                .build());

        menu.findItem(R.id.action_notifications)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FragmentUtils.changeFragment(getFragmentManager(), new NotificationFragment());
                    return true;
                }
            });

        if (CacheManager.getGenericCache().get(CacheKeys.MY_PHONE_NUMBER) == null)
        {
            menu.findItem(R.id.action_add_phone).setVisible(true);
            menu.findItem(R.id.action_add_phone).setIcon(MaterialDrawableBuilder.with(getActivity())
                                                                                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                                                                                .setColor(getResources()
                                                                                        .getColor(R.color.whity))
                                                                                .setSizeDp(36)
                                                                                .build());

            menu.findItem(R.id.action_add_phone)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        final View dialogView = inflater
                                .inflate(R.layout.alert_dialog_add_phone, null);
                        builder.setView(dialogView);

                        final EditText phoneNumber = (EditText) dialogView
                                .findViewById(R.id.et_alert_dialog_add_phone);

                        builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

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
                                           Boolean wantToCloseDialog = false;
                                           String parsedPhone = PhoneUtils
                                                   .parsePhone(phoneNumber.getText()
                                                                          .toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                                           if (parsedPhone == null)
                                           {
                                               Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG)
                                                       .show();
                                               wantToCloseDialog = false;
                                           }
                                           else
                                           {
                                               UserService userService = new UserService(bus);
                                               userService.updatePhoneNumber(parsedPhone);

                                               menu.findItem(R.id.action_add_phone)
                                                   .setVisible(false);

                                               InputMethodManager inputManager = (InputMethodManager) getActivity()
                                                       .getSystemService(Context.INPUT_METHOD_SERVICE);
                                               inputManager.hideSoftInputFromWindow(dialogView
                                                       .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                               wantToCloseDialog = true;

                                           }

                                           if (wantToCloseDialog)
                                           {
                                               alertDialog.dismiss();
                                           }
                                       }
                                   });

                        return true;
                    }

                });
        }
        else
        {
            menu.findItem(R.id.action_add_phone).setVisible(false);
        }

        menu.findItem(R.id.action_account)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FragmentUtils.changeFragment(getFragmentManager(), new AccountsFragment());
                    return true;
                }
            });
    }

    @Subscribe
    public void newNotificationsAvailable(NewNotificationsAvailableTrigger trigger)
    {
        notification.setIcon(MaterialDrawableBuilder.with(getActivity())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                                                    .setColor(getResources()
                                                            .getColor(R.color.accent))
                                                    .setSizeDp(36)
                                                    .build());
    }

    @Subscribe
    public void newNotificationsNotAvailable(NewNotificationsNotAvailableTrigger trigger)
    {
        notification.setIcon(MaterialDrawableBuilder.with(getActivity())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                                                    .setColor(getResources()
                                                            .getColor(R.color.whity))
                                                    .setSizeDp(36)
                                                    .build());
    }

    @Subscribe
    public void newNotificationReceived(NewNotificationReceivedTrigger trigger)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notification.setIcon(MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                        .setColor(getResources()
                                .getColor(R.color.accent))
                        .setSizeDp(36)
                        .build());
            }
        });
    }

    @Subscribe
    public void clickOnViewPagerDetected(ViewPagerClickedTrigger trigger)
    {
        subscriptions.clear();
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger)
    {
        if(trigger.getEvents().size() == 0)
        {
            showNoEventsMessage();
        }else
        {
            showEvents(trigger.getEvents());
        }

    }

}
