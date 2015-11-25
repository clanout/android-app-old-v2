package reaper.android.app.ui.screens.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.List;

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
                               .inflate(R.layout.list_item_event, parent, false);
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
        View llCategoryIconContainer;
        ImageView ivCategoryIcon;
        TextView tvTitle;
        TextView tvFriendsGoing;
        MaterialIconView mivRsvp;

        public EventViewHolder(View itemView)
        {
            super(itemView);

            llCategoryIconContainer = itemView.findViewById(R.id.llCategoryIconContainer);
            ivCategoryIcon = (ImageView) itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvFriendsGoing = (TextView) itemView.findViewById(R.id.tvFriendsGoing);
            mivRsvp = (MaterialIconView) itemView.findViewById(R.id.mivRsvp);

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
            if (event.getTitle().length() <= 20)
            {
                tvTitle.setText(event.getTitle());
            }
            else
            {
                tvTitle.setText(event.getTitle().substring(0, 18) + "...");
            }

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(DrawableFactory
                    .get(category, Dimensions.EVENT_FEED_ICON_SIZE));
            llCategoryIconContainer.setBackground(DrawableFactory.getIconBackground(category));

            // Friends Attending
            List<String> friends = event.getFriends();
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
        }
    }
}
