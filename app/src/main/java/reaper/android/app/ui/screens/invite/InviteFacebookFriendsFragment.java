package reaper.android.app.ui.screens.invite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.user.FacebookFriendsFetchedTrigger;
import reaper.android.common.communicator.Communicator;

public class InviteFacebookFriendsFragment extends Fragment
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;

    private InviteFriendsAdapter inviteFriendsAdapter;
    private UserService userService;
    private LocationService locationService;
    private Bus bus;
    private InviteeListCommunicator inviteeListCommunicator;

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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle == null)
        {
            inviteeList = new ArrayList<>();
        }
        else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get("invitee_list");

            if (inviteeList == null)
            {
                inviteeList = new ArrayList<>();
            }

            inviteeListCommunicator = (InviteeListCommunicator) bundle.get("invitee_communicator");
        }

        friendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        bus.register(this);
        userService = new UserService(bus);
        locationService = new LocationService(bus);

        initRecyclerView();

        userService.getFacebookFriends(locationService.getUserLocation().getZone());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, inviteeListCommunicator, true);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, inviteeListCommunicator, true);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (friendList.size() == 0)
        {
            noFriendsMessage.setText("None of your facebook friends are on the app. Invite people by going to the accounts page.");
            noFriendsMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            noFriendsMessage.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onFacebookFriendsFetched(FacebookFriendsFetchedTrigger trigger)
    {
        friendList = trigger.getFriends();

        refreshRecyclerView();
    }

    @Subscribe
    public void onFacebookFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCH_FAILURE)
        {
            noFriendsMessage.setText("Could not load your facebook friends. Please try again.");
            noFriendsMessage.setVisibility(View.VISIBLE);
        }
    }
}
