package reaper.android.app.ui.screens.details;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.User;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.InvitationResponseDialog;
import reaper.android.app.ui.dialog.LastMinuteStatusDialog;
import reaper.android.app.ui.dialog.StatusDialog;
import reaper.android.app.ui.screens.details.mvp.EventDetailsPresenter;
import reaper.android.app.ui.screens.details.mvp.EventDetailsPresenterImpl;
import reaper.android.app.ui.screens.details.mvp.EventDetailsView;
import reaper.android.app.ui.util.CircleTransform;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FriendBubbles;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.VisibilityAnimationUtil;

public class EventDetailsFragment extends BaseFragment implements
        EventDetailsView
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
    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.tvType)
    TextView tvType;

    @Bind(R.id.tvDescription)
    TextView tvDescription;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvLocation)
    TextView tvLocation;

    @Bind(R.id.mivGoogleMap)
    View mivGoogleMap;

    @Bind(R.id.ivPic)
    ImageView ivPic;

    @Bind(R.id.tvName)
    TextView tvName;

    @Bind(R.id.ivStatus)
    ImageView ivStatus;

    @Bind(R.id.tvStatus)
    TextView tvStatus;

    @Bind(R.id.tvRsvp)
    TextView tvRsvp;

    @Bind(R.id.sRsvp)
    SwitchCompat sRsvp;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.llNoAttendees)
    View llNoAttendees;

    @Bind(R.id.tvInvite)
    TextView tvInvite;

    @Bind(R.id.friendBubbles)
    View friendBubbles;

    @Bind(R.id.rvAttendees)
    RecyclerView rvAttendees;

    @Bind(R.id.llEventActionsContainerYay)
    View llEventActionsContainerYay;

    @Bind(R.id.llEventActionsContainerNay)
    View llEventActionsContainerNay;

    @Bind(R.id.btnInvitationResponse)
    Button btnInvitationResponse;

    MenuItem edit;
    boolean isEditVisible;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        UserService userService = UserService.getInstance();
        Event event = (Event) getArguments().getSerializable(ARG_EVENT);
        presenter = new EventDetailsPresenterImpl(eventService, userService, event);
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

        FriendBubbles.show(getActivity(), friendBubbles);
        tvInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.invite();
                }
            }
        });

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
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (presenter != null)
                {
                    presenter.edit();
                }

                return true;
            }
        });

        presenter.requestEditActionState();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_details, menu);

        edit = menu.findItem(R.id.action_edit);

        edit.setVisible(isEditVisible);
        edit.setIcon(MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                .setColor(ContextCompat.getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build());
    }

    /* Listeners */
    @OnClick(R.id.btnInvite)
    public void onInviteClicked()
    {
        if (presenter != null)
        {
            presenter.invite();
        }
    }

    @OnClick(R.id.btnChat)
    public void onChatClicked()
    {
        if (presenter != null)
        {
            presenter.chat();
        }
    }

    @OnClick(R.id.btnInvitationResponse)
    public void onInvitationResponseClicked()
    {
        displayInvitationResponseDialog();
    }

    @OnClick(R.id.btnJoin)
    public void onJoinClicked()
    {
        if (presenter != null)
        {
            presenter.toggleRsvp();
        }
    }

    /* View Methods */
    @Override
    public void displayEventSummary(Event event)
    {
        tvTitle.setText(event.getTitle());

        EventCategory eventCategory = EventCategory.valueOf(event.getCategory());
        llCategoryIconContainer.setBackground(DrawableFactory.getIconBackground(eventCategory));
        ivCategoryIcon.setImageDrawable(DrawableFactory
                .get(eventCategory, Dimensions.EVENT_DETAILS_ICON_SIZE));

        switch (event.getType())
        {
            case INVITE_ONLY:
                tvType.setText(R.string.event_type_secret);
                break;

            case PUBLIC:
                tvType.setText(R.string.event_type_open);
                break;
        }

        tvTime.setText(event.getStartTime().toString(DateTimeUtil.DATE_TIME_FORMATTER));

        final Location location = event.getLocation();
        if (location.getName() == null || location.getName().isEmpty())
        {
            tvLocation.setText(R.string.event_details_no_location);
        }
        else
        {
            tvLocation.setText(location.getName());
        }

        if (location.getLatitude() != null && location.getLongitude() != null)
        {
            mivGoogleMap.setVisibility(View.VISIBLE);
            mivGoogleMap.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivity(GoogleService_.getInstance().getGoogleMapsIntent(location));
                }
            });
        }
        else
        {
            mivGoogleMap.setVisibility(View.GONE);
        }
    }

    @Override
    public void displayUserSummary(User user)
    {
        tvName.setText(user.getName());

        Drawable placeHolder =
                MaterialDrawableBuilder
                        .with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                        .setColor(ContextCompat.getColor(getActivity(), R.color.light_grey))
                        .setSizeDp(24)
                        .build();

        Picasso.with(getActivity())
               .load(user.getProfilePicUrl())
               .placeholder(placeHolder)
               .transform(new CircleTransform())
               .into(ivPic);
    }

    @Override
    public void displayDescription(String description)
    {
        if (description != null && !description.isEmpty())
        {
            tvDescription.setText(description);
        }
        else
        {
            tvDescription.setText(R.string.event_details_no_description);
        }
    }

    @Override
    public void displayRsvp(boolean isGoing, boolean isInvited)
    {
        tvRsvp.setVisibility(View.VISIBLE);
        sRsvp.setVisibility(View.VISIBLE);

        sRsvp.setOnCheckedChangeListener(null);

        sRsvp.setChecked(isGoing);

        if (isGoing)
        {
            tvRsvp.setText(R.string.rsvp_yes);
            tvRsvp.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));

            if (llEventActionsContainerNay.getVisibility() != View.GONE)
            {
                VisibilityAnimationUtil.collapse(llEventActionsContainerNay, 200);
            }

            if (llEventActionsContainerYay.getVisibility() != View.VISIBLE)
            {
                VisibilityAnimationUtil.expand(llEventActionsContainerYay, 200);
            }
        }
        else
        {
            tvRsvp.setText(R.string.rsvp_no);
            tvRsvp.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_subtitle));

            if (isInvited)
            {
                btnInvitationResponse.setVisibility(View.VISIBLE);
            }
            else
            {
                btnInvitationResponse.setVisibility(View.GONE);
            }

            if (llEventActionsContainerYay.getVisibility() != View.GONE)
            {
                VisibilityAnimationUtil.collapse(llEventActionsContainerYay, 200);
            }

            if (llEventActionsContainerNay.getVisibility() != View.VISIBLE)
            {
                VisibilityAnimationUtil.expand(llEventActionsContainerNay, 200);
            }
        }

        sRsvp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (presenter != null)
                {
                    presenter.toggleRsvp();
                }
            }
        });
    }

    @Override
    public void displayRsvpError()
    {
        SnackbarFactory.create(getActivity(), R.string.error_rsvp_update);
    }

    @Override
    public void disableRsvp()
    {
        tvRsvp.setVisibility(View.GONE);
        sRsvp.setVisibility(View.GONE);
    }

    @Override
    public void hideStatus()
    {
        ivStatus.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
        tvStatus.setOnClickListener(null);
    }

    @Override
    public void displayStatus(final String status)
    {
        ivStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);

        Drawable drawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.TOOLTIP_EDIT)
                .setColor(ContextCompat.getColor(getActivity(), R.color.accent))
                .setSizeDp(18)
                .build();

        ivStatus.setImageDrawable(drawable);

        if (TextUtils.isEmpty(status))
        {
            tvStatus.setText(R.string.label_status);
            tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
        }
        else
        {
            tvStatus.setText(status);
            tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_subtitle));
        }

        tvStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                StatusDialog.show(getActivity(), status, new StatusDialog.Listener()
                {
                    @Override
                    public void onStatusEntered(String status)
                    {
                        if (presenter != null)
                        {
                            presenter.setStatus(status);
                        }
                    }

                    @Override
                    public void onStatusCancelled()
                    {
                    }
                });
            }
        });
    }

    @Override
    public void displayLastMinuteStatus(final String status)
    {
        ivStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);

        Drawable drawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CLOCK_FAST)
                .setColor(ContextCompat.getColor(getActivity(), R.color.accent))
                .setSizeDp(18)
                .build();

        ivStatus.setImageDrawable(drawable);

        if (TextUtils.isEmpty(status))
        {
            tvStatus.setText(R.string.label_status_last_moment);
            tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
        }
        else
        {
            tvStatus.setText(status);
            tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_subtitle));
        }

        tvStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LastMinuteStatusDialog
                        .show(getActivity(), status, new LastMinuteStatusDialog.Listener()
                        {
                            @Override
                            public void onLastMinuteStatusSuggestionSelected(String suggestion)
                            {
                            }

                            @Override
                            public void onLastMinuteStatusEntered(String status)
                            {
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
        });
    }

    @Override
    public void displayAttendeeList(List<EventDetails.Attendee> attendees)
    {
        if (attendees.isEmpty())
        {
            llNoAttendees.setVisibility(View.VISIBLE);
            rvAttendees.setVisibility(View.GONE);
        }
        else
        {
            rvAttendees.setAdapter(new EventAttendeesAdapter(attendees, getActivity()));
            llNoAttendees.setVisibility(View.GONE);
            rvAttendees.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showAttendeeLoading()
    {
        loading.setVisibility(View.VISIBLE);
    }

    public void hideAttendeeLoading()
    {
        loading.setVisibility(View.GONE);
    }

    @Override
    public void setEditActionState(boolean isVisible)
    {
        if (edit != null)
        {
            edit.setVisible(isVisible);
        }

        isEditVisible = isVisible;
    }

    @Override
    public void displayEventFinalizedMessage()
    {
        SnackbarFactory.create(getActivity(), R.string.error_edit_finalized);
    }

    @Override
    public void navigateToInviteScreen(String eventId)
    {
        screen.navigateToInviteScreen(eventId);
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        screen.navigateToChatScreen(eventId);
    }

    @Override
    public void navigateToEditScreen(Event event, EventDetails eventDetails)
    {
        screen.navigateToEditScreen(event, eventDetails);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvAttendees.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAttendees
                .setAdapter(new EventAttendeesAdapter(new ArrayList<EventDetails.Attendee>(), getActivity()));
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
                if(presenter != null)
                {
                    presenter.sendInvitationResponse(invitationResponse);
                    SnackbarFactory.create(getActivity(), R.string.invitation_response_sent);
                }
            }

            @Override
            public void onInvitationResponseCancelled()
            {
            }
        });
    }
}
