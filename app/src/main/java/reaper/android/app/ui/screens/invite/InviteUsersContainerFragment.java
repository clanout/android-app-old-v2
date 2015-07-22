package reaper.android.app.ui.screens.invite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class InviteUsersContainerFragment extends Fragment implements TabLayout.OnTabSelectedListener, InviteeListCommunicator, View.OnClickListener
{
    private ViewPager viewPager;
    private ImageButton done;
    private TabLayout tabLayout;

    private InviteUsersPagerAdapter inviteUsersPagerAdapter;
    private FragmentManager fragmentManager;
    private EventService eventService;
    private Bus bus;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private String eventId;

    private List<String> invitedFacebookFriends;
    private List<String> invitedPhoneContacts;
    private List<String> invitedUsers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite_friends_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_invite_friends_container);
        done = (ImageButton) view.findViewById(R.id.ib_invite_friends_container_done);
        tabLayout = (TabLayout) view.findViewById(R.id.tl_invite_friends_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle == null)
        {
            inviteeList = new ArrayList<>();
        }else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get("invitee_list");
            eventId = (String) bundle.get("event_id");
        }

        invitedFacebookFriends = new ArrayList<>();
        invitedPhoneContacts = new ArrayList<>();
        invitedUsers = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        fragmentManager = getActivity().getSupportFragmentManager();

        inviteUsersPagerAdapter = new InviteUsersPagerAdapter(getChildFragmentManager(), inviteeList, this);
        viewPager.setAdapter(inviteUsersPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        done.setOnClickListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void manageFacebookFriends(String id)
    {
        if(invitedFacebookFriends.contains(id))
        {
            invitedFacebookFriends.remove(id);
        }else
        {
            invitedFacebookFriends.add(id);
        }
    }

    @Override
    public void managePhoneContacts(String id)
    {

    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.ib_invite_friends_container_done)
        {
            invitedUsers.addAll(invitedFacebookFriends);
            invitedUsers.addAll(invitedPhoneContacts);

            eventService.inviteUsers(eventId, invitedUsers);

            FragmentUtils.changeFragment(fragmentManager, new HomeFragment(), false);
        }
    }
}
