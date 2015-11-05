package reaper.android.app.ui.screens.invite.phone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.model.FriendsComparator;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.PhoneAddedTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class InvitePhoneContactsFragment extends BaseFragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage;
    private Menu menu;
    private LinearLayout lockedContent, mainContent, loading;
    private FloatingActionButton addPhone, inviteWhatsapp;
    private Drawable refreshDrawable, whatsappDrawable;
    private ProgressBar progressBar;
    private EditText search;
    private LinearLayout searchContainer;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private ArrayList<EventDetails.Attendee> attendeeList;
    private List<Friend> friendList;
    private List<Friend> visibleFriendList;
    private Event event;
    private InviteFriendsAdapter inviteFriendsAdapter;
    private boolean isPhoneAdded;

    private Bus bus;
    private UserService userService;
    private LocationService locationService;
    private android.app.FragmentManager fragmentManager;
    private GenericCache genericCache;
    private Drawable phoneDrawable;

    private TextWatcher searchWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
        inviteWhatsapp = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_phone_contacts_invite_people_whatsapp);
        lockedContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_locked_content);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_main_content);
        loading = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_loading);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_invite_phone_contacts);
        search = (EditText) view.findViewById(R.id.et_fragment_invite_phone_contacts_search);
        searchContainer = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_search);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        locationService = new LocationService(bus);
        isPhoneAdded = false;
        inviteWhatsapp.setOnClickListener(this);
        addPhone.setOnClickListener(this);
        fragmentManager = getActivity().getFragmentManager();
        genericCache = CacheManager.getGenericCache();

        generateDrawables();
        inviteWhatsapp.setImageDrawable(whatsappDrawable);
        addPhone.setImageDrawable(phoneDrawable);

        if (genericCache.get(CacheKeys.MY_PHONE_NUMBER) == null)
        {
            isPhoneAdded = false;
            displayInvitesLockedView();
            return;
        } else
        {
            isPhoneAdded = true;
            displayLoadingView();
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);
        }

        Bundle bundle = getArguments();

        if (bundle == null)
        {
            inviteeList = new ArrayList<>();
            attendeeList = new ArrayList<>();
        } else
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) bundle.get(BundleKeys.INVITEE_LIST);
            attendeeList = (ArrayList<EventDetails.Attendee>) bundle.get(BundleKeys.ATTENDEE_LIST);
            event = (Event) bundle.get(BundleKeys.EVENT);

            if (inviteeList == null)
            {
                inviteeList = new ArrayList<>();
            }

            if(attendeeList == null)
            {
                attendeeList = new ArrayList<>();
            }
        }

        friendList = new ArrayList<>();
        visibleFriendList = new ArrayList<>();

        searchWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length() >= 1)
                {
                    visibleFriendList = new ArrayList<>();
                    for(Friend friend : friendList)
                    {
                        if(friend.getName().toLowerCase().contains(s.toString().toLowerCase()))
                        {
                            visibleFriendList.add(friend);
                        }
                    }

                    if(visibleFriendList.size() == 0)
                    {
                        displayNoSearchResultsView();

                    }else{

                        Collections.sort(visibleFriendList, new FriendsComparator());
                        refreshRecyclerView();
                    }

                }else if(s.length() == 0)
                {
                    visibleFriendList = new ArrayList<>();

                    for(Friend friend : friendList)
                    {
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

        initRecyclerView();
    }

    private void generateDrawables()
    {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(36)
                .build();

        whatsappDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(36)
                .build();

        phoneDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(36)
                .build();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);

        search.removeTextChangedListener(searchWatcher);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_PHONEBOOK_CONTACTS_FRAGMENT);

        bus.register(this);

        if (isPhoneAdded)
        {
            userService.getPhoneContacts();
        }

        search.addTextChangedListener(searchWatcher);
    }

    private void displayInvitesLockedView()
    {
        mainContent.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
        lockedContent.setVisibility(View.VISIBLE);
        invitesLockedMessage.setText(R.string.add_phone_number);
        addPhone.setImageDrawable(phoneDrawable);
        loading.setVisibility(View.GONE);
    }

    private void displayNoContactsView()
    {
        noContactsMessage.setText(R.string.no_local_phone_contacts);
        searchContainer.setVisibility(View.VISIBLE);
        noContactsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void displayNoSearchResultsView()
    {
        noContactsMessage.setText(R.string.no_search_results_on_app);
        searchContainer.setVisibility(View.VISIBLE);
        noContactsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void displayBasicView()
    {
        mainContent.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    private void displayErrorView()
    {

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_LOAD_ON_APP_FRIENDS, userService.getActiveUserId());

        mainContent.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.VISIBLE);
        noContactsMessage.setText(R.string.phone_contacts_not_fetched);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    private void displayLoadingView() {

        mainContent.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
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
        menu.findItem(R.id.action_status).setVisible(false);

        menu.findItem(R.id.action_refresh).setIcon(refreshDrawable);

        menu.findItem(R.id.action_refresh).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                if (!isPhoneAdded) {
                    Snackbar.make(getView(), R.string.add_phone_number_toast, Snackbar.LENGTH_LONG).show();
                    return true;
                } else {

                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.INVITE_ON_APP_FRIENDS_REFRESH_CLICKED, userService.getActiveUserId());

                    menuItem.setActionView(R.layout.action_button_refreshing);
                    userService.refreshPhoneContacts(getActivity()
                            .getContentResolver(), locationService.getUserLocation().getZone());
                    return true;
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            setHasOptionsMenu(true);
        }
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, visibleFriendList, false, bus, event, attendeeList);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, visibleFriendList, false, bus, event, attendeeList);
        recyclerView.setAdapter(inviteFriendsAdapter);

        if (visibleFriendList.size() == 0)
        {
            displayNoContactsView();
        } else
        {
            displayBasicView();
        }
    }

    @Subscribe
    public void onPhoneContactsFetched(PhoneContactsFetchedTrigger trigger)
    {
        friendList = trigger.getPhoneContacts();

        visibleFriendList = new ArrayList<>();

        for(Friend friend : friendList)
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
            displayErrorView();

            if (menu != null)
            {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
        }
    }

    @Subscribe
    public void onPhoneAdded(PhoneAddedTrigger trigger)
    {
        isPhoneAdded = true;
        displayLoadingView();
        userService.refreshPhoneContacts(getActivity().getContentResolver(), locationService
                .getUserLocation().getZone());
    }

    @Subscribe
    public void onPhoneNotAdded(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.PHONE_ADD_FAILURE)
        {
            isPhoneAdded = false;
            displayInvitesLockedView();
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.fib_fragment_invite_phone_contacts_add_phone)
        {

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.UPDATE_PHONE_CLICKED_ON_APP_FRAGMENT, userService.getActiveUserId());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
            builder.setView(dialogView);

            final EditText phoneNumber = (EditText) dialogView.findViewById(R.id.et_alert_dialog_add_phone);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;
                    String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText().toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                    if (parsedPhone == null)
                    {
                        Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG).show();
                        wantToCloseDialog = false;
                    } else
                    {

                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.UPDATED_PHONE_ON_APP_FRAGMENT, userService.getActiveUserId());

                        userService.updatePhoneNumber(parsedPhone);

                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(dialogView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        wantToCloseDialog = true;

                    }

                    if (wantToCloseDialog)
                    {
                        alertDialog.dismiss();
                    }
                }
            });

        } else if (view.getId() == R.id.fib_fragment_invite_phone_contacts_invite_people_whatsapp)
        {

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.WHATSAPP_INVITE_ON_APP_FRAGMENT, userService.getActiveUserId());

            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, userService.getActiveUserName() + AppConstants.WHATSAPP_INVITATION_MESSAGE + AppConstants.APP_LINK);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } else
            {
                Snackbar.make(getView(), R.string.whatsapp_not_installed, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
