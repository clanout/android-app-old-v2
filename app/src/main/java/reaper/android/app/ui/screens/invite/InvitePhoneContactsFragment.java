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
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.common.communicator.Communicator;

public class InvitePhoneContactsFragment extends Fragment
{
    private RecyclerView recyclerView;
    private TextView noContactsMessage;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private List<Friend> friendList;
    private InviteFriendsAdapter inviteFriendsAdapter;

    private InviteeListCommunicator inviteeListCommunicator;
    private Bus bus;
    private UserService userService;
    private LocationService locationService;

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
        View view = inflater.inflate(R.layout.fragment_invite_phone_contacts, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_invite_phone_contacts);
        noContactsMessage = (TextView) view.findViewById(R.id.tv_invite_phone_contacts_no_users);

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
        userService.getPhoneContacts(getActivity().getContentResolver());
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

        // TODO - Add 'refresh' menu item
    }


    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, inviteeListCommunicator, false);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, inviteeListCommunicator, false);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (friendList.size() == 0)
        {
            noContactsMessage.setText("None of your phone contacts are on the app. Invite people by going to the accounts page.");
            noContactsMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            noContactsMessage.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onPhoneContactsFetched(PhoneContactsFetchedTrigger trigger)
    {
        friendList = trigger.getPhoneContacts();
        refreshRecyclerView();
    }

    @Subscribe
    public void onPhoneContactsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.PHONE_CONTACTS_FETCH_FAILURE)
        {
            noContactsMessage.setText("Could not load your phone contacts. Please try again.");
            noContactsMessage.setVisibility(View.VISIBLE);
        }
    }
}
