package reaper.android.app.ui.screens.accounts.friends;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Friend;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.user.AllAppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class ManageFriendsFragment extends Fragment implements BlockListCommunicator, View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;
    private ImageButton done;
    private FloatingActionButton shareFacebook, inviteWhatsapp;
    private Menu menu;
    private Drawable refreshDrawable, checkDrawable;

    private ManageFriendsAdapter manageFriendsAdapter;
    private UserService userService;
    private FacebookService facebookService;
    private Bus bus;
    private FragmentManager fragmentManager;

    private ArrayList<String> blockList;
    private ArrayList<String> unblockList;
    private ArrayList<Friend> friendList;

    private GenericCache genericCache;

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
        View view = inflater.inflate(R.layout.fragment_manage_friends, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_manage_friends);
        noFriendsMessage = (TextView) view.findViewById(R.id.tv_manage_friends_no_users);
        done = (ImageButton) view.findViewById(R.id.ib_manage_friends_done);
        shareFacebook = (FloatingActionButton) view.findViewById(R.id.fib_fragment_manage_friends_share_facebook);
        inviteWhatsapp = (FloatingActionButton) view.findViewById(R.id.fib_fragment_manage_friends_invite_people_whatsapp);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        displayBasicView();

        blockList = new ArrayList<>();
        unblockList = new ArrayList<>();
        friendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        facebookService = new FacebookService(bus);
        genericCache = CacheManager.getGenericCache();
        fragmentManager = getActivity().getSupportFragmentManager();

        done.setOnClickListener(this);
        inviteWhatsapp.setOnClickListener(this);
        shareFacebook.setOnClickListener(this);

        generateDrawable();
        done.setImageDrawable(checkDrawable);

        initRecyclerView();
    }

    private void generateDrawable()
    {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        checkDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
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

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.MANAGE_FRIENDS_FRAGMENT);

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.MANAGE_FRIENDS);
        bus.register(this);
        userService.getAllAppFriends();
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
        shareFacebook.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
    }

    private void displayErrorView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        shareFacebook.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);

        noFriendsMessage.setText(R.string.facebook_friends_not_fetched);
    }

    private void displayNoFriendsView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);
        shareFacebook.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_facebook_friends);
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
        manageFriendsAdapter = new ManageFriendsAdapter(getActivity(), friendList, this);

        recyclerView.setAdapter(manageFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        manageFriendsAdapter = new ManageFriendsAdapter(getActivity(), friendList, this);

        recyclerView.setAdapter(manageFriendsAdapter);

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
            userService.getAllAppFriends();
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now().toString());
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
    public void onAllAppFriendsFetched(AllAppFriendsFetchedTrigger trigger)
    {
        if (menu != null)
        {
            menu.findItem(R.id.action_refresh).setActionView(null);
        }

        friendList = (ArrayList<Friend>) trigger.getFriends();
        refreshRecyclerView();
    }

    @Subscribe
    public void onAllAppFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.USER_ALL_APP_FRIENDS_FETCH_FAILURE)
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
        if (view.getId() == R.id.ib_manage_friends_done)
        {
            userService.sendBlockRequests(blockList, unblockList);
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        } else if (view.getId() == R.id.fib_fragment_manage_friends_invite_people_whatsapp)
        {
            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } else
            {
                Toast.makeText(getActivity(), R.string.whatsapp_not_installed, Toast.LENGTH_LONG).show();
            }
        } else if (view.getId() == R.id.fib_fragment_manage_friends_share_facebook)
        {
            Toast.makeText(getActivity(), "Facebook", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void toggleBlock(String id)
    {
        Friend friend = new Friend();
        friend.setId(id);

        int position = friendList.indexOf(friend);

        if (friendList.get(position).isBlocked())
        {
            if (unblockList.contains(id))
            {
                unblockList.remove(id);
                blockList.add(id);
            } else
            {
                unblockList.add(id);

                if (blockList.contains(id))
                {
                    blockList.remove(id);
                }
            }
        } else
        {
            if (blockList.contains(id))
            {
                blockList.remove(id);
                unblockList.add(id);
            } else
            {
                unblockList.remove(id);
                blockList.add(id);
            }
        }
    }
}
