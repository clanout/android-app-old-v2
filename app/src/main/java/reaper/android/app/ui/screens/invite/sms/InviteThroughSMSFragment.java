package reaper.android.app.ui.screens.invite.sms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import reaper.android.app.model.PhoneContact;
import reaper.android.app.model.PhoneContactComparator;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllPhoneContactsForSMSFetchedTrigger;
import reaper.android.app.trigger.user.PhoneAddedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

/**
 * Created by harsh on 25/09/15.
 */
public class InviteThroughSMSFragment extends BaseFragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage;
    private LinearLayout lockedContent, mainContent;
    private FloatingActionButton addPhone, inviteWhatsapp;
    private Drawable whatsappDrawable;

    private boolean isPhoneAdded;

    private Bus bus;
    private UserService userService;
    private GenericCache genericCache;
    private Drawable phoneDrawable;
    private List<PhoneContact> phoneContactList;

    private InviteThroughSMSAdapter inviteThroughSMSAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_through_sms, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_invite_through_sms);
        noContactsMessage = (TextView) view.findViewById(R.id.tv_invite_through_sms_no_users);
        invitesLockedMessage = (TextView) view.findViewById(R.id.tv_fragment_invte_through_sms_locked);
        addPhone = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_through_sms_add_phone);
        inviteWhatsapp = (FloatingActionButton) view.findViewById(R.id.fib_fragment_invite_through_sms_invite_people_whatsapp);
        lockedContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_through_sms_locked_content);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_through_sms_main_content);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        isPhoneAdded = false;
        inviteWhatsapp.setOnClickListener(this);
        addPhone.setOnClickListener(this);
        genericCache = CacheManager.getGenericCache();

        generateDrawables();
        inviteWhatsapp.setImageDrawable(whatsappDrawable);
        addPhone.setImageDrawable(phoneDrawable);

        if (genericCache.get(CacheKeys.MY_PHONE_NUMBER) == null) {
            isPhoneAdded = false;
            displayInvitesLockedView();
            return;
        } else {
            isPhoneAdded = true;
            displayBasicView();
        }

        phoneContactList = new ArrayList<>();
        initRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();

        bus.register(this);

        if (isPhoneAdded) {
            userService.fetchAllPhoneContacts(getActivity().getContentResolver());
        }
    }

    private void generateDrawables() {

        whatsappDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(24)
                .build();

        phoneDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    private void displayInvitesLockedView() {
        mainContent.setVisibility(View.GONE);
        lockedContent.setVisibility(View.VISIBLE);
        invitesLockedMessage.setText(R.string.add_phone_number);
        addPhone.setImageDrawable(phoneDrawable);
    }

    private void displayNoContactsView() {
        noContactsMessage.setText(R.string.no_local_phone_contacts);
        noContactsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    private void displayBasicView() {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
    }

    private void displayErrorView() {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.VISIBLE);
        noContactsMessage.setText(R.string.phone_contacts_not_fetched);
        lockedContent.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fib_fragment_invite_through_sms_add_phone) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
            builder.setView(dialogView);

            final EditText phoneNumber = (EditText) dialogView.findViewById(R.id.et_alert_dialog_add_phone);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText().toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                    if (parsedPhone == null) {
                        Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG).show();
                        wantToCloseDialog = false;
                    } else {
                        userService.updatePhoneNumber(parsedPhone);

                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(dialogView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        wantToCloseDialog = true;

                    }

                    if (wantToCloseDialog) {
                        alertDialog.dismiss();
                    }
                }
            });

        } else if (view.getId() == R.id.fib_fragment_invite_through_sms_invite_people_whatsapp) {
            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled) {
                // TODO -- Whatsapp invite message

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } else {
                Snackbar.make(getView(), R.string.whatsapp_not_installed, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void onPhoneAdded(PhoneAddedTrigger trigger) {
        isPhoneAdded = true;
        displayBasicView();
        userService.fetchAllPhoneContacts(getActivity().getContentResolver());
    }

    @Subscribe
    public void onPhoneNotAdded(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.PHONE_ADD_FAILURE) {
            isPhoneAdded = false;
            displayInvitesLockedView();
        }
    }

    @Subscribe
    public void onAllContactsForSMSFetched(AllPhoneContactsForSMSFetchedTrigger trigger)
    {
        phoneContactList = trigger.getPhoneContactList();
        Collections.sort(phoneContactList, new PhoneContactComparator());
        refreshRecyclerView();
    }

    @Subscribe
    public void onAllContactsForSMSNotFetched(GenericErrorTrigger trigger)
    {
        if(trigger.getErrorCode() == ErrorCode.PHONE_CONTACTS_FOR_SMS_FETCH_FAILURE)
        {
            displayErrorView();
        }
    }


    private void initRecyclerView() {

        inviteThroughSMSAdapter = new InviteThroughSMSAdapter(getActivity(), phoneContactList, bus);
        recyclerView.setAdapter(inviteThroughSMSAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void refreshRecyclerView() {

        inviteThroughSMSAdapter = new InviteThroughSMSAdapter(getActivity(), phoneContactList, bus);
        recyclerView.setAdapter(inviteThroughSMSAdapter);

        if(phoneContactList.size() == 0)
        {
            displayNoContactsView();
        }else{
            displayBasicView();
        }
    }

}
