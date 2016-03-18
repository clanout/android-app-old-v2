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
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.CategoryIconFactory;
import reaper.android.common.analytics.AnalyticsHelper;

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

        @Bind(R.id.tvToday)
        TextView tvToday;

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

            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME, GoogleAnalyticsConstants.ACTION_OPEN_FEED_ITEM, String.valueOf(getAdapterPosition()));
            /* Analytics */
        }

        public void render(Event event)
        {
            // Title
            tvTitle.setText(event.getTitle());

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(CategoryIconFactory
                    .get(category, Dimensions.DEFAULT_BUBBLE_SIZE));
            llCategoryIconContainer.setBackground(CategoryIconFactory.getIconBackground(category));

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
                if (friends.size() > 1)
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
                if (friends.size() > 0)
                {
                    int otherCount = event.getFriendCount() - 1;
                    if (otherCount > 1)
                    {
                        tvFriendsGoing.setText(context
                                .getString(R.string.label_feed_multiple_friend_multiple_other,
                                        friends.get(0), otherCount));
                    }
                    else
                    {
                        tvFriendsGoing.setText(context
                                .getString(R.string.label_feed_multiple_friend_one_other,
                                        friends.get(0)));
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

            // Time
            DateTime startTime = event.getStartTime();
            LocalDate today = LocalDate.now();
            LocalDate startDate = startTime.toLocalDate();
            if (startDate.equals(today))
            {
                tvToday.setVisibility(View.VISIBLE);
            }
            else
            {
                tvToday.setVisibility(View.GONE);
            }
        }
    }
}
