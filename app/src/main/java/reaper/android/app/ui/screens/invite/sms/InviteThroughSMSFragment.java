package reaper.android.app.ui.screens.invite.sms;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.common.communicator.Communicator;

/**
 * Created by harsh on 25/09/15.
 */
public class InviteThroughSMSFragment extends BaseFragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView noContactsMessage, invitesLockedMessage;
    private Menu menu;
    private LinearLayout lockedContent, mainContent;
    private FloatingActionButton addPhone, inviteWhatsapp;
    private Drawable refreshDrawable, whatsappDrawable;

    private boolean isPhoneAdded;

    private Bus bus;
    private UserService userService;
    private LocationService locationService;
    private android.app.FragmentManager fragmentManager;
    private GenericCache genericCache;
    private Drawable phoneDrawable;
    private Event event;

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
            displayBasicView();
        }

        Bundle bundle = getArguments();

        if (bundle == null)
        {
            throw new IllegalStateException("Event is null in Invite through SMS Fragment");
        } else
        {
            event = (Event) bundle.get(BundleKeys.EVENT);

        }

//        initRecyclerView();
    }

    private void generateDrawables()
    {
        refreshDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

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

    private void displayInvitesLockedView()
    {
        mainContent.setVisibility(View.GONE);
        lockedContent.setVisibility(View.VISIBLE);
        invitesLockedMessage.setText(R.string.add_phone_number);
        addPhone.setImageDrawable(phoneDrawable);
    }

    private void displayNoContactsView()
    {
        noContactsMessage.setText(R.string.no_local_phone_contacts);
        noContactsMessage.setVisibility(View.VISIBLE);
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
        noContactsMessage.setVisibility(View.GONE);
        lockedContent.setVisibility(View.GONE);
    }

    private void displayErrorView()
    {
        mainContent.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        inviteWhatsapp.setVisibility(View.GONE);
        noContactsMessage.setVisibility(View.VISIBLE);
        noContactsMessage.setText(R.string.phone_contacts_not_fetched);
        lockedContent.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        
    }
}
