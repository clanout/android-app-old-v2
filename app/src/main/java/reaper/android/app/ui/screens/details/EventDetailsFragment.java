package reaper.android.app.ui.screens.details;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.chat.ChatActivity;
import reaper.android.app.ui.screens.edit.EditEventActivity;
import reaper.android.app.ui.screens.invite.InviteActivity;
import reaper.android.app.ui.util.CircleTransform;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class EventDetailsFragment extends BaseFragment implements EventDetailsView
{
    private static final String ARG_EVENT = "arg_event";
    private static final String ARG_LAST_MINUTE_DIALOG = "arg_last_minute_dialog";

    private static final int FLAG_DEFAULT = 0;
    public static final int FLAG_LAST_MINUTE_STATUS = 1;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat
            .forPattern("hh:mm a, dd MMM (EEEE)");

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
    SwitchCompat sRsvp;

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
    GenericCache genericCache;

    private Event event;

    public static EventDetailsFragment newInstance(Event event)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putInt(ARG_LAST_MINUTE_DIALOG, FLAG_DEFAULT);

        EventDetailsFragment fragment = new EventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDetailsFragment newInstance(Event event, int flag)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putInt(ARG_LAST_MINUTE_DIALOG, flag);

        EventDetailsFragment fragment = new EventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EVENT_DETAILS_FRAGMENT);
        setHasOptionsMenu(true);

        int flag;

        try
        {
            flag = getArguments().getInt(ARG_LAST_MINUTE_DIALOG, FLAG_DEFAULT);
            getArguments().remove(ARG_LAST_MINUTE_DIALOG);

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

        presenter = new EventDetailsPresenterImpl(bus, event, flag);
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
        sRsvp = (SwitchCompat) view.findViewById(R.id.sRsvp);

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
        userService = UserService.getInstance();
        genericCache = CacheManager.getGenericCache();

        initRecyclerView();

        btnInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (presenter != null)
                {
                    if (genericCache.get(GenericCacheKeys.READ_CONTACT_PERMISSION_DENIED) == null)
                    {
                        handleReadContactsPermission();
                    }
                    else
                    {
                        presenter.invite();
                    }
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
                tvType.setText(R.string.event_type_secret);
                break;

            case PUBLIC:
                tvType.setText(R.string.event_type_open);
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
                            userService.getSessionUserId());

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
               .load(AppConstants.BASE_URL_FACEBOOK_API + userId + "/picture?width=500")
               .placeholder(placeHolder)
               .transform(new CircleTransform())
               .into(ivPic);
    }

    @Override
    public void displayRsvp(boolean isGoing)
    {
        tvRsvp.setVisibility(View.VISIBLE);
        sRsvp.setVisibility(View.VISIBLE);

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
            tvRsvp.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_subtitle));
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
        SnackbarFactory.create(getActivity(), R.string.error_rsvp_update);
    }

    @Override
    public void disableRsvp()
    {
        tvRsvp.setVisibility(View.GONE);
        sRsvp.setVisibility(View.GONE);
    }

    @Override
    public void displayStatusMessage(int statusType, String status)
    {
        Timber.v(">>>> qwerty : " + status);
        switch (statusType)
        {
            case StatusType.NONE:
                tvStatus.setText("");
                break;

            case StatusType.INVITED:
                tvStatus.setText(R.string.label_invited_not_going);
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.EMPTY:
                tvStatus.setText(R.string.label_status);
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.NORMAL:
                tvStatus.setText(status);
                tvStatus.setTextColor(ContextCompat
                        .getColor(getActivity(), R.color.text_subtitle));
                break;

            case StatusType.LAST_MINUTE_EMPTY:
                tvStatus.setText(R.string.label_status_last_moment);
                tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                break;

            case StatusType.LAST_MINUTE:
                tvStatus.setText(status);
                tvStatus.setTextColor(ContextCompat
                        .getColor(getActivity(), R.color.text_subtitle));
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
    public void displayInvitationResponseDialog(final String eventId, final String userId)
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL,
                GoogleAnalyticsConstants.INVITATION_RESPONSE_DIALOG_OPENED,
                userService.getSessionUserId());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_invitation_response, null);
        builder.setView(dialogView);

        final EditText message = (EditText) dialogView.findViewById(R.id.etInvitationResponse);
        ListView list = (ListView) dialogView.findViewById(R.id.lvInvitationResponse);

        final List<String> responseList = new ArrayList<>();
        responseList.add("Not in a mood");
        responseList.add("Busy with other plans");
        responseList.add("Thanks. Can't make it this time");
        responseList.add("Staying in bed. Waiting for aliens to pick me up");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_list_view_dialog, responseList);

        list.setAdapter(statusAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK,
                        GoogleAnalyticsConstants.INVITAION_RESPONSE_TEMPLATE_CHOSEN,
                        "user:" + userId + ";template:" + responseList.get(position));

                message.setText(responseList.get(position));
            }
        });

        builder.setPositiveButton(R.string.invitation_response_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                if (message.getText().toString() != null)
                {
                    if (!message.getText().toString().isEmpty())
                    {
                        presenter.setStatus(message.getText().toString());

                        SnackbarFactory.create(getActivity(), R.string.invitation_response_sent);
                        dialog.dismiss();
                    }
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void displayUpdateStatusDialog(String eventId, final String userId, String oldStatus, boolean isLastMinute)
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL,
                GoogleAnalyticsConstants.STATUS_DIALOG_OPENED,
                userId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_status, null);
        builder.setView(dialogView);

        TextView message = (TextView) dialogView.findViewById(R.id.tvMessage);
        final EditText status = (EditText) dialogView.findViewById(R.id.etStatus);
        ListView list = (ListView) dialogView.findViewById(R.id.lvStatus);

        final List<String> statusList = new ArrayList<>();
        statusList.add("On my way");
        statusList.add("Running late");
        statusList.add("Sorry, changed my mind");
        statusList.add("Yippie-kai yay!");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_list_view_dialog, statusList);
        list.setAdapter(statusAdapter);

        if (isLastMinute)
        {
            list.setVisibility(View.VISIBLE);
            message.setText(R.string.status_dialog_message_last_minute);
        }
        else
        {
            list.setVisibility(View.GONE);
            message.setText(R.string.status_dialog_message);
        }

        if (oldStatus == null || oldStatus.isEmpty())
        {
            status.setHint(R.string.status_default);
        }
        else
        {
            status.setText(oldStatus);
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK,
                                GoogleAnalyticsConstants.STATUS_TEMPLATE_CHOSEN,
                                "user:" + userId + ";template:" + statusList.get(position));

                status.setText(statusList.get(position));
            }
        });


        builder.setPositiveButton(R.string.status_dialog_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                presenter.setStatus(status.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        startActivity(InviteActivity.callingIntent(getActivity(), false, event.getId()));
    }

    @Override
    public void navigateToChatScreen(String eventId, String eventTitle)
    {
        startActivity(ChatActivity.callingIntent(getActivity(), eventId));
    }

    @Override
    public void setEditActionState(boolean isVisible)
    {
        edit.setVisible(isVisible);
    }

    @Override
    public void displayEventFinalizedMessage()
    {
        SnackbarFactory.create(getActivity(), R.string.error_edit_finalized);
    }

    @Override
    public void navigateToEditScreen(Event event, EventDetails eventDetails)
    {
//        FragmentUtils.changeFragment(getActivity().getFragmentManager(), EditEventFragment
//                .newInstance(event, eventDetails));
        startActivity(EditEventActivity.callingIntent(getActivity(), event, eventDetails));
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
                .setColor(ContextCompat.getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build());
    }

    private void handleReadContactsPermission()
    {

        Log.d("APP", "inside handle Read contacts");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            try
            {

                Log.d("APP", "Marshmallow --- 1 ");

                Dexter.checkPermission(new PermissionListener()
                {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
                    {

                        Log.d("APP", "inside handle Read contacts --- permission granted");
                        navigateToInviteScreen(event);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
                    {

                        Log.d("APP", "inside handle Read contacts --- permission denied");

                        if (permissionDeniedResponse.isPermanentlyDenied())
                        {

                            displayContactsPermissionRequiredDialogPermanentlyDeclinedCase();
                        }
                        else
                        {

                            displayContactsPermissionRequiredDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken)
                    {

                        permissionToken.continuePermissionRequest();
                    }
                }, Manifest.permission.READ_CONTACTS);
            }
            catch (Exception e)
            {
                Log.d("APP", "inside handle Read contacts --- exception");
            }
        }
        else
        {

            presenter.invite();
        }
    }

    private void displayContactsPermissionRequiredDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(R.string.read_contacts_permission_required_message);
        builder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    try
                    {

                        Log.d("APP", "Marshmallow ---- 2");

                        Dexter.checkPermission(new PermissionListener()
                        {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
                            {

                                Log.d("APP", "2 ---- permission granted");
                                navigateToInviteScreen(event);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
                            {

                                Log.d("APP", "2 ---- permission denied");

                                genericCache
                                        .put(GenericCacheKeys.READ_CONTACT_PERMISSION_DENIED, true);
                                navigateToInviteScreen(event);
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken)
                            {

                                permissionToken.continuePermissionRequest();
                            }
                        }, Manifest.permission.READ_CONTACTS);
                    }
                    catch (Exception e)
                    {

                    }
                }
                else
                {

                    presenter.invite();
                }
            }
        });

        builder.create().show();
    }

    private void displayContactsPermissionRequiredDialogPermanentlyDeclinedCase()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(R.string.read_contacts_permission_required_message);
        builder.setPositiveButton("TAKE ME TO SETTINGS", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                genericCache.put(GenericCacheKeys.READ_CONTACT_PERMISSION_DENIED, true);
                navigateToInviteScreen(event);
            }
        });

        builder.create().show();
    }


    private void goToSettings()
    {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
