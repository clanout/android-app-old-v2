package reaper.android.app.ui.screens.home.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.DrawableFactory;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder>
{
    Context context;
    List<Event> events;
    EventActionListener eventActionListener;

    public EventsAdapter(Context context, List<Event> events, EventActionListener eventActionListener)
    {
        this.context = context;
        this.events = events;
        this.eventActionListener = eventActionListener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position)
    {
        Event event = events.get(position);
        holder.render(event);
    }

    @Override
    public int getItemCount()
    {
        return events.size();
    }

    public interface EventActionListener
    {
        void onEventClicked(Event event);
    }

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.llCategoryIconContainer)
        View llCategoryIconContainer;

        @Bind(R.id.ivCategoryIcon)
        ImageView ivCategoryIcon;

        @Bind(R.id.tvTitle)
        TextView tvTitle;

        @Bind(R.id.tvFriendsGoing)
        TextView tvFriendsGoing;

        @Bind(R.id.mivRsvp)
        MaterialIconView mivRsvp;

        @Bind(R.id.llToday)
        View llToday;

        public EventViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    eventActionListener.onEventClicked(events.get(getAdapterPosition()));
                }
            });
        }

        public void render(Event event)
        {
            // Title
            tvTitle.setText(event.getTitle());

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(DrawableFactory
                    .get(category, Dimensions.EVENT_FEED_ICON_SIZE));
            llCategoryIconContainer.setBackground(DrawableFactory.getIconBackground(category));

            // Friends Attending
            List<String> friends = event.getFriends();

            if (friends == null)
            {
                friends = new ArrayList<>();
            }

            if (event.getFriendCount() == 0)
            {
                tvFriendsGoing.setText(R.string.label_feed_no_friends);
            }
            else if (event.getFriendCount() == 1)
            {
                if (friends.size() > 0)
                {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_one_friend, friends.get(0)));
                }
                else
                {
                    tvFriendsGoing.setText(R.string.label_feed_one_friend_default);
                }
            }
            else
            {
                if (friends.size() == 1)
                {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_multiple_friend_one_name,
                                    friends.get(0), (event.getFriendCount() - 1)));
                }
                else if (friends.size() > 1)
                {
                    if (event.getFriendCount() == 2)
                    {
                        tvFriendsGoing.setText(context
                                .getString(R.string.label_feed_multiple_friend_two_name,
                                        friends.get(0), friends.get(1)));
                    }
                    else
                    {
                        tvFriendsGoing.setText(context
                                .getString(R.string.label_feed_multiple_friend,
                                        friends.get(0), friends.get(1), (event
                                                .getFriendCount() - 2)));
                    }
                }
                else
                {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_multiple_friend_default,
                                    event.getFriendCount()));
                }

                String friendsGoingText = tvFriendsGoing.getText().toString();
                if (friendsGoingText.length() > 30)
                {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_multiple_friend_default,
                                    event.getFriendCount()));
                }
            }

            // RSVP
            if (event.getRsvp() == Event.RSVP.YES)
            {
                mivRsvp.setVisibility(View.VISIBLE);
            }
            else
            {
                mivRsvp.setVisibility(View.GONE);
            }

            // Time
            DateTime startTime = event.getStartTime();
            LocalDate today = LocalDate.now();
            LocalDate startDate = startTime.toLocalDate();
            if (startDate.equals(today))
            {
                llToday.setVisibility(View.VISIBLE);
            }
            else
            {
                llToday.setVisibility(View.GONE);
            }
        }
    }
}
