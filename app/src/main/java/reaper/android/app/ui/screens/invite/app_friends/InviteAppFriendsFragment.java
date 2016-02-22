package reaper.android.app.ui.screens.invite.app_friends;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.LinkedHashSet;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.model.FriendsComparator;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedFromNetworkTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class InviteAppFriendsFragment extends BaseFragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noFriendsMessage, tabTitle;
    private FloatingActionButton inviteWhatsapp;
    private Menu menu;
    private Drawable refreshDrawable;
    private LinearLayout loading;
    private ProgressBar progressBar;
    private EditText search;
    private LinearLayout searchContainer;
    private View divider;

    private InviteFriendsAdapter inviteFriendsAdapter;
    private UserService userService;
    private FacebookService facebookService;
    private Bus bus;
    private android.app.FragmentManager fragmentManager;

    private GenericCache genericCache;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private ArrayList<EventDetails.Attendee> attendeeList;
    private List<Friend> friendList;
    private List<Friend> visibleFriendList;
    private Event event;
    private Drawable whatsappDrawable;

    private TextWatcher searchWatcher;
    private Drawable addPhoneDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_FACEBOOK_FRIENDS_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite_app_friends, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_invite_facebook_friends);
        noFriendsMessage = (TextView) view.findViewById(R.id.tv_invite_facebook_friends_no_users);
        inviteWhatsapp = (FloatingActionButton) view
                .findViewById(R.id.fib_fragment_invite_facebook_friends_invite_people_whatsapp);
        loading = (LinearLayout) view
                .findViewById(R.id.ll_fragment_invite_facebook_friends_loading);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_invite_facebook_friends);
        search = (EditText) view.findViewById(R.id.et_fragment_invite_facebook_friends_search);
        searchContainer = (LinearLayout) view
                .findViewById(R.id.ll_fragment_invite_facebook_friends_search);
        tabTitle = (TextView) view.findViewById(R.id.tv_invite_app_friends_title);
        divider = view.findViewById(R.id.v_divider_invite_app_friends);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        displayLoadingView();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat
                .getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);

        Bundle bundle = getArguments();

        if (bundle == null)
        {
            inviteeList = new ArrayList<>();
            attendeeList = new ArrayList<>();
        }
        else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get(BundleKeys.INVITEE_LIST);
            attendeeList = (ArrayList<EventDetails.Attendee>) bundle.get(BundleKeys.ATTENDEE_LIST);
            event = (Event) bundle.get(BundleKeys.EVENT);

            if (inviteeList == null)
            {
                inviteeList = new ArrayList<>();
            }

            if (attendeeList == null)
            {
                attendeeList = new ArrayList<>();
            }
        }

        friendList = new ArrayList<>();
        visibleFriendList = new ArrayList<>();

        searchWatcher = new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

                if (s.length() >= 1)
                {
                    visibleFriendList = new ArrayList<>();
                    for (Friend friend : friendList)
                    {
                        if (friend.getName().toLowerCase().contains(s.toString().toLowerCase()))
                        {
                            visibleFriendList.add(friend);
                        }
                    }

                    if (visibleFriendList.size() == 0)
                    {
                        displayNoSearchResultsView();
                    }
                    else
                    {
                        Collections.sort(visibleFriendList, new FriendsComparator());
                        refreshRecyclerView();
                    }
                }
                else if (s.length() == 0)
                {
                    visibleFriendList = new ArrayList<>();

                    for (Friend friend : friendList)
                    {
                        visibleFriendList.add(friend);
                    }

                    Collections.sort(visibleFriendList, new FriendsComparator());
                    refreshRecyclerView();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        };

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        facebookService = new FacebookService(bus);

        genericCache = CacheManager.getGenericCache();

        inviteWhatsapp.setOnClickListener(this);
        fragmentManager = getActivity().getFragmentManager();

        generateDrawables();

        tabTitle.setText(getResources()
                .getString(R.string.message_friends_invite, LocationService_.getInstance()
                                                                            .getCurrentLocation()
                                                                            .getZone()));

        inviteWhatsapp.setImageDrawable(whatsappDrawable);

        initRecyclerView();
    }

    private void generateDrawables()
    {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                                                 .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                                                 .setColor(ContextCompat
                                                         .getColor(getActivity(), R.color.white))
                                                 .setSizeDp(36)
                                                 .build();

        addPhoneDrawable = MaterialDrawableBuilder.with(getActivity())
                                                  .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE)
                                                  .setColor(ContextCompat
                                                          .getColor(getActivity(), R.color.white))
                                                  .setSizeDp(36)
                                                  .build();


        whatsappDrawable = MaterialDrawableBuilder.with(getActivity())
                                                  .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                                                  .setColor(ContextCompat
                                                          .getColor(getActivity(), R.color.white))
                                                  .setSizeDp(36)
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

        bus.register(this);
        userService.getAppFriends(LocationService_.getInstance().getCurrentLocation().getZone());

        search.addTextChangedListener(searchWatcher);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);

        search.removeTextChangedListener(searchWatcher);
    }

    private void displayLoadingView()
    {
        recyclerView.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        divider.setVisibility(View.GONE);
    }

    private void displayBasicView()
    {
        recyclerView.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.VISIBLE);
        noFriendsMessage.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        divider.setVisibility(View.VISIBLE);
    }

    private void displayNoFriendsView()
    {
        recyclerView.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        divider.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_local_facebook_friends);
    }

    private void displayNoSearchResultsView()
    {
        recyclerView.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        divider.setVisibility(View.VISIBLE);

        noFriendsMessage.setText(R.string.no_search_results_facebook);
    }

    private void displayErrorView()
    {
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_LOAD_FACEBOOK_FRIENDS, userService
                        .getActiveUserId());

        recyclerView.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
        noFriendsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);

        noFriendsMessage.setText(R.string.error_facebook_friends);
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
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);
        menu.findItem(R.id.action_notifications).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);

        menu.findItem(R.id.action_refresh).setIcon(refreshDrawable);

        if (genericCache.get(GenericCacheKeys.MY_PHONE_NUMBER) == null)
        {
            menu.findItem(R.id.action_add_phone).setVisible(true);
            menu.findItem(R.id.action_add_phone).setIcon(addPhoneDrawable);
        }
        else
        {
            menu.findItem(R.id.action_add_phone).setVisible(false);
        }

        menu.findItem(R.id.action_refresh)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {

                    SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                    AnalyticsHelper
                            .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INVITE_FACEBOOK_FRIENDS_REFRESH_CLIKCED, userService
                                    .getActiveUserId());

                    item.setActionView(R.layout.action_button_refreshing);
                    facebookService.getFacebookFriends(false);
                    return true;
                }
            });

        menu.findItem(R.id.action_add_phone)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {

                    displayUpdatePhoneDialog();

                    return true;
                }
            });
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, visibleFriendList, bus, event, attendeeList);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {

        visibleFriendList = new ArrayList<Friend>(new LinkedHashSet<Friend>(visibleFriendList));

        Friend friend = new Friend();
        friend.setId(userService.getActiveUserId());

        if (visibleFriendList.contains(friend))
        {
            visibleFriendList.remove(friend);
        }

        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, visibleFriendList, bus, event, attendeeList);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (visibleFriendList.size() == 0)
        {
            displayNoFriendsView();

        }
        else
        {
            displayBasicView();
        }
    }

    @Subscribe
    public void onFacebookFriendsUpdatedOnServer(FacebookFriendsUpdatedOnServerTrigger trigger)
    {
        if (!trigger.isPolling())
        {

            Log.d("APP", "Facebook friends updated on server");

            userService.getAppFriendsFromNetwork(LocationService_.getInstance().getCurrentLocation().getZone());
            genericCache.put(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.now());
        }
    }

    @Subscribe
    public void onFacebookFriendsNotUpdatedOnServer(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_UPDATION_ON_SERVER_FAILURE)
        {

            Log.d("APP", "Facebook friends not updated on server");

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

            Log.d("APP", "Facebook friends id fetched");

            userService.updateFacebookFriends(trigger.getFriendsIdList(), trigger.isPolling());
        }
    }

    @Subscribe
    public void onFacebookFriendsIdNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE)
        {

            Log.d("APP", "Facebook friends id not fetched");

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

        Log.d("APP", "zonal app friends fetched");

        friendList = trigger.getFriends();

        visibleFriendList = new ArrayList<>();

        for (Friend friend : friendList)
        {
            visibleFriendList.add(friend);
        }

        if (genericCache.get(GenericCacheKeys.MY_PHONE_NUMBER) != null)
        {

            userService.getPhoneContacts();

        }
        else
        {

            Collections.sort(visibleFriendList, new FriendsComparator());
            refreshRecyclerView();
        }
    }

    @Subscribe
    public void onZonalAppFriendsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.USER_APP_FRIENDS_FETCH_FAILURE)
        {

            Log.d("APP", "zonal app friends not fetched");

            displayErrorView();
        }
    }

    @Subscribe
    public void onZonalFriendsFetchedFromNetwork(AppFriendsFetchedFromNetworkTrigger trigger)
    {

        Log.d("APP", "zonal friends fetched from network");

        friendList = trigger.getFriends();

        visibleFriendList = new ArrayList<>();

        for (Friend friend : friendList)
        {
            visibleFriendList.add(friend);
        }

        Log.d("APP", "checking permission");

        PackageManager pm = getActivity().getPackageManager();
        if (pm.checkPermission(Manifest.permission.READ_CONTACTS, getActivity()
                .getPackageName()) == PackageManager.PERMISSION_GRANTED)
        {

            Log.d("APP", "checking permission --- true");

            userService.refreshPhoneContacts(getActivity().getContentResolver(), LocationService_.getInstance().getCurrentLocation().getZone());

        }
        else
        {

            Log.d("APP", "checking permission --- false");

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }

            Collections.sort(visibleFriendList, new FriendsComparator());
            refreshRecyclerView();

        }
    }

    @Subscribe
    public void onZonalFriendsNotFetchedFromNetwork(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.APP_FRIENDS_FETCH_FROM_NETWORK_FAILURE)
        {

            Log.d("APP", "zonal friends not fetched from network");

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }

            displayErrorView();
        }
    }

    @Subscribe
    public void onPhoneContactsFetched(PhoneContactsFetchedTrigger trigger)
    {
        Log.d("APP", "phone contacts fetched");


        friendList.addAll(trigger.getPhoneContacts());

        visibleFriendList = new ArrayList<>();

        for (Friend friend : friendList)
        {
            visibleFriendList.add(friend);
        }

        Collections.sort(visibleFriendList, new FriendsComparator());
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
//            displayErrorView();
            Log.d("APP", "phone contacts not fetched");

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }

            Collections.sort(visibleFriendList, new FriendsComparator());
            refreshRecyclerView();
        }
    }


    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.fib_fragment_invite_facebook_friends_invite_people_whatsapp)
        {
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.WHATSAPP_INVITATION_INVITE_FACEBOOK_FRAGMENT, userService
                            .getActiveUserId());

            boolean isWhatsappInstalled = AccountsService
                    .appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, userService
                        .getActiveUserName() + AppConstants.WHATSAPP_INVITATION_MESSAGE + AppConstants.APP_LINK);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
            else
            {
                Snackbar.make(getView(), R.string.error_no_watsapp, Snackbar.LENGTH_LONG)
                        .show();
            }
        }

    }

    private void displayUpdatePhoneDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView
                .findViewById(R.id.etMobileNumber);

        final TextView tvInvalidPhoneError = (TextView) dialogView
                .findViewById(R.id.tvInvalidPhoneError);

        phoneNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                tvInvalidPhoneError.setVisibility(View.INVISIBLE);
            }
        });

        builder.setPositiveButton(R.string.add_phone_positive_button, new DialogInterface.OnClickListener()
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
                           String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText()
                                                                                 .toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                           if (parsedPhone == null)
                           {
                               tvInvalidPhoneError.setVisibility(View.VISIBLE);
                               wantToCloseDialog = false;
                           }
                           else
                           {
                               AnalyticsHelper
                                       .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.PHONE_NUMBER_UPDATED, userService
                                               .getActiveUserId());

                               userService.updatePhoneNumber(parsedPhone);

                               menu.findItem(R.id.action_add_phone).setVisible(false);
                               displayLoadingView();
                               facebookService.getFacebookFriends(false);

                               SoftKeyboardHandler.hideKeyboard(getActivity(), dialogView);
                               wantToCloseDialog = true;
                           }

                           if (wantToCloseDialog)
                           {
                               alertDialog.dismiss();
                           }
                       }
                   });

    }
}
