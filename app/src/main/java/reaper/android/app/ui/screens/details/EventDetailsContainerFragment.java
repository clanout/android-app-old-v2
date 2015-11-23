package reaper.android.app.ui.screens.details;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.event.ChangeAttendeeListTrigger;
import reaper.android.app.trigger.event.EventRsvpNotChangedTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.chat.ChatFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class EventDetailsContainerFragment extends BaseFragment implements View.OnClickListener {
    private android.app.FragmentManager fragmentManager;

    // Services
    private EventService eventService;
    private UserService userService;

    private GenericCache genericCache;
    private Bus bus;

    // Data
    private ArrayList<Event> events;
    private int activePosition;

    // UI Elements
    private ViewPager viewPager;
    private Button rsvp, invite, chat;
    private Drawable goingDrawable, maybeDrawable, notGoingDrawable, inviteDrawable, chatDrawable;
    private Toolbar toolbar;

    private PagerAdapter pagerAdapter;

    private boolean shoulPopupStatusDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_event_details_container);
        rsvp = (Button) view.findViewById(R.id.btn_event_details_rsvp);
        invite = (Button) view.findViewById(R.id.btn_event_details_invite);
        chat = (Button) view.findViewById(R.id.btn_event_details_chat);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_event_details_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            events = (ArrayList<Event>) savedInstanceState.get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = savedInstanceState.getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
        } else {
            Bundle bundle = getArguments();
            events = (ArrayList<Event>) bundle.get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = bundle.getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
            shoulPopupStatusDialog = bundle.getBoolean(BundleKeys.POPUP_STATUS_DIALOG);
        }

        if (events == null) {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getFragmentManager();
        eventService = new EventService(bus);
        userService = new UserService(bus);
        genericCache = CacheManager.getGenericCache();

        generateDrawables();

        chat.setText("Chat");
        invite.setText("Invite");

        pagerAdapter = new EventDetailsPagerAdapter(getChildFragmentManager(), events, activePosition, shoulPopupStatusDialog);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(activePosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                activePosition = i;

                setActionBarTitle();
                renderRsvpButton(events.get(activePosition).getRsvp());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        rsvp.setOnClickListener(this);
        invite.setOnClickListener(this);
        chat.setOnClickListener(this);

        renderRsvpButton(events.get(activePosition).getRsvp());
    }

    private void generateDrawables() {
        goingDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();

        maybeDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.HELP)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();

        notGoingDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CLOSE)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();

        chatDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();

        inviteDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EVENT_DETAILS_CONTAINER_FRAGMENT);

        setActionBarTitle();
        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.EVENT_DETAILS_CONTAINER);

        bus.register(this);

        handleInAppRating();
    }

    @Override
    public void onPause() {
        super.onPause();

        bus.unregister(this);
    }

    private void setActionBarTitle()
    {
        switch (events.get(activePosition).getCategory()) {
            case "CAFE":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Cafe");
                break;
            case "MOVIES":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Movie");
                break;
            case "SHOPPING":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Shopping");
                break;
            case "SPORTS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Sports");
                break;
            case "INDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Indoors");
                break;
            case "EAT_OUT":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Eat Out");
                break;
            case "DRINKS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Drinks");
                break;
            case "OUTDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Outdoors");
                break;
            case "GENERAL":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("General");
                break;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        outState.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_event_details_rsvp) {
            if (events.get(activePosition).getOrganizerId().equals(userService.getActiveUserId())) {

                Snackbar.make(getView(), R.string.cannot_change_rsvp, Snackbar.LENGTH_LONG).show();

            } else {
                PopupMenu rsvpMenu = new PopupMenu(getActivity(), rsvp);
                rsvpMenu.getMenuInflater().inflate(R.menu.popup_rsvp, rsvpMenu.getMenu());

                rsvpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int menuItemId = menuItem.getItemId();
                        switch (menuItemId) {
                            case R.id.menu_rsvp_yes:
                                bus.post(new ChangeAttendeeListTrigger(Event.RSVP.YES, events.get(activePosition).getId()));
                                updateRsvp(Event.RSVP.YES);
                                break;
                            case R.id.menu_rsvp_maybe:
                                bus.post(new ChangeAttendeeListTrigger(Event.RSVP.MAYBE, events.get(activePosition).getId()));
                                updateRsvp(Event.RSVP.MAYBE);
                                break;
                            case R.id.menu_rsvp_no:
                                bus.post(new ChangeAttendeeListTrigger(Event.RSVP.NO, events.get(activePosition).getId()));
                                updateRsvp(Event.RSVP.NO);
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                        return false;
                    }
                });

                rsvpMenu.show();
            }
        } else if (view.getId() == R.id.btn_event_details_invite) {
            if (EventUtils.canInviteFriends(events.get(activePosition))) {
                InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, events.get(activePosition));
                bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, false);
                inviteUsersContainerFragment.setArguments(bundle);
                FragmentUtils.changeFragment(fragmentManager, inviteUsersContainerFragment);
            } else {
                Snackbar.make(getView(), R.string.cannot_invite, Snackbar.LENGTH_LONG).show();
            }
        } else if (view.getId() == R.id.btn_event_details_chat) {
            if (EventUtils.canViewChat(events.get(activePosition))) {
                ChatFragment chatFragment = new ChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_ID, events.get(activePosition).getId());
                bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_NAME, events.get(activePosition).getTitle());
                chatFragment.setArguments(bundle);
                FragmentUtils.changeFragment(fragmentManager, chatFragment);

            } else {
                Snackbar.make(getView(), R.string.cannot_chat, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            ((MainActivity)getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateRsvp(Event.RSVP newRsvp) {

        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.RSVP_UPDATED, "user - " + userService.getActiveUserId() + "event - " + events.get(activePosition).getId() + "rsvp - " + newRsvp.toString());

        renderRsvpButton(newRsvp);

        Event.RSVP oldRsvp = events.get(activePosition).getRsvp();
        events.get(activePosition).setRsvp(newRsvp);

        eventService.updateRsvp(events.get(activePosition), oldRsvp, false);

    }

    private void renderRsvpButton(Event.RSVP rsvpStatus) {
        switch (rsvpStatus) {
            case YES:
                rsvp.setText(R.string.rsvp_yes);
                break;
            case MAYBE:
                rsvp.setText(R.string.rsvp_maybe);
                break;
            case NO:
                rsvp.setText(R.string.rsvp_no);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Subscribe
    public void onRsvpNotChanged(EventRsvpNotChangedTrigger trigger) {
        if (trigger.getEventId().equals(events.get(activePosition).getId())) {

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.RSVP_UPDATE_FAILURE, "user - " + userService.getActiveUserId() + "event - " + events.get(activePosition).getId() + "rsvp - " + trigger.getOldRsvp().toString());

            events.get(activePosition).setRsvp(trigger.getOldRsvp());
            Snackbar.make(getView(), R.string.message_rsvp_update_failure, Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void backPressed(BackPressedTrigger trigger) {

        if (trigger.getActiveFragment().equals(BackstackTags.EVENT_DETAILS_CONTAINER)) {

            HomeFragment homeFragment = new HomeFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, events);
            bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);

            homeFragment.setArguments(bundle);

            FragmentUtils.changeFragment(getFragmentManager(), homeFragment);
        }
    }

    private void handleInAppRating() {

      if(genericCache.get(CacheKeys.HAS_GIVEN_FEEDBACK) == null)
      {
          int timesAppOpened;

          try{
              timesAppOpened = Integer.parseInt(genericCache.get(CacheKeys.TIMES_APPLICATION_OPENED));
          }catch (Exception e)
          {
              timesAppOpened = 0;
          }

          if(timesAppOpened > 10)
          {

              double random = Math.random();

              if(random < 0.1) {
                  displayShareFeedbackDialog();
              }
          }
      }
    }

    private void displayShareFeedbackDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle("Feedback");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_share_feedback, null);
        builder.setView(dialogView);

        final EditText commentMessage = (EditText) dialogView.findViewById(R.id.et_alert_dialog_share_feedback_comment);
        RadioButton bug = (RadioButton) dialogView.findViewById(R.id.rb_share_feedback_bug);
        RadioButton newFeature = (RadioButton) dialogView.findViewById(R.id.rb_share_feedback_new_feature);
        RadioButton suggestion = (RadioButton) dialogView.findViewById(R.id.rb_share_feedback_other);
        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.rg_share_feedback);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setNegativeButton("REMIND LATER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int type = 0;

                switch (radioGroup.getCheckedRadioButtonId())
                {
                    case R.id.rb_share_feedback_bug:
                        type = 0;
                        break;
                    case R.id.rb_share_feedback_new_feature:
                        type = 1;
                        break;
                    case R.id.rb_share_feedback_other:
                        type = 2;
                        break;
                }

                String comment = commentMessage.getText().toString();
                Boolean wantToCloseDialog = false;

                if (comment == null || comment.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.empty_rating, Toast.LENGTH_LONG).show();
                    wantToCloseDialog = false;
                } else {

                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.FEEDBACK_SHARED, userService.getActiveUserId());

                    userService.shareFeedback(type, comment);

                    genericCache.put(CacheKeys.HAS_GIVEN_FEEDBACK, true);

                    Toast.makeText(getActivity(), R.string.feedback_submitted, Toast.LENGTH_LONG).show();
                    wantToCloseDialog = true;
                }

                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });
    }
}
