package reaper.android.app.ui.screens.invite.sms;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.PhoneContact;
import reaper.android.app.model.PhoneContactComparator;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllPhoneContactsForSMSFetchedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

/**
 * Created by harsh on 25/09/15.
 */
public class InviteThroughSMSFragment extends BaseFragment implements View.OnClickListener
{

    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage, givePermission;
    private LinearLayout lockedContent, mainContent, loading;
    private FloatingActionButton addPhone, inviteWhatsapp;
    private Drawable whatsappDrawable;
    private ProgressBar progressBar;
    private EditText search;
    private LinearLayout searchContainer;
    private View divider;

    private Bus bus;
    private UserService userService;
    private GenericCache genericCache;
    private Drawable phoneDrawable;
    private List<PhoneContact> phoneContactList;
    private List<PhoneContact> visiblePhoneContactList;

    private InviteThroughSMSAdapter inviteThroughSMSAdapter;

    private TextWatcher searchWatcher;
    private Drawable addPhoneDrawable;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_THROUGH_SMS_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite_through_sms, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_invite_through_sms);
        noContactsMessage = (TextView) view.findViewById(R.id.tv_invite_through_sms_no_users);
        givePermission = (TextView) view.findViewById(R.id.tvGivePermission);
        invitesLockedMessage = (TextView) view
                .findViewById(R.id.tv_fragment_invte_through_sms_locked);
        addPhone = (FloatingActionButton) view
                .findViewById(R.id.fib_fragment_invite_through_sms_add_phone);
        inviteWhatsapp = (FloatingActionButton) view
                .findViewById(R.id.fib_fragment_invite_through_sms_invite_people_whatsapp);
        lockedContent = (LinearLayout) view
                .findViewById(R.id.ll_fragment_invite_through_sms_locked_content);
        mainContent = (LinearLayout) view
                .findViewById(R.id.ll_fragment_invite_through_sms_main_content);
        loading = (LinearLayout) view.findViewById(R.id.ll_fragment_invite_through_sms_loading);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_invite_through_sms);
        search = (EditText) view.findViewById(R.id.et_fragment_invite_through_sms_search);
        searchContainer = (LinearLayout) view
                .findViewById(R.id.ll_fragment_invite_through_sms_search);
        divider = view.findViewById(R.id.v_divider_invite_sms);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        userService = UserService.getInstance();
        inviteWhatsapp.setOnClickListener(this);
        addPhone.setOnClickListener(this);
        givePermission.setOnClickListener(this);
        genericCache = CacheManager.getGenericCache();

        generateDrawables();
        inviteWhatsapp.setImageDrawable(whatsappDrawable);
        addPhone.setImageDrawable(phoneDrawable);

        phoneContactList = new ArrayList<>();

        visiblePhoneContactList = new ArrayList<>();

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
                    visiblePhoneContactList = new ArrayList<>();
                    for (PhoneContact phoneContact : phoneContactList)
                    {
                        if (phoneContact.getName().toLowerCase()
                                        .contains(s.toString().toLowerCase()))
                        {
                            visiblePhoneContactList.add(phoneContact);
                        }
                    }

                    if (visiblePhoneContactList.size() == 0)
                    {
                        displayNoSearchResultsView();
                    }
                    else
                    {
                        Collections.sort(phoneContactList, new PhoneContactComparator());
                        refreshRecyclerView();
                    }
                }
                else if (s.length() == 0)
                {
                    visiblePhoneContactList = new ArrayList<>();

                    for (PhoneContact phoneContact : phoneContactList)
                    {
                        visiblePhoneContactList.add(phoneContact);
                    }

                    Collections.sort(visiblePhoneContactList, new PhoneContactComparator());
                    refreshRecyclerView();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        };


        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_THROUGH_SMS_FRAGMENT);

        bus.register(this);

        search.addTextChangedListener(searchWatcher);

        userService.fetchAllPhoneContacts(getActivity().getContentResolver());
    }

    private void generateDrawables()
    {

        whatsappDrawable = MaterialDrawableBuilder.with(getActivity())
                                                  .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                                                  .setColor(getResources().getColor(R.color.white))
                                                  .setSizeDp(36)
                                                  .build();

        phoneDrawable = MaterialDrawableBuilder.with(getActivity())
                                               .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                                               .setColor(getResources().getColor(R.color.white))
                                               .setSizeDp(36)
                                               .build();

        addPhoneDrawable = MaterialDrawableBuilder.with(getActivity())
                                                  .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE)
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
    public void onPause()
    {
        super.onPause();

        bus.unregister(this);


        search.removeTextChangedListener(searchWatcher);
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
        noContactsMessage.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        givePermission.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
    }

    private void displayNoSearchResultsView()
    {
        noContactsMessage.setText(R.string.no_search_results_phonebook);
        noContactsMessage.setVisibility(View.VISIBLE);
        givePermission.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
    }

    private void displayBasicView()
    {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        givePermission.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
    }

    private void displayErrorView()
    {

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_LOAD_PHONEBOOK, userService
                        .getSessionUserId());

        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.VISIBLE);
        noContactsMessage.setText(R.string.phone_contacts_not_fetched);
        givePermission.setVisibility(View.VISIBLE);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        searchContainer.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
    }

    private void displayLoadingView()
    {
        mainContent.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
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
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);

        if (genericCache.get(GenericCacheKeys.MY_PHONE_NUMBER) == null)
        {
            menu.findItem(R.id.action_add_phone).setVisible(true);
            menu.findItem(R.id.action_add_phone).setIcon(addPhoneDrawable);
        }
        else
        {
            menu.findItem(R.id.action_add_phone).setVisible(false);
        }

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


    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.fib_fragment_invite_through_sms_add_phone)
        {
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.UPDATE_PHONE_CLICKED_INVITE_SMS_FRAGMENT, userService
                            .getSessionUserId());

            displayUpdatePhoneDialog();
        }
        else if (view.getId() == R.id.fib_fragment_invite_through_sms_invite_people_whatsapp)
        {

            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.WHATSAPP_INVITATION_INVITE_THROUGH_SMS_FRAGMENT, userService
                            .getSessionUserId());

            boolean isWhatsappInstalled = AccountsService
                    .appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, userService
                        .getSessionUserName() + AppConstants.WHATSAPP_INVITATION_MESSAGE + AppConstants.APP_LINK);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            }
            else
            {
                SnackbarFactory.create(getActivity(), R.string.error_no_watsapp);
            }
        }
        else if (view.getId() == R.id.tvGivePermission)
        {
//            Dexter.checkPermission(new PermissionListener() {
//                @Override
//                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//
//                    displayLoadingView();
//                    userService.fetchAllPhoneContacts(getActivity().getContentResolver());
//                }
//
//                @Override
//                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//
//                    if(permissionDeniedResponse.isPermanentlyDenied())
//                    {
//                        goToSettings();
//                    }
//                }
//
//                @Override
//                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//
//                    permissionToken.continuePermissionRequest();
//                }
//            }, Manifest.permission.READ_CONTACTS);

            goToSettings();
        }
    }

    @Subscribe
    public void onAllContactsForSMSFetched(AllPhoneContactsForSMSFetchedTrigger trigger)
    {
        phoneContactList = trigger.getPhoneContactList();

        visiblePhoneContactList = new ArrayList<>();

        for (PhoneContact phoneContact : phoneContactList)
        {
            visiblePhoneContactList.add(phoneContact);
        }

        Collections.sort(visiblePhoneContactList, new PhoneContactComparator());
        refreshRecyclerView();
    }

    @Subscribe
    public void onAllContactsForSMSNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.PHONE_CONTACTS_FOR_SMS_FETCH_FAILURE)
        {
            displayErrorView();
        }
    }


    private void initRecyclerView()
    {

        inviteThroughSMSAdapter = new InviteThroughSMSAdapter(getActivity(), visiblePhoneContactList, bus);
        recyclerView.setAdapter(inviteThroughSMSAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void refreshRecyclerView()
    {

        inviteThroughSMSAdapter = new InviteThroughSMSAdapter(getActivity(), visiblePhoneContactList, bus);
        recyclerView.setAdapter(inviteThroughSMSAdapter);

        if (visiblePhoneContactList.size() == 0)
        {
            displayNoContactsView();
        }
        else
        {
            displayBasicView();
        }
    }

    private void goToSettings()
    {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
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

        alertDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
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
                            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK,
                                    GoogleAnalyticsConstants.PHONE_NUMBER_UPDATED, userService
                                            .getSessionUserId());

                            userService.updatePhoneNumber(parsedPhone);

                            menu.findItem(R.id.action_add_phone).setVisible(false);
                            displayLoadingView();
                            userService.fetchAllPhoneContacts(getActivity().getContentResolver());

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
