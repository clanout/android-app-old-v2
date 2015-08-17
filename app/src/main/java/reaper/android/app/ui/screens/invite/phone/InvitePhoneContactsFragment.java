package reaper.android.app.ui.screens.invite.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.PhoneAddedTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.screens.invite.core.InviteFriendsAdapter;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class InvitePhoneContactsFragment extends Fragment implements View.OnClickListener
{
    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage;
    private Menu menu;
    private LinearLayout lockedContent, mainContent;
    private FloatingActionButton addPhone, inviteWhatsapp, sharefacebook;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private List<Friend> friendList;
    private InviteFriendsAdapter inviteFriendsAdapter;
    private boolean isPhoneAdded;

    private Bus bus;
    private UserService userService;
    private LocationService locationService;
    private FragmentManager fragmentManager;

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
        inviteWhatsapp = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_phone_contacts_invite_people_whatsapp);
        sharefacebook = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_phone_contacts_share_facebook);
        lockedContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_locked_content);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_phone_contacts_main_content);

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
        sharefacebook.setOnClickListener(this);
        addPhone.setOnClickListener(this);
        fragmentManager = getActivity().getSupportFragmentManager();

        if (Cache.getInstance().get(CacheKeys.MY_PHONE_NUMBER) == null)
        {
            isPhoneAdded = false;
            displayInvitesLockedView();
            return;
        }
        else
        {
            isPhoneAdded = true;
            displayBasicView();
        }

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

        initRecyclerView();
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
    }

    @Override
    public void onResume()
    {
        super.onResume();
        bus.register(this);

        if (isPhoneAdded)
        {
            userService.getPhoneContacts();
        }
    }

    private void displayInvitesLockedView()
    {
        mainContent.setVisibility(View.GONE);
        lockedContent.setVisibility(View.VISIBLE);
        invitesLockedMessage.setText(R.string.add_phone_number);
        addPhone.setImageResource(R.drawable.ic_action_phone);
    }

    private void displayNoContactsView()
    {
        noContactsMessage.setText(R.string.no_local_phone_contacts);
        noContactsMessage.setVisibility(View.VISIBLE);
        sharefacebook.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    private void displayBasicView()
    {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        sharefacebook.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
    }

    private void displayErrorView()
    {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        sharefacebook.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.VISIBLE);
        noContactsMessage.setText(R.string.phone_contacts_not_fetched);
        lockedContent.setVisibility(View.GONE);
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

                if (!isPhoneAdded)
                {
                    Toast.makeText(getActivity(), R.string.add_phone_number, Toast.LENGTH_LONG).show();
                    return true;
                }
                else
                {

                    menuItem.setActionView(R.layout.action_button_refreshing);
                    userService.refreshPhoneContacts(getActivity()
                            .getContentResolver(), locationService.getUserLocation().getZone());
                    return true;
                }
            }
        });
    }

    private void initRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, false, bus);

        recyclerView.setAdapter(inviteFriendsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity(), inviteeList, friendList, false, bus);

        recyclerView.setAdapter(inviteFriendsAdapter);

        if (friendList.size() == 0)
        {
            displayNoContactsView();
        }
        else
        {
            displayBasicView();
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
        displayBasicView();
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
                        Toast.makeText(getActivity(), R.string.phone_invalid, Toast.LENGTH_LONG).show();
                        wantToCloseDialog = false;
                    }
                    else
                    {
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

        }
        else if (view.getId() == R.id.fib_fragment_invite_phone_contacts_invite_people_whatsapp)
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
        else if (view.getId() == R.id.fib_fragment_invite_phone_contacts_share_facebook)
        {
            Toast.makeText(getActivity(), "Facebook", Toast.LENGTH_LONG).show();
        }
    }
}
