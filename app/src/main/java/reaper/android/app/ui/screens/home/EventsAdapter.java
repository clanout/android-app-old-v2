package reaper.android.app.ui.screens.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.ViewPagerStateChangedTrigger;
import reaper.android.app.trigger.event.EventClickTrigger;
import reaper.android.app.trigger.event.RsvpChangeTrigger;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder>
{
    private Bus bus;
    private UserService userService;
    private Context context;
    private Drawable generalEventDrawable, eatoutDrawable, drinksDrawable, cafeDrawable, movieDrawable, outdoorsDrawable, partyDrawable, eventsDrawable, shoppingDrawable, goingDrawable, maybeDrawable;

    // Data
    private List<Event> events;
    private List<SwipedState> state;

    public EventsAdapter(Bus bus, List<Event> events, Context context)
    {
        this.context = context;
        this.bus = bus;
        this.events = events;
        this.userService = new UserService(bus);

        state = new ArrayList<>();
        for (int i = 0; i < events.size(); i++)
        {
            state.add(SwipedState.SHOWING_PRIMARY_CONTENT);
        }

        generateDrawables();
    }

    private void generateDrawables()
    {
        generalEventDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        eatoutDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.FOOD)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        drinksDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MARTINI)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        cafeDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.COFFEE)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        movieDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MOVIE)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        outdoorsDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.TENNIS)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        partyDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.GIFT)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        eventsDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CITY)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        shoppingDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.SHOPPING)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(Dimensions.EVENT_FEED_ICON_SIZE)
                .build();

        goingDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(context.getResources().getColor(R.color.green))
                .setSizeDp(24)
                .setStyle(Paint.Style.STROKE)
                .build();

        maybeDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.HELP)
                .setColor(context.getResources().getColor(R.color.yellow))
                .setSizeDp(24)
                .build();
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
//        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
//        v.getLayoutParams().width = displayMetrics.widthPixels;
//        v.requestLayout();

        EventViewHolder vh = new EventViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, final int position)
    {
        final ViewPager viewPager = holder.viewPager;
        viewPager.setCurrentItem(state.get(position).getPosition());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
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

                bus.post(new ViewPagerStateChangedTrigger(state));
            }
        });

        Event event = events.get(position);
        holder.render(event);
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
        private ImageButton going, mayBe, notGoing;

        public EventViewHolder(View itemView)
        {
            super(itemView);

            viewPager = (ViewPager) itemView;
            viewPager.setAdapter(new EventViewPagerAdapter());

            cardView = (CardView) itemView.findViewById(R.id.cv_list_item_event);
            eventIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_icon);
            rsvpIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_rsvp);
            chatIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_chat);
            updatesIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_updates);
            title = (TextView) itemView.findViewById(R.id.tv_list_item_event_title);
            timeLocation = (TextView) itemView.findViewById(R.id.tv_list_item_event_time_location);
            attendees = (TextView) itemView.findViewById(R.id.tv_list_item_event_attendees);
            date = (TextView) itemView.findViewById(R.id.tv_list_item_event_date);
            going = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_going);
            mayBe = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_maybe);
            notGoing = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_not_going);

            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    bus.post(new EventClickTrigger(events.get(getAdapterPosition())));
                }
            });

            going.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (events.get(getAdapterPosition()).getOrganizerId().equals(userService.getActiveUserId()))
                    {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else
                    {
                        state.set(getAdapterPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition()).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition()).getRsvp();
                        events.get(getAdapterPosition()).setRsvp(Event.RSVP.YES);
                        render(events.get(getAdapterPosition()));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition()), oldRsvp));
                    }
                }
            });

            mayBe.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (events.get(getAdapterPosition()).getOrganizerId().equals(userService.getActiveUserId()))
                    {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else
                    {
                        state.set(getAdapterPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition()).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition()).getRsvp();
                        events.get(getAdapterPosition()).setRsvp(Event.RSVP.MAYBE);
                        render(events.get(getAdapterPosition()));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition()), oldRsvp));
                    }
                }
            });

            notGoing.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (events.get(getAdapterPosition()).getOrganizerId().equals(userService.getActiveUserId()))
                    {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else
                    {
                        state.set(getAdapterPosition(), SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition()).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition()).getRsvp();
                        events.get(getAdapterPosition()).setRsvp(Event.RSVP.NO);
                        render(events.get(getAdapterPosition()));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition()), oldRsvp));
                    }
                }
            });
        }

        public void render(Event event)
        {
            boolean isChatUpdated = false;

            // Title
            if (event.getTitle().length() <= 20)
            {
                title.setText(event.getTitle());
            } else
            {
                title.setText(event.getTitle().substring(0, 18) + "...");
            }

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            switch (category)
            {
                case GENERAL:
                    eventIcon.setImageDrawable(generalEventDrawable);
                    break;
                case EAT_OUT:
                    eventIcon.setImageDrawable(eatoutDrawable);
                    break;
                case DRINKS:
                    eventIcon.setImageDrawable(drinksDrawable);
                    break;
                case CAFE:
                    eventIcon.setImageDrawable(cafeDrawable);
                    break;
                case MOVIES:
                    eventIcon.setImageDrawable(movieDrawable);
                    break;
                case OUTDOORS:
                    eventIcon.setImageDrawable(outdoorsDrawable);
                    break;
                case PARTY:
                    eventIcon.setImageDrawable(partyDrawable);
                    break;
                case LOCAL_EVENTS:
                    eventIcon.setImageDrawable(eventsDrawable);
                    break;
                case SHOPPING:
                    eventIcon.setImageDrawable(shoppingDrawable);
                    break;
                default:
                    eventIcon.setImageDrawable(generalEventDrawable);
            }

            // Date, Time and Location
            DateTime dateTime = event.getStartTime();
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM dd");
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

            date.setText(dateTime.toString(dateFormatter));
            if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
            {
                timeLocation.setText(dateTime.toString(timeFormatter) + ", (Location Not Specified)");
            } else
            {
                if (event.getLocation().getName().length() <= 23)
                {
                    timeLocation.setText(dateTime.toString(timeFormatter) + ", " + event.getLocation().getName());
                } else
                {
                    timeLocation.setText(dateTime.toString(timeFormatter) + ", " + event.getLocation().getName().substring(0, 22) + "..");
                }
            }

            // Friends Attending
            if (event.getFriendCount() == 0)
            {
                attendees.setText("No friends are going");
            } else if (event.getFriendCount() == 1)
            {
                attendees.setText("1 friend is going");
            } else
            {
                attendees.setText(event.getFriendCount() + " friends are going");
            }

            // RSVP
            if (event.getRsvp() == Event.RSVP.YES)
            {
                rsvpIcon.setVisibility(View.VISIBLE);
                rsvpIcon.setImageDrawable(goingDrawable);
            } else if (event.getRsvp() == Event.RSVP.MAYBE)
            {
                rsvpIcon.setVisibility(View.VISIBLE);
                rsvpIcon.setImageDrawable(maybeDrawable);
            } else
            {
                rsvpIcon.setVisibility(View.INVISIBLE);
            }

            if (event.getRsvp() == Event.RSVP.YES || event.getRsvp() == Event.RSVP.MAYBE)
            {
                // Chat Updates
                if (isChatUpdated)
                {
                    chatIcon.setVisibility(View.VISIBLE);
                } else
                {
                    chatIcon.setVisibility(View.INVISIBLE);
                }

                // Event Updates
                if (event.isUpdated())
                {
                    updatesIcon.setVisibility(View.VISIBLE);
                } else
                {
                    updatesIcon.setVisibility(View.INVISIBLE);
                }
            } else
            {
                chatIcon.setVisibility(View.INVISIBLE);
                updatesIcon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
