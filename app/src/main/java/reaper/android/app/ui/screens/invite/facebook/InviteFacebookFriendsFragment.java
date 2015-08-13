package reaper.android.app.ui.screens.invite.facebook;

import android.content.ComponentName;
import android.content.Intent;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.FacebookFriendsFetchedTrigger;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.common.communicator.Communicator;

public class InviteFacebookFriendsFragment extends Fragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;
    private Menu menu;
    private FloatingActionButton inviteWhatsapp, shareFacebook;

    private InviteFriendsAdapter inviteFriendsAdapter;
    private UserService userService;
    private LocationService locationService;
    private Bus bus;
    private FragmentManager fragmentManager;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private List<Friend> friendList;

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
        shareFacebook = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_facebook_friends_share_facebook);

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
        }
        else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get(BundleKeys.INVITEE_LIST);

            if (inviteeList == null)
            {
                inviteeList = new ArrayList<>();
            }
        }

        friendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        locationService = new LocationService(bus);
        inviteWhatsapp.setOnClickListener(this);
        shareFacebook.setOnClickListener(this);
        fragmentManager = getActivity().getSupportFragmentManager();

        initRecyclerView();
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
        bus.register(this);
        userService.getFacebookFriends(locationService.getUserLocation().getZone());
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

    private void displayNoFriendsView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        shareFacebook.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_local_facebook_friends);
    }

    private void displayErrorView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        shareFacebook.setVisibility(View.GONE);
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
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);

        menu.findItem(R.id.action_refresh).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                menuItem.setActionView(R.layout.action_button_refreshing);
                userService.getFacebookFriends(locationService.getUserLocation().getZone());
                return true;
            }
        });
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, true, bus);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, true, bus);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (friendList.size() == 0)
        {
            displayNoFriendsView();

        }
        else
        {
            displayBasicView();
        }
    }

    @Subscribe
    public void onFacebookFriendsFetched(FacebookFriendsFetchedTrigger trigger)
    {
        friendList = trigger.getFriends();
        refreshRecyclerView();

        if (menu != null)
        {
            menu.findItem(R.id.action_refresh).setActionView(null);
        }
    }

    @Subscribe
    public void onFacebookFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCH_FAILURE)
        {
            displayErrorView();

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
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
                ComponentName componentName = new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, R.string.whatsapp_message);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getActivity(), R.string.whatsapp_not_installed, Toast.LENGTH_LONG).show();
            }
        }
        else if (view.getId() == R.id.fib_fragment_invite_facebook_friends_share_facebook)
        {
            Toast.makeText(getActivity(), "Facebook", Toast.LENGTH_LONG).show();
        }
    }
}
