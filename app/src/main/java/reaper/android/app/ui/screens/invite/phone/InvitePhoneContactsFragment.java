package reaper.android.app.ui.screens.invite.phone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.app.ui.screens.invite.core.InviteeListCommunicator;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class InvitePhoneContactsFragment extends Fragment
{
    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage;
    private Menu menu;
    private LinearLayout lockedContent, mainContent;
    private FloatingActionButton addPhone;

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
        invitesLockedMessage = (TextView) view.findViewById(R.id.tv_fragment_invte_phone_contacts_locked);
        addPhone = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_phone_contacts_add_phone);
        lockedContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_locked_content);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_main_content);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (Cache.getInstance().get(CacheKeys.MY_PHONE_NUMBER) == null)
        {
            mainContent.setVisibility(View.GONE);
            lockedContent.setVisibility(View.VISIBLE);
            invitesLockedMessage.setText("Please add our phone number to send and receive invites to your phone contacts");
            addPhone.setImageResource(R.drawable.ic_action_phone);
            addPhone.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                }
            });

            return;
        }
        else
        {
            mainContent.setVisibility(View.VISIBLE);
            lockedContent.setVisibility(View.GONE);
        }

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
        userService.getPhoneContacts(getActivity().getContentResolver(), locationService.getUserLocation().getZone());
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
                userService.getPhoneContacts(getActivity().getContentResolver(), locationService.getUserLocation().getZone());
                return true;
            }
        });
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

        if (menu != null)
        {
            menu.findItem(R.id.action_refresh).setActionView(null);
        }
    }

    @Subscribe
    public void onPhoneContactsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.PHONE_CONTACTS_FETCH_FAILURE)
        {
            noContactsMessage.setText("Could not load your phone contacts. Please try again.");
            noContactsMessage.setVisibility(View.VISIBLE);

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
        }
    }
}
