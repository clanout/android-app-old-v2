package reaper.android.app.ui.screens.invite.facebook;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.model.FriendsComparator;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedFromNetworkTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class InviteFacebookFriendsFragment extends Fragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;
    private FloatingActionButton inviteWhatsapp;
    private Menu menu;
    private Drawable refreshDrawable;

    private InviteFriendsAdapter inviteFriendsAdapter;
    private UserService userService;
    private LocationService locationService;
    private FacebookService facebookService;
    private Bus bus;
    private FragmentManager fragmentManager;

    private GenericCache genericCache;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private List<Friend> friendList;
    private Event event;
    private Drawable whatsappDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite_facebook_friends, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_invite_facebook_friends);
        noFriendsMessage = (TextView) view.findViewById(R.id.tv_invite_facebook_friends_no_users);
        inviteWhatsapp = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_facebook_friends_invite_people_whatsapp);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        displayBasicView();

        Bundle bundle = getArguments();

        if (bundle == null)
        {
            inviteeList = new ArrayList<>();
        } else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get(BundleKeys.INVITEE_LIST);
            event = (Event) bundle.get(BundleKeys.EVENT);

            if (inviteeList == null)
            {
                inviteeList = new ArrayList<>();
            }

        }

        friendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        locationService = new LocationService(bus);
        facebookService = new FacebookService(bus);

        genericCache = CacheManager.getGenericCache();

        inviteWhatsapp.setOnClickListener(this);
        fragmentManager = getActivity().getSupportFragmentManager();

        generateDrawables();

        inviteWhatsapp.setImageDrawable(whatsappDrawable);

        initRecyclerView();
    }

    private void generateDrawables()
    {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        whatsappDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_FACEBOOK_FRIENDS_FRAGMENT);

        bus.register(this);
        userService.getAppFriends(locationService.getUserLocation().getZone());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    private void displayBasicView()
    {
        recyclerView.setVisibility(View.VISIBLE);
        noFriendsMessage.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
    }

    private void displayNoFriendsView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_local_facebook_friends);
    }

    private void displayErrorView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);

        noFriendsMessage.setText(R.string.facebook_friends_not_fetched);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        this.menu = menu;

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);
        menu.findItem(R.id.action_notifications).setVisible(false);

        menu.findItem(R.id.action_refresh).setIcon(refreshDrawable);

        menu.findItem(R.id.action_refresh).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                item.setActionView(R.layout.action_button_refreshing);
                facebookService.getFacebookFriends(false);
                return true;
            }
        });
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, true, bus, event);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, true, bus, event);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (friendList.size() == 0)
        {
            displayNoFriendsView();

        } else
        {
            displayBasicView();
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger)
    {
        if (!trigger.isPolling())
        {
            userService.getAppFriendsFromNetwork(locationService.getUserLocation().getZone());
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now());
        }
    }

    @Subscribe
    public void onFacebookFriendsNotUpdatedOnServer(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_UPDATION_ON_SERVER_FAILURE)
        {
            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
            displayErrorView();
        }
    }

    @Subscribe
    public void onFacebookFriendsIdFetched(FacebookFriendsIdFetchedTrigger trigger)
    {
        if (!trigger.isPolling())
        {
            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsIdNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE)
        {
            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
            displayErrorView();
        }
    }

    @Subscribe
    public void onZonalAppFriendsFetched(AppFriendsFetchedTrigger trigger)
    {
        friendList = trigger.getFriends();
        Collections.sort(friendList, new FriendsComparator());
        refreshRecyclerView();
    }

    @Subscribe
    public void onZonalAppFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.USER_APP_FRIENDS_FETCH_FAILURE)
        {
            displayErrorView();
        }
    }

    @Subscribe
    public void onZonalFriendsFetchedFromNetwork(AppFriendsFetchedFromNetworkTrigger trigger)
    {
        if (menu != null)
        {
            menu.findItem(R.id.action_refresh).setActionView(null);
        }
        friendList = trigger.getFriends();
        Collections.sort(friendList, new FriendsComparator());
        refreshRecyclerView();
    }

    @Subscribe
    public void onZonalFriendsNotFetchedFromNetwork(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.APP_FRIENDS_FETCH_FROM_NETWORK_FAILURE)
        {
            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }

            displayErrorView();
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.fib_fragment_invite_facebook_friends_invite_people_whatsapp)
        {
            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            } else
            {
                Toast.makeText(getActivity(), R.string.whatsapp_not_installed, Toast.LENGTH_LONG).show();
            }
        }
    }
}
