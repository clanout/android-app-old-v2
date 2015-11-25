package reaper.android.app.ui.screens.details.redesign;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.screens.chat.ChatFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.edit.EditEventFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.CircleTransform;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class EventDetailsFragment extends BaseFragment implements EventDetailsView
{
    private static final String ARG_EVENT = "arg_event";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat
            .forPattern("hh:mm a, dd MMM");

    /* UI Elements */
    View llCategoryIconContainer;
    ImageView ivCategoryIcon;
    TextView tvTitle;
    TextView tvType;
    TextView tvDescription;
    TextView tvTime;
    TextView tvLocation;
    View mivGoogleMap;

    View rlMeContainer;
    ImageView ivPic;
    TextView tvName;
    TextView tvStatus;
    TextView tvRsvp;
    Switch sRsvp;

    ProgressBar loading;
    RecyclerView rvAttendees;

    View llEventActionsContainer;
    Button btnInvite;
    Button btnChat;

    MenuItem edit;

    /* Presenter */
    EventDetailsPresenter presenter;

    /* Services */
    Bus bus;
    UserService userService;

    public static EventDetailsFragment newInstance(Event event)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);

        EventDetailsFragment fragment = new EventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Event event;
        try
        {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
            if (event == null)
            {
                throw new NullPointerException();
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Event cannot be null");
        }

        presenter = new EventDetailsPresenterImpl(bus, event);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details_, container, false);

        llCategoryIconContainer = view.findViewById(R.id.llCategoryIconContainer);
        ivCategoryIcon = (ImageView) view.findViewById(R.id.ivCategoryIcon);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvType = (TextView) view.findViewById(R.id.tvType);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        mivGoogleMap = view.findViewById(R.id.mivGoogleMap);

        rlMeContainer = view.findViewById(R.id.rlMeContainer);
        ivPic = (ImageView) view.findViewById(R.id.ivPic);
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        tvRsvp = (TextView) view.findViewById(R.id.tvRsvp);
        sRsvp = (Switch) view.findViewById(R.id.sRsvp);

        loading = (ProgressBar) view.findViewById(R.id.loading);
        rvAttendees = (RecyclerView) view.findViewById(R.id.rvAttendees);

        llEventActionsContainer = view.findViewById(R.id.llEventActionsContainer);
        btnInvite = (Button) view.findViewById(R.id.btnInvite);
        btnChat = (Button) view.findViewById(R.id.btnChat);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);

        initRecyclerView();

        btnInvite.setOnClickListener(new View.OnClickListener()
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

        btnChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.chat();
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        presenter.attachView(this);
        Timber.v(tvTitle.getText().toString() + " : onResume");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        presenter.detachView();
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
                tvType.setText(R.string.event_details_type_invite_only);
                break;

            case PUBLIC:
                tvType.setText(R.string.event_details_type_public);
                break;
        }

        tvTime.setText(event.getStartTime().toString(DATE_TIME_FORMATTER));

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
                    AnalyticsHelper.sendEvents(
                            GoogleAnalyticsConstants.BUTTON_CLICK,
                            GoogleAnalyticsConstants.EVENT_DETAILS_LOCATION_CLICKED,
                            userService.getActiveUserId());

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr="
                                    + location.getLatitude() + "," + location.getLongitude()));
                    startActivity(intent);
                }
            });
        }
        else
        {
            mivGoogleMap.setVisibility(View.GONE);
        }
    }

    @Override
    public void displayUserSummary(String userId, String name)
    {
        tvName.setText(name);

        Drawable placeHolder =
                MaterialDrawableBuilder
                        .with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                        .setColor(ContextCompat.getColor(getActivity(), R.color.light_grey))
                        .setSizeDp(24)
                        .build();

        Picasso.with(getActivity())
               .load(AppConstants.FACEBOOK_END_POINT + userId + "/picture?width=500")
               .placeholder(placeHolder)
               .transform(new CircleTransform())
               .into(ivPic);
    }

    @Override
    public void displayRsvp(boolean isGoing)
    {
        sRsvp.setOnCheckedChangeListener(null);

        sRsvp.setChecked(isGoing);

        if (isGoing)
        {
            tvRsvp.setText(R.string.rsvp_yes);
            tvRsvp.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
            VisibilityAnimationUtil.expand(llEventActionsContainer, 200);
        }
        else
        {
            tvRsvp.setText(R.string.rsvp_no);
            tvRsvp.setTextColor(ContextCompat.getColor(getActivity(), R.color._text_subtitle));
            VisibilityAnimationUtil.collapse(llEventActionsContainer, 200);
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
        Snackbar.make(getView(), R.string.message_rsvp_update_failure, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void disableRsvp()
    {
        sRsvp.setEnabled(false);
    }

    @Override
    public void displayStatusMessage(int statusType, String status)
    {
        switch (statusType)
        {
            case StatusType.NONE:
                tvStatus.setText("");
                break;

            case StatusType.INVITED:
                tvStatus.setText("Not Joining? Inform friends");
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.EMPTY:
                tvStatus.setText("** No Status **");
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.NORMAL:
                tvStatus.setText(status);
                tvStatus.setTextColor(ContextCompat
                        .getColor(getActivity(), R.color._text_subtitle));
                break;

            case StatusType.LAST_MINUTE_EMPTY:
                tvStatus.setText("Inform friends of your last minute ...");
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.LAST_MINUTE:
                tvStatus.setText(status);
                tvStatus.setTextColor(ContextCompat
                        .getColor(getActivity(), R.color._text_subtitle));
                break;
        }

        rlMeContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                presenter.onStatusClicked();
            }
        });
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
    public void displayAttendeeList(List<EventDetails.Attendee> attendees)
    {
        rvAttendees.setAdapter(new EventAttendeesAdapter(attendees, getActivity()));
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
    public void navigateToInviteScreen(Event event)
    {
        InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, event);
        bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, false);
        inviteUsersContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getActivity().getFragmentManager(), inviteUsersContainerFragment);
    }

    @Override
    public void navigateToChatScreen(String eventId, String eventTitle)
    {
        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_ID, eventId);
        bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_NAME, eventTitle);
        chatFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getActivity().getFragmentManager(), chatFragment);
    }

    @Override
    public void setEditActionState(boolean isVisible)
    {
        edit.setVisible(isVisible);
    }

    @Override
    public void displayEventFinalizedMessage()
    {
        Snackbar.make(getView(), R.string.cannot_edit_event_locked, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void navigateToEditScreen(Event event, EventDetails eventDetails)
    {
        FragmentUtils.changeFragment(getActivity().getFragmentManager(), EditEventFragment
                .newInstance(event, eventDetails));
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvAttendees.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAttendees
                .setAdapter(new EventAttendeesAdapter(new ArrayList<EventDetails.Attendee>(), getActivity()));
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
                    presenter.onEdit();
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

        edit.setVisible(false);
        edit.setIcon(MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                .setSizeDp(36)
                .build());
    }
}
