package reaper.android.app.ui.screens.accounts.friends;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Friend;
import reaper.android.app.model.FriendsComparator;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.user.AllAppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class ManageFriendsFragment extends BaseFragment implements BlockListCommunicator, View.OnClickListener {
    private RecyclerView recyclerView;
    private TextView noFriendsMessage;
    private Menu menu;
    private Drawable refreshDrawable;
    private Toolbar toolbar;
    private LinearLayout loading, infoContainer, searchContainer;
    private ProgressBar progressBar;
    private EditText search;

    private ManageFriendsAdapter manageFriendsAdapter;
    private UserService userService;
    private FacebookService facebookService;
    private Bus bus;

    private ArrayList<String> blockList;
    private ArrayList<String> unblockList;
    private ArrayList<Friend> friendList;
    private ArrayList<Friend> visibleFriendList;

    private GenericCache genericCache;

    private TextWatcher searchWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.MANAGE_FRIENDS_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_friends, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_manage_friends);
        noFriendsMessage = (TextView) view.findViewById(R.id.tv_manage_friends_no_users);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_manage_friends);
        loading = (LinearLayout) view.findViewById(R.id.ll_fragment_manage_friends_loading);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_manage_friends);
        infoContainer = (LinearLayout) view.findViewById(R.id.llBlockScreenInfoContainer);
        searchContainer = (LinearLayout) view.findViewById(R.id.ll_fragment_manage_friends_search);
        search = (EditText) view.findViewById(R.id.et_fragment_manage_friends_search);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayLoadingView();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);

        blockList = new ArrayList<>();
        unblockList = new ArrayList<>();
        friendList = new ArrayList<>();
        visibleFriendList = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        userService = UserService.getInstance();
        facebookService = new FacebookService(bus);
        genericCache = CacheManager.getGenericCache();

        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() >= 1) {
                    visibleFriendList = new ArrayList<>();
                    for (Friend friend : friendList) {
                        if (friend.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            visibleFriendList.add(friend);
                        }
                    }

                    if (visibleFriendList.size() == 0) {
                        displayNoFriendsView();
                    } else {
                        Collections.sort(visibleFriendList, new FriendsComparator());
                        refreshRecyclerView();
                    }
                } else if (s.length() == 0) {
                    visibleFriendList = new ArrayList<>();

                    for (Friend friend : friendList) {
                        visibleFriendList.add(friend);
                    }

                    Collections.sort(visibleFriendList, new FriendsComparator());
                    refreshRecyclerView();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        generateDrawable();

        initRecyclerView();
    }

    private void generateDrawable() {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(ContextCompat.getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Block a Friend");

        genericCache.put(GenericCacheKeys.ACTIVE_FRAGMENT, BackstackTags.MANAGE_FRIENDS);
        bus.register(this);
        userService.getAllAppFriends();

        search.addTextChangedListener(searchWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();

        userService.sendBlockRequests(blockList, unblockList);

        bus.unregister(this);

        search.removeTextChangedListener(searchWatcher);
    }

    private void displayBasicView() {
        recyclerView.setVisibility(View.VISIBLE);
        noFriendsMessage.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        infoContainer.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.VISIBLE);
    }

    private void displayErrorView() {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        infoContainer.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);

        noFriendsMessage.setText(R.string.error_facebook_friends);
    }

    private void displayNoFriendsView() {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        infoContainer.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_facebook_friends);
    }

    private void displayLoadingView()
    {
        recyclerView.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        infoContainer.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
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
        menu.findItem(R.id.action_status).setVisible(false);

        menu.findItem(R.id.action_refresh).setIcon(refreshDrawable);

        menu.findItem(R.id.action_refresh).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setActionView(R.layout.action_button_refreshing);
                facebookService.getFacebookFriends(false);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            ((MainActivity)getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        manageFriendsAdapter = new ManageFriendsAdapter(getActivity(), visibleFriendList, this);

        recyclerView.setAdapter(manageFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView() {
        Collections.sort(visibleFriendList, new FriendsComparator());
        manageFriendsAdapter = new ManageFriendsAdapter(getActivity(), visibleFriendList, this);

        recyclerView.setAdapter(manageFriendsAdapter);

        if (visibleFriendList.size() == 0) {
            displayNoFriendsView();

        } else {
            displayBasicView();
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger) {
        if (!trigger.isPolling()) {
            userService.getAllAppFriends();
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now().toString());
        }
    }

    @Subscribe
    public void onFacebookFriendsNotUpdatedOnServer(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_UPDATION_ON_SERVER_FAILURE) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (menu != null) {
                        menu.findItem(R.id.action_refresh).setActionView(null);
                    }
                    displayErrorView();
                }
            });
        }
    }

    @Subscribe
    public void onFacebookFriendsIdFetched(FacebookFriendsIdFetchedTrigger trigger) {
        if (!trigger.isPolling()) {
            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsIdNotFetched(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (menu != null) {
                        menu.findItem(R.id.action_refresh).setActionView(null);
                    }
                    displayErrorView();
                }
            });
        }
    }

    @Subscribe
    public void onAllAppFriendsFetched(final AllAppFriendsFetchedTrigger trigger) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (menu != null) {
                    menu.findItem(R.id.action_refresh).setActionView(null);
                }

                friendList = (ArrayList<Friend>) trigger.getFriends();

                visibleFriendList = new ArrayList<>();

                for(Friend friend : friendList)
                {
                    visibleFriendList.add(friend);
                }

                refreshRecyclerView();
            }
        };

        getActivity().runOnUiThread(runnable);
    }

    @Subscribe
    public void onAllAppFriendsNotFetched(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.USER_ALL_APP_FRIENDS_FETCH_FAILURE) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (menu != null) {
                        menu.findItem(R.id.action_refresh).setActionView(null);
                    }

                    displayErrorView();
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
    }

    @Override
    public void toggleBlock(String id, boolean isNowBlocked) {

        if (isNowBlocked) {
            if (!(blockList.contains(id))) {
                blockList.add(id);
            }

            if (unblockList.contains(id)) {
                unblockList.remove(id);
            }
        } else {

            if (!(unblockList.contains(id))) {
                unblockList.add(id);
            }

            if (blockList.contains(id)) {
                blockList.remove(id);
            }
        }
    }
}
