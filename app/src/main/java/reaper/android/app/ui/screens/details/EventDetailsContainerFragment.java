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
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.common.communicator.Communicator;

public class EventDetailsContainerFragment extends Fragment implements View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Servoces
    private EventService eventService;

    // Data
    private List<Event> events;
    private int activePosition;

    // UI Elements
    private ViewPager viewPager;
    private ImageButton rsvp;

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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            events = (List<Event>) savedInstanceState.get("events");
            activePosition = savedInstanceState.getInt("active_event");
        }
        else
        {
            Bundle bundle = getArguments();
            events = (List<Event>) bundle.get("events");
            activePosition = bundle.getInt("active_event");
        }

        if (events == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();

        eventService = new EventService(bus);

        pagerAdapter = new EventDetailsPagerAdapter(getChildFragmentManager(), events);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setPageTransformer(true, new ViewPagerTransformer(ViewPagerTransformer.TransformType.ZOOM));

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
        renderRsvpButton(events.get(activePosition).getRsvp());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("events", (ArrayList<Event>) events);
        outState.putInt("active_event", activePosition);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.ibtn_event_details_rsvp)
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
                            updateRsvp(Event.RSVP.YES);
                            break;
                        case R.id.menu_rsvp_maybe:
                            updateRsvp(Event.RSVP.MAYBE);
                            break;
                        case R.id.menu_rsvp_no:
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
    }

    private void updateRsvp(Event.RSVP newRsvp)
    {
        renderRsvpButton(newRsvp);

        Event event = events.get(activePosition);
        Event.RSVP oldRsvp = event.getRsvp();
        event.setRsvp(newRsvp);
        eventService.updateRsvp(event, oldRsvp);
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
    public void onGenericErrorTrigger(GenericErrorTrigger trigger)
    {
        ErrorCode code = trigger.getErrorCode();

        if (code == ErrorCode.RSVP_UPDATE_FAILURE)
        {
            Toast.makeText(getActivity(), R.string.message_rsvp_update_failure, Toast.LENGTH_LONG).show();
        }
    }
}
