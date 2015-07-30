package reaper.android.app.ui.screens.accounts.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Friend;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllFacebookFriendsFetchedTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class ManageFriendsFragment extends Fragment implements BlockListCommunicator, View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;
    private ImageButton done;

    private ManageFriendsAdapter manageFriendsAdapter;
    private UserService userService;
    private Bus bus;
    private FragmentManager fragmentManager;

    private ArrayList<String> blockList;
    private ArrayList<String> unblockList;
    private ArrayList<Friend> friendList;

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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        blockList = new ArrayList<>();
        unblockList = new ArrayList<>();
        friendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        bus.register(this);
        userService = new UserService(bus);
        fragmentManager = getActivity().getSupportFragmentManager();

        done.setOnClickListener(this);

        initRecyclerView();

        userService.getAllFacebookFriends();
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
    public void onAllFacebookFriendsFetched(AllFacebookFriendsFetchedTrigger trigger)
    {
        friendList = (ArrayList<Friend>) trigger.getFriends();

        refreshRecyclerView();
    }

    @Subscribe
    public void onFacebookFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.ALL_FACEBOOK_FRIENDS_FETCH_FAILURE)
        {
            noFriendsMessage.setText("Could not load your facebook friends. Please try again.");
            noFriendsMessage.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.ib_manage_friends_done)
        {
            userService.sendBlockRequests(blockList, unblockList);
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment(), false);
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
            }
            else
            {
                unblockList.add(id);

                if (blockList.contains(id))
                {
                    blockList.remove(id);
                }
            }
        }
        else
        {
            if (blockList.contains(id))
            {
                blockList.remove(id);
                unblockList.add(id);
            }
            else
            {
                unblockList.remove(id);
                blockList.add(id);
            }
        }
    }
}
