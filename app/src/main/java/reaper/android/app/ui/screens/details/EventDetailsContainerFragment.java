package reaper.android.app.ui.screens.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.ChangeAttendeeListTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventRsvpNotChangedTrigger;
import reaper.android.app.ui.screens.chat.ChatFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.common.communicator.Communicator;

public class EventDetailsContainerFragment extends Fragment implements View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private EventService eventService;
    private UserService userService;

    private GenericCache genericCache;

    // Data
    private List<Event> events;
    private int activePosition;

    // UI Elements
    private ViewPager viewPager;
    private ImageButton rsvp, invite, chat;

    private PagerAdapter pagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_event_details_container);
        rsvp = (ImageButton) view.findViewById(R.id.ibtn_event_details_rsvp);
        invite = (ImageButton) view.findViewById(R.id.ibtn_event_details_invite);
        chat = (ImageButton) view.findViewById(R.id.ibtn_event_details_chat);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            events = (List<Event>) savedInstanceState.get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = savedInstanceState.getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
        } else
        {
            Bundle bundle = getArguments();
            events = (List<Event>) bundle.get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = bundle.getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
        }

        if (events == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        eventService = new EventService(bus);
        userService = new UserService(bus);
        genericCache = CacheManager.getGenericCache();

        pagerAdapter = new EventDetailsPagerAdapter(getChildFragmentManager(), events);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(activePosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                activePosition = i;
                renderRsvpButton(events.get(activePosition).getRsvp());
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });

        rsvp.setOnClickListener(this);
        invite.setOnClickListener(this);
        chat.setOnClickListener(this);

        renderRsvpButton(events.get(activePosition).getRsvp());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.EVENT_DETAILS_CONTAINER);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        outState.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.ibtn_event_details_rsvp)
        {
            if(events.get(activePosition).getOrganizerId().equals(userService.getActiveUserId())){

                Toast.makeText(getActivity(), R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();

            }else
            {
                PopupMenu rsvpMenu = new PopupMenu(getActivity(), rsvp);
                rsvpMenu.getMenuInflater().inflate(R.menu.popup_rsvp, rsvpMenu.getMenu());

                rsvpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        int menuItemId = menuItem.getItemId();
                        switch (menuItemId)
                        {
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
        } else if (view.getId() == R.id.ibtn_event_details_invite)
        {
            if (EventUtils.canInviteFriends(events.get(activePosition)))
            {
                InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT_ID, events.get(activePosition).getId());
                bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, false);
                inviteUsersContainerFragment.setArguments(bundle);
                FragmentUtils.changeFragment(fragmentManager, inviteUsersContainerFragment);
            } else
            {
                Toast.makeText(getActivity(), R.string.cannot_invite, Toast.LENGTH_LONG).show();
            }
        } else if (view.getId() == R.id.ibtn_event_details_chat)
        {
            if (EventUtils.canViewChat(events.get(activePosition)))
            {
                ChatFragment chatFragment = new ChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_ID, events.get(activePosition).getId());
                chatFragment.setArguments(bundle);
                FragmentUtils.changeFragment(fragmentManager, chatFragment);
            } else
            {
                Toast.makeText(getActivity(), R.string.cannot_chat, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateRsvp(Event.RSVP newRsvp)
    {
        renderRsvpButton(newRsvp);

        Event.RSVP oldRsvp = events.get(activePosition).getRsvp();
        events.get(activePosition).setRsvp(newRsvp);

        eventService.updateRsvp(events.get(activePosition), oldRsvp, false);

    }

    private void renderRsvpButton(Event.RSVP rsvpStatus)
    {
        switch (rsvpStatus)
        {
            case YES:
                rsvp.setImageResource(R.drawable.ic_check_circle_white_24dp);
                break;
            case MAYBE:
                rsvp.setImageResource(R.drawable.ic_help_white_24dp);
                break;
            case NO:
                rsvp.setImageResource(R.drawable.ic_cancel_white_24dp);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Subscribe
    public void onRsvpNotChanged(EventRsvpNotChangedTrigger trigger)
    {
        if (trigger.getEventId().equals(events.get(activePosition).getId()))
        {
            events.get(activePosition).setRsvp(trigger.getOldRsvp());
            Toast.makeText(getActivity(), R.string.message_rsvp_update_failure, Toast.LENGTH_LONG).show();
        }
    }
}
