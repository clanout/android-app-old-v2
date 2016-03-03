package reaper.android.app.ui.screens.invite;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.service._new.PhonebookService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui._core.PermissionHandler;
import reaper.android.app.ui.dialog.UpdateMobileDialog;
import reaper.android.app.ui.screens.invite.mvp.FriendInviteWrapper;
import reaper.android.app.ui.screens.invite.mvp.InvitePresenter;
import reaper.android.app.ui.screens.invite.mvp.InvitePresenterImpl;
import reaper.android.app.ui.screens.invite.mvp.InviteView;
import reaper.android.app.ui.screens.invite.mvp.PhonebookContactInviteWrapper;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common._debugger.TraceDebugger;

public class InviteFragment extends BaseFragment implements
        InviteView,
        InviteAdapter.InviteListener,
        PermissionHandler.Listener
{
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static InviteFragment newInstance(String eventId)
    {
        InviteFragment fragment = new InviteFragment();

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);

        return fragment;
    }

    InviteScreen screen;

    InvitePresenter presenter;

    /* UI Elements */
    @Bind(R.id.llError)
    View llError;

    @Bind(R.id.tvRetry)
    View tvRetry;

    @Bind(R.id.etSearch)
    EditText etSearch;

    @Bind(R.id.llPermission)
    View llPermission;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.rvFriends)
    RecyclerView rvFriends;

    @Bind(R.id.tvMessage)
    TextView tvMessage;

    @Bind(R.id.rlInvite)
    View rlInvite;

    @Bind(R.id.tvInviteCount)
    TextView tvInviteCount;

    ProgressBar pbRefreshing;

    MenuItem refresh;
    MenuItem addPhone;
    boolean isAddPhoneVisible;

    TextWatcher search;
    String locationZone;
    List<FriendInviteWrapper> friends;
    List<PhonebookContactInviteWrapper> contacts;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        isAddPhoneVisible = true;

        /* Presenter */
        UserService userService = UserService.getInstance();
        EventService eventService = EventService.getInstance();
        PhonebookService_ phonebookService = PhonebookService_.getInstance();
        LocationService_ locationService = LocationService_.getInstance();
        String eventId = getArguments().getString(ARG_EVENT_ID);
        presenter = new InvitePresenterImpl(userService, eventService, phonebookService, locationService, eventId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (InviteScreen) getActivity();

        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        rlInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.sendInvitations();
                }
            }
        });

        llPermission.setVisibility(View.GONE);
        screen.setReadContactsPermissionListener(this);
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        etSearch.removeTextChangedListener(search);
        search = null;

        rlInvite.setOnClickListener(null);
        llPermission.setOnClickListener(null);
        screen.setReadContactsPermissionListener(null);
        tvRetry.setOnClickListener(null);
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_invite, menu);

        refresh = menu.findItem(R.id.action_refresh);
        addPhone = menu.findItem(R.id.action_add_phone);

        Drawable addPhoneIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build();
        addPhone.setIcon(addPhoneIcon);

        Drawable refreshIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build();
        refresh.setIcon(refreshIcon);

        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (presenter != null)
                {
                    presenter.refresh();
                }
                return true;
            }
        });

        addPhone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                UpdateMobileDialog.show(getActivity(), new UpdateMobileDialog.Listener()
                {
                    @Override
                    public void onSuccess(String mobileNumber)
                    {
                        addPhone.setVisible(false);
                    }
                });
                return true;
            }
        });

        addPhone.setVisible(isAddPhoneVisible);
    }

    /* Permission Handling */
    @Override
    public void onPermissionGranted(@PermissionHandler.Permissions int permission)
    {
        llPermission.setOnClickListener(null);
        presenter.attachView(this);

        if (llPermission.getVisibility() != View.GONE)
        {
            VisibilityAnimationUtil.collapse(llPermission, 200);
        }
    }

    @Override
    public void onPermissionDenied(@PermissionHandler.Permissions int permission)
    {
        llPermission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PermissionHandler
                        .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
            }
        });

        if (llPermission.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llPermission, 200);
        }
    }

    @Override
    public void onPermissionPermanentlyDenied(@PermissionHandler.Permissions int permission)
    {
        llPermission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                screen.navigateToAppSettings();
            }
        });

        if (llPermission.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llPermission, 200);
        }
    }

    /* Listeners */
    @Override
    public void onFriendSelected(FriendInviteWrapper friend, boolean isInvited)
    {
        if (presenter != null)
        {
            presenter.select(friend, isInvited);
        }
    }

    @Override
    public void onContactSelected(PhonebookContactInviteWrapper contact, boolean isInvited)
    {
        if (friends != null)
        {
            presenter.select(contact, isInvited);
        }
    }

    /* View Methods */
    @Override
    public void showAddPhoneOption()
    {
        isAddPhoneVisible = true;
        if (addPhone != null)
        {
            addPhone.setVisible(true);
        }
    }

    @Override
    public void hideAddPhoneOption()
    {
        isAddPhoneVisible = false;
        if (addPhone != null)
        {
            addPhone.setVisible(false);
        }
    }

    @Override
    public void handleReadContactsPermission()
    {
        if (PermissionHandler
                .isRationalRequired(getActivity(), PermissionHandler.Permissions.READ_CONTACTS))
        {
            llPermission.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PermissionHandler
                            .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
                }
            });

            VisibilityAnimationUtil.expand(llPermission, 200);
        }
        else
        {
            // Read contacts permission has not been granted yet. Request it directly.
            PermissionHandler
                    .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
        }
    }

    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public void displayError()
    {
        TraceDebugger.print("InviteError");
        llError.setVisibility(View.VISIBLE);
        tvRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.retry();
                }
            }
        });
    }

    @Override
    public void displayNoFriendOrContactMessage()
    {
        tvMessage.setVisibility(View.VISIBLE);
        rvFriends.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void displayInviteList(String locationZone, List<FriendInviteWrapper> friends, List<PhonebookContactInviteWrapper> phonebookContacts)
    {
        this.locationZone = locationZone;
        this.friends = friends;
        this.contacts = phonebookContacts;
        initSearch();
        etSearch.addTextChangedListener(search);

        refreshRecyclerView(locationZone, friends, phonebookContacts);
    }

    @Override
    public void showInviteButton(int inviteCount)
    {
        tvInviteCount.setText(String.valueOf(inviteCount));
        rlInvite.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInviteButton()
    {
        rlInvite.setVisibility(View.GONE);
    }

    @Override
    public void navigateToDetailsScreen()
    {
        screen.navigateToDetailsScreen();
    }

    @Override
    public void showRefreshing()
    {
        refresh.setActionView(R.layout.view_action_refreshing);
        pbRefreshing = (ProgressBar) refresh.getActionView().findViewById(R.id.pbRefreshing);
        pbRefreshing.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(getActivity(), R.color.white),
                            android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void hideRefreshing()
    {
        refresh.setActionView(null);
        Drawable refreshIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build();
        refresh.setIcon(refreshIcon);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFriends
                .setAdapter(new InviteAdapter(getActivity(), this, null, new ArrayList<FriendInviteWrapper>(), new ArrayList<PhonebookContactInviteWrapper>()));
    }

    private void refreshRecyclerView(String locationZone, List<FriendInviteWrapper> friends, List<PhonebookContactInviteWrapper> contacts)
    {
        rvFriends
                .setAdapter(new InviteAdapter(getActivity(), this, locationZone, friends, contacts));

        rvFriends.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    private void initSearch()
    {
        search = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                List<FriendInviteWrapper> visibleFriends = new ArrayList<>();
                List<PhonebookContactInviteWrapper> visibleContacts = new ArrayList<>();

                if (s.length() >= 1)
                {
                    visibleFriends = new ArrayList<>();
                    for (FriendInviteWrapper friend : friends)
                    {
                        if (friend.getFriend().getName().toLowerCase()
                                  .contains(s.toString().toLowerCase()))
                        {
                            visibleFriends.add(friend);
                        }
                    }

                    visibleContacts = new ArrayList<>();
                    for (PhonebookContactInviteWrapper contact : contacts)
                    {
                        if (contact.getPhonebookContact().getName().toLowerCase()
                                   .contains(s.toString().toLowerCase()))
                        {
                            visibleContacts.add(contact);
                        }
                    }

                    if (visibleFriends.isEmpty() && visibleContacts.isEmpty())
                    {
                        displayNoFriendOrContactMessage();
                    }
                    else
                    {
                        refreshRecyclerView(locationZone, visibleFriends, visibleContacts);
                    }
                }
                else if (s.length() == 0)
                {
                    refreshRecyclerView(locationZone, friends, contacts);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        };
    }
}
