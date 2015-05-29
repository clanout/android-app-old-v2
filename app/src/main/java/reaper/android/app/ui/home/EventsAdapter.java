package reaper.android.app.ui.home;

import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.trigger.EventClickTrigger;
import reaper.android.app.trigger.RsvpChangeTrigger;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder>
{
    private Bus bus;

    // Data
    private List<Event> events;
    private List<String> updates;
    private List<String> chatUpdates;

    private List<SwipedState> state;

    public EventsAdapter(Bus bus, List<Event> events, List<String> updates, List<String> chatUpdates)
    {
        this.bus = bus;
        this.events = events;
        this.updates = updates;
        this.chatUpdates = chatUpdates;

        state = new ArrayList<>();
        for (int i = 0; i < events.size(); i++)
        {
            state.add(SwipedState.SHOWING_PRIMARY_CONTENT);
        }
    }

    public void setUpdates(List<String> updates)
    {
        this.chatUpdates = updates;
    }

    public void setChatUpdates(List<String> chatUpdates)
    {
        this.chatUpdates = chatUpdates;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Create a new view which is basically just a ViewPager in this case
        ViewPager v = (ViewPager) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);

        //Perhaps the first most crucial part. The ViewPager loses its width information when it is put
        //inside a RecyclerView. It needs to be explicitly resized, in this case to the width of the
        //screen. The height must be provided as a fixed value.
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        v.getLayoutParams().width = displayMetrics.widthPixels;
        v.requestLayout();

        EventViewHolder vh = new EventViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, final int position)
    {
        final ViewPager viewPager = holder.viewPager;
        final Event event = events.get(position);
        boolean isUpdated = false;
        boolean isChatUpdated = false;
        if (updates.contains(event.getId()))
        {
            isUpdated = true;
        }
        if (chatUpdates.contains(event.getId()))
        {
            isChatUpdated = true;
        }

        viewPager.setCurrentItem(state.get(position).getPosition());
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            int previousPagePosition = 0;

            @Override
            public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels)
            {
                if (pagePosition == previousPagePosition)
                {
                    return;
                }

                switch (pagePosition)
                {
                    case 0:
                        state.set(position, SwipedState.SHOWING_PRIMARY_CONTENT);
                        break;
                    case 1:
                        state.set(position, SwipedState.SHOWING_SECONDARY_CONTENT);
                        break;
                }
                previousPagePosition = pagePosition;
            }

            @Override
            public void onPageSelected(int pagePosition)
            {
                //This method keep incorrectly firing as the RecyclerView scrolls.
                //Use the one above instead
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        // Title
        holder.title.setText(event.getTitle());

        // Icon
        holder.eventIcon.setImageResource(R.drawable.ic_local_bar_black_36dp);

        // Date, Time and Location
        DateTime dateTime = event.getStartTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM dd");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        holder.date.setText(dateTime.toString(dateFormatter));
        if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
        {
            holder.timeLocation.setText(dateTime.toString(timeFormatter) + ", (Location Not Specified)");
        }
        else
        {
            holder.timeLocation.setText(dateTime.toString(timeFormatter) + ", " + event.getLocation().getName());
        }

        // Friends Attending
        if (event.getFriendCount() == 0)
        {
            holder.attendees.setText("No friends are going");
        }
        else if (event.getFriendCount() == 1)
        {
            holder.attendees.setText("1 friend is going");
        }
        else
        {
            holder.attendees.setText(event.getFriendCount() + " friends are going");
        }

        // RSVP
        if (event.getRsvp() == Event.RSVP.YES)
        {
            holder.rsvpIcon.setVisibility(View.VISIBLE);
            holder.rsvpIcon.setImageResource(R.drawable.ic_check_circle_black_24dp);
        }
        else if (event.getRsvp() == Event.RSVP.MAYBE)
        {
            holder.rsvpIcon.setVisibility(View.VISIBLE);
            holder.rsvpIcon.setImageResource(R.drawable.ic_help_black_24dp);
        }
        else
        {
            holder.rsvpIcon.setVisibility(View.INVISIBLE);
        }

        // Chat Updates
        if (isChatUpdated)
        {
            holder.chatIcon.setVisibility(View.VISIBLE);
            holder.chatIcon.setImageResource(R.drawable.ic_chat_black_18dp);
        }
        else
        {
            holder.chatIcon.setVisibility(View.INVISIBLE);
        }

        // Event Updates
        if (isUpdated)
        {
            holder.updatesIcon.setVisibility(View.VISIBLE);
            holder.updatesIcon.setImageResource(R.drawable.ic_info_black_18dp);
        }
        else
        {
            holder.updatesIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount()
    {
        return events.size();
    }

    private enum SwipedState
    {
        SHOWING_PRIMARY_CONTENT(0),
        SHOWING_SECONDARY_CONTENT(1);

        private int position;

        private SwipedState(int position)
        {
            this.position = position;
        }

        public int getPosition()
        {
            return position;
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        private ViewPager viewPager;
        private CardView cardView;
        private ImageView eventIcon, rsvpIcon, chatIcon, updatesIcon;
        private TextView title, timeLocation, attendees, date;
        private Button going, mayBe, notGoing;

        public EventViewHolder(View itemView)
        {
            super(itemView);

            viewPager = (ViewPager) itemView;
            viewPager.setAdapter(new EventViewPagerAdapter());

            cardView = (CardView) itemView.findViewById(R.id.cv_list_item_event);
            eventIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_icon);
            rsvpIcon = (ImageView) itemView.findViewById(R.id.iv_list_litem_event_rsvp);
            chatIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_chat);
            updatesIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_updates);
            title = (TextView) itemView.findViewById(R.id.tv_list_item_event_title);
            timeLocation = (TextView) itemView.findViewById(R.id.tv_list_item_event_time_location);
            attendees = (TextView) itemView.findViewById(R.id.tv_list_item_event_attendees);
            date = (TextView) itemView.findViewById(R.id.tv_list_item_event_date);
            going = (Button) itemView.findViewById(R.id.btn_list_item_event_going);
            mayBe = (Button) itemView.findViewById(R.id.btn_list_item_event_maybe);
            notGoing = (Button) itemView.findViewById(R.id.btn_list_item_event_not_going);

            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    bus.post(new EventClickTrigger(events.get(getPosition())));
                }
            });

            going.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    rsvpIcon.setVisibility(View.VISIBLE);
                    rsvpIcon.setImageResource(R.drawable.ic_check_circle_black_24dp);

                    state.set(getPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getPosition()).getPosition());

                    Event event = events.get(getPosition());
                    event.setRsvp(Event.RSVP.YES);
                    bus.post(new RsvpChangeTrigger(event, Event.RSVP.YES));
                }
            });

            mayBe.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    rsvpIcon.setVisibility(View.VISIBLE);
                    rsvpIcon.setImageResource(R.drawable.ic_help_black_24dp);

                    state.set(getPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getPosition()).getPosition());

                    Event event = events.get(getPosition());
                    event.setRsvp(Event.RSVP.MAYBE);
                    bus.post(new RsvpChangeTrigger(event, Event.RSVP.MAYBE));
                }
            });

            notGoing.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    rsvpIcon.setVisibility(View.INVISIBLE);

                    state.set(getPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getPosition()).getPosition());

                    Event event = events.get(getPosition());
                    event.setRsvp(Event.RSVP.NO);
                    bus.post(new RsvpChangeTrigger(event, Event.RSVP.NO));
                }
            });
        }
    }
}
