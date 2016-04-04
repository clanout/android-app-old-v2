package reaper.android.app.ui.screens.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.User;
import reaper.android.app.service.EventService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.DefaultDialog;
import reaper.android.app.ui.dialog.DescriptionDialog;
import reaper.android.app.ui.dialog.InvitationResponseDialog;
import reaper.android.app.ui.dialog.LastMinuteStatusDialog;
import reaper.android.app.ui.dialog.StatusDialog;
import reaper.android.app.ui.screens.details.mvp.EventDetailsPresenter;
import reaper.android.app.ui.screens.details.mvp.EventDetailsPresenterImpl;
import reaper.android.app.ui.screens.details.mvp.EventDetailsView;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;

public class EventDetailsFragment extends BaseFragment implements
        EventDetailsView,
        EventDetailsAdapter.EventDetailsListener
{
    private static final String ARG_EVENT = "arg_event";

    public static EventDetailsFragment newInstance(Event event)
    {
        if (event == null)
        {
            throw new IllegalStateException("event null");
        }

        EventDetailsFragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);

        return fragment;
    }

    EventDetailsScreen screen;

    EventDetailsPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvEventDetails)
    RecyclerView rvEventDetails;

    @Bind(R.id.llEventActionsContainerYay)
    View llEventActionsContainerYay;

    @Bind(R.id.llEventActionsContainerNay)
    View llEventActionsContainerNay;

    @Bind(R.id.btnInvitationResponse)
    Button btnInvitationResponse;

    @Bind(R.id.ivShadow)
    View ivShadow;

    MenuItem loading;
    MenuItem edit;
    MenuItem delete;
    boolean isLoadingVisible;
    boolean isEditVisible;
    boolean isDeleteVisible;

    EventDetailsAdapter adapter;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        UserService userService = UserService.getInstance();
        NotificationService notificationService = NotificationService.getInstance();
        Event event = (Event) getArguments().getSerializable(ARG_EVENT);
        presenter = new EventDetailsPresenterImpl(eventService, userService, notificationService,
                CacheManager.getGenericCache(), event);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (EventDetailsScreen) getActivity();
        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_details, menu);

        edit = menu.findItem(R.id.action_edit);
        delete = menu.findItem(R.id.action_delete);
        loading = menu.findItem(R.id.action_refresh);

        loading.setActionView(R.layout.view_action_refreshing);
        ProgressBar pbRefreshing = (ProgressBar) loading.getActionView()
                                                        .findViewById(R.id.pbRefreshing);
        pbRefreshing.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(getActivity(), R.color.white),
                            android.graphics.PorterDuff.Mode.SRC_IN);

        edit.setVisible(isEditVisible);
        delete.setVisible(isDeleteVisible);
        loading.setVisible(isLoadingVisible);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        edit.setVisible(isEditVisible);
        delete.setVisible(isDeleteVisible);
        loading.setVisible(isLoadingVisible);

        if (presenter != null)
        {
            screen.setTitle(presenter.getTitle());
        }

        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (presenter != null)
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_EDIT);
                    /* Analytics */

                    presenter.edit();
                }
                return true;
            }
        });

        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_DELETE);
                /* Analytics */

                if (presenter != null)
                {
                    displayDeleteDialog();
                }

                return true;
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.btnInvite)
    public void onInviteClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_OPEN,GoogleAnalyticsConstants.LABEL_FROM_DETAILS);
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_INVITE);
        /* Analytics */

        if (presenter != null)
        {
            presenter.invite();
        }
    }

    @OnClick(R.id.btnChat)
    public void onChatClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_CHAT);
        /* Analytics */

        if (presenter != null)
        {
            presenter.chat();
        }
    }

    @OnClick(R.id.btnInvitationResponse)
    public void onInvitationResponseClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_INVITATION_RESPONSE_NOT_GOING,null);
        /* Analytics */

        displayInvitationResponseDialog();
    }

    @OnClick(R.id.btnJoin)
    public void onJoinClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_JOIN,null);
        /* Analytics */

        if (presenter != null)
        {
            presenter.toggleRsvp();
        }
    }

    @Override
    public void onEdit()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_EDIT);
        /* Analytics */

        if (presenter != null)
        {
            presenter.edit();
        }
    }

    @Override
    public void onRsvpToggled()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_RSVP_TOGGLED, null);
        /* Analytics */

        if (presenter != null)
        {
            presenter.toggleRsvp();
        }
    }

    @Override
    public void onStatusClicked(String oldStatus)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_STATUS_ATTEMPT, GoogleAnalyticsConstants.LABEL_NORMAL);
        /* Analytics */

        StatusDialog.show(getActivity(), oldStatus, new StatusDialog.Listener()
        {
            @Override
            public void onStatusEntered(String status)
            {
                if (presenter != null)
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_STATUS_SUCCESS, GoogleAnalyticsConstants.LABEL_NORMAL);
                    /* Analytics */

                    presenter.setStatus(status);
                }
            }

            @Override
            public void onStatusCancelled()
            {
            }
        });
    }

    @Override
    public void onLastMinuteStatusClicked(String oldStatus)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_STATUS_ATTEMPT, GoogleAnalyticsConstants.LABEL_LAST_MOMENT);
        /* Analytics */

        LastMinuteStatusDialog
                .show(getActivity(), oldStatus, new LastMinuteStatusDialog.Listener()
                {
                    @Override
                    public void onLastMinuteStatusSuggestionSelected(String suggestion)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_STATUS_SUCCESS, GoogleAnalyticsConstants.LABEL_LAST_MOMENT_FROM_SUGGESTION);
                        /* Analytics */

                    }

                    @Override
                    public void onLastMinuteStatusEntered(String status)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS, GoogleAnalyticsConstants.ACTION_STATUS_SUCCESS, GoogleAnalyticsConstants.LABEL_LAST_MOMENT_MANUAL);
                        /* Analytics */

                        if (presenter != null)
                        {
                            presenter.setStatus(status);
                        }
                    }

                    @Override
                    public void onLastMinuteStatusCancelled()
                    {
                    }
                });

    }

    @Override
    public void onDescriptionClicked(String description)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_SHOW_DESCRIPTION,null);
        /* Analytics */

        DescriptionDialog.show(getActivity(), description);
    }

    @Override
    public void onNavigationClicked(Location location)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_NAVIGATE,null);
        /* Analytics */

        startActivity(GoogleService_.getInstance().getGoogleMapsIntent(location));
    }

    @Override
    public void onFriendsBubbleClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_OPEN,GoogleAnalyticsConstants.LABEL_FRIENDS_BUBBLE);
        /* Analytics */

        if (presenter != null)
        {
            presenter.invite();
        }
    }

    /* View Methods */
    @Override
    public void init(User sessionUser, Event event, boolean isLastMinute)
    {
        adapter = new EventDetailsAdapter(getActivity(), this, sessionUser, event, isLastMinute);
        rvEventDetails.setAdapter(adapter);
    }

    @Override
    public void displayAttendees(List<EventDetails.Attendee> attendees)
    {
        adapter.setAttendees(attendees);
    }

    @Override
    public void resetEvent(Event event)
    {
        adapter.resetEvent(event);
    }


    @Override
    public void showLoading()
    {
        isLoadingVisible = true;
        if (loading != null)
        {
            loading.setVisible(isLoadingVisible);
            setEditVisibility(false);
            setDeleteVisibility(false);
        }
    }

    @Override
    public void hideLoading()
    {
        isLoadingVisible = false;
        if (loading != null)
        {
            loading.setVisible(isLoadingVisible);
        }
    }

    @Override
    public void setEditVisibility(boolean isVisible)
    {
        isEditVisible = isVisible;
        if (edit != null)
        {
            edit.setVisible(isEditVisible);
        }
    }

    @Override
    public void setDeleteVisibility(boolean isVisible)
    {
        isDeleteVisible = isVisible;
        if (delete != null)
        {
            delete.setVisible(isDeleteVisible);
        }
    }

    @Override
    public void displayYayActions()
    {
        llEventActionsContainerNay.setVisibility(View.GONE);

        if (llEventActionsContainerYay.getVisibility() != View.VISIBLE)
        {
            ivShadow.setVisibility(View.VISIBLE);
            VisibilityAnimationUtil.expand(llEventActionsContainerYay, 200);
        }
    }

    @Override
    public void displayNayActions(boolean isInvited)
    {
        if (isInvited)
        {
            btnInvitationResponse.setVisibility(View.VISIBLE);
        }
        else
        {
            btnInvitationResponse.setVisibility(View.GONE);
        }

        llEventActionsContainerYay.setVisibility(View.GONE);

        if (llEventActionsContainerNay.getVisibility() != View.VISIBLE)
        {
            ivShadow.setVisibility(View.VISIBLE);
            VisibilityAnimationUtil.expand(llEventActionsContainerNay, 200);
        }
    }

    @Override
    public void navigateToInvite(String eventId)
    {
        screen.navigateToInviteScreen(eventId);
    }

    @Override
    public void navigateToChat(String eventId)
    {
        screen.navigateToChatScreen(eventId);
    }

    @Override
    public void navigateToEdit(Event event)
    {
        screen.navigateToEditScreen(event);
    }

    @Override
    public void navigateToHome()
    {
        screen.navigateToHomeScreen();
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvEventDetails.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void displayInvitationResponseDialog()
    {
        InvitationResponseDialog.show(getActivity(), new InvitationResponseDialog.Listener()
        {
            @Override
            public void onInvitationResponseSuggestionSelected(String suggestion)
            {
            }

            @Override
            public void onInvitationResponseEntered(String invitationResponse)
            {
                if (presenter != null)
                {
                    if(!TextUtils.isEmpty(invitationResponse))
                    {
                        presenter.sendInvitationResponse(invitationResponse);
                        SnackbarFactory.create(getActivity(), R.string.invitation_response_sent);
                    }
                }
            }

            @Override
            public void onInvitationResponseCancelled()
            {
            }
        });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_INVITATION_RESPONSE_DIALOG);
        /* Analytics */
    }

    private void displayDeleteDialog()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_DELETE,GoogleAnalyticsConstants.LABEL_ATTEMPT);
        /* Analytics */

        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());
        DefaultDialog.show(getActivity(),
                R.string.event_delete_title,
                R.string.event_delete_message,
                R.string.event_delete_positive_button,
                R.string.event_delete_negative_button,
                true,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_DELETE,GoogleAnalyticsConstants.LABEL_SUCCESS);
                        /* Analytics */

                        if (presenter != null)
                        {
                            presenter.delete();
                        }
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_DELETE,GoogleAnalyticsConstants.LABEL_CANCEL);
                        /* Analytics */
                    }
                });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_DISMISS_DIALOG);
        /* Analytics */
    }
}
