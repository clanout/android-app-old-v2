package reaper.android.app.ui.screens.home;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.factory.CreateEventSuggestionFactory;
import reaper.android.app.root.Reaper;
import reaper.android.app.ui.util.DrawableFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private List<Event> events;
    private List<SwipedState> state;
    private EventActionListener eventActionListener;
    private PagerSwipeListener pagerSwipeListener;
    private FragmentManager fragmentManager;
    private CreateEventFragment.CreateEventCycleHandler createEventCycleHandler;

    private static final int CREATE_EVENT_VIEW = 0;
    private static final int EVENT_LIST_ITEM_VIEW = 1;

    public EventsAdapter(List<Event> events,
                         EventActionListener eventActionListener,
                         PagerSwipeListener pagerSwipeListener,
                         FragmentManager fragmentManager,
                         CreateEventFragment.CreateEventCycleHandler createEventCycleHandler)
    {
        this.events = events;
        this.eventActionListener = eventActionListener;
        this.pagerSwipeListener = pagerSwipeListener;
        this.fragmentManager = fragmentManager;
        this.createEventCycleHandler = createEventCycleHandler;

        state = new ArrayList<>();
        for (int i = 0; i < events.size(); i++)
        {
            state.add(SwipedState.SHOWING_PRIMARY_CONTENT);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
        {
            return CREATE_EVENT_VIEW;
        }
        else
        {
            return EVENT_LIST_ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case CREATE_EVENT_VIEW:
                View view = LayoutInflater.from(parent.getContext())
                                          .inflate(R.layout.list_item_create, parent, false);
                return new CreateEventViewHolder(view);

            case EVENT_LIST_ITEM_VIEW:
                ViewPager v = (ViewPager) LayoutInflater.from(parent.getContext())
                                                        .inflate(R.layout.list_item_event, parent, false);
                return new EventViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        switch (getItemViewType(position))
        {
            case CREATE_EVENT_VIEW:
                CreateEventViewHolder createEventViewHolder = (CreateEventViewHolder) holder;
                onBindCreateEventViewHolder(createEventViewHolder, position);
                break;

            case EVENT_LIST_ITEM_VIEW:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                onBindEventViewHolder(eventViewHolder, position);
                break;
        }
    }

    private int getEventPosition(int position)
    {
        return position - 1;
    }

    private void onBindCreateEventViewHolder(CreateEventViewHolder holder, int position)
    {
    }

    private void onBindEventViewHolder(EventViewHolder holder, final int position)
    {
        final ViewPager viewPager = holder.viewPager;
        viewPager.setCurrentItem(state.get(getEventPosition(position)).getPosition());
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
                        state.set(getEventPosition(position), SwipedState.SHOWING_PRIMARY_CONTENT);
                        break;
                    case 1:
                        state.set(getEventPosition(position), SwipedState.SHOWING_SECONDARY_CONTENT);
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
                // TODO
            }
        });

        Event event = events.get(getEventPosition(position));
        holder.render(event);
    }

    @Override
    public int getItemCount()
    {
        return events.size() + 1;
    }

    public interface EventActionListener
    {
        void onEventClicked(Event event);

        void onRsvpChanged(EventsView.EventListItem eventListItem, Event event, Event.RSVP rsvp);
    }

    public interface PagerSwipeListener
    {
        void onPagerSwipe(int state);
    }

    private enum SwipedState
    {
        SHOWING_PRIMARY_CONTENT(0),
        SHOWING_SECONDARY_CONTENT(1);

        private int position;

        SwipedState(int position)
        {
            this.position = position;
        }

        public int getPosition()
        {
            return position;
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements EventsView.EventListItem
    {
        private ViewPager viewPager;
        private CardView cardView;
        private ImageView eventIcon;
        private TextView title, timeLocation, attendees, date, rsvp;
        private ImageButton going, mayBe, notGoing;
        private LinearLayout iconContainer;

        public EventViewHolder(View itemView)
        {
            super(itemView);

            viewPager = (ViewPager) itemView;
            cardView = (CardView) itemView.findViewById(R.id.cv_list_item_event);
            eventIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_icon);
            title = (TextView) itemView.findViewById(R.id.tv_list_item_event_title);
            timeLocation = (TextView) itemView.findViewById(R.id.tv_list_item_event_time_location);
            attendees = (TextView) itemView.findViewById(R.id.tv_list_item_event_attendees);
            date = (TextView) itemView.findViewById(R.id.tv_list_item_event_date);
            going = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_going);
            mayBe = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_maybe);
            notGoing = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_not_going);
            iconContainer = (LinearLayout) itemView
                    .findViewById(R.id.ll_list_item_event_icon_container);
            rsvp = (TextView) itemView.findViewById(R.id.tv_list_item_event_rsvp);

            viewPager.setAdapter(new EventViewPagerAdapter());
            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    eventActionListener
                            .onEventClicked(events.get(getEventPosition(getAdapterPosition())));
                }
            });

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                {

                }

                @Override
                public void onPageSelected(int position)
                {

                }

                @Override
                public void onPageScrollStateChanged(int state)
                {
                    pagerSwipeListener.onPagerSwipe(state);
                }
            });

            going.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    state.set(getEventPosition(getAdapterPosition()), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getEventPosition(getAdapterPosition()))
                                                  .getPosition());

                    eventActionListener
                            .onRsvpChanged(EventViewHolder.this, events
                                    .get(getEventPosition(getAdapterPosition())), Event.RSVP.YES);
                }
            });

            mayBe.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    state.set(getEventPosition(getAdapterPosition()), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getEventPosition(getAdapterPosition()))
                                                  .getPosition());

                    eventActionListener
                            .onRsvpChanged(EventViewHolder.this, events
                                    .get(getEventPosition(getAdapterPosition())), Event.RSVP.MAYBE);
                }
            });

            notGoing.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    state.set(getEventPosition(getAdapterPosition()), SwipedState.SHOWING_PRIMARY_CONTENT);
                    viewPager.setCurrentItem(state.get(getEventPosition(getAdapterPosition()))
                                                  .getPosition());

                    eventActionListener
                            .onRsvpChanged(EventViewHolder.this, events
                                    .get(getEventPosition(getAdapterPosition())), Event.RSVP.NO);
                }
            });
        }

        @Override
        public void render(Event event)
        {
            // Title
            if (event.getTitle().length() <= 20)
            {
                title.setText(event.getTitle());
            }
            else
            {
                title.setText(event.getTitle().substring(0, 18) + "...");
            }

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            eventIcon.setImageDrawable(DrawableFactory
                    .get(category, Dimensions.EVENT_FEED_ICON_SIZE));
            iconContainer.setBackground(DrawableFactory.randomIconBackground());

            // Date, Time and Location
            DateTime dateTime = event.getStartTime();
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

            int dayToday = DateTime.now().getDayOfWeek();

            Context context = Reaper.getReaperContext();
            date.setTextColor(ContextCompat.getColor(context, R.color.text_subtitle));
            if (dateTime.getDayOfWeek() == dayToday)
            {
                date.setTextColor(ContextCompat.getColor(context, R.color.text_subtitle));
            }
            else if (dateTime.getDayOfWeek() == (dayToday + 1))
            {
                date.setText("Tomorrow");
            }
            else
            {
                DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("EEEE");
                date.setText(dateTime.toString(dayFormatter));
            }

            if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
            {
                timeLocation
                        .setText(dateTime.toString(timeFormatter) + ", (Location Not Specified)");
            }
            else
            {
                if (event.getLocation().getName().length() <= 23)
                {
                    timeLocation
                            .setText(dateTime.toString(timeFormatter) + ", " + event.getLocation()
                                                                                    .getName());
                }
                else
                {
                    timeLocation
                            .setText(dateTime.toString(timeFormatter) + ", " + event.getLocation()
                                                                                    .getName()
                                                                                    .substring(0, 22) + "..");
                }
            }

            // Friends Attending
            if (event.getFriendCount() == 0)
            {
                attendees.setText("No friends are going");
            }
            else if (event.getFriendCount() == 1)
            {
                attendees.setText("1 friend is going");
            }
            else
            {
                attendees.setText(event.getFriendCount() + " friends are going");
            }

            // RSVP
            if (event.getRsvp() == Event.RSVP.YES)
            {
                rsvp.setVisibility(View.VISIBLE);
                rsvp.setTextColor(ContextCompat.getColor(context, R.color.going));
                rsvp.setText("Going");
            }
            else if (event.getRsvp() == Event.RSVP.MAYBE)
            {
                rsvp.setVisibility(View.VISIBLE);
                rsvp.setTextColor(ContextCompat.getColor(context, R.color.may_be));
                rsvp.setText("Maybe");
            }
            else
            {
                rsvp.setVisibility(View.INVISIBLE);
            }

            // already started
            if (DateTime.now().isAfter(event.getStartTime()))
            {
                date.setText("Started");
                date.setTextColor(Color.RED);
            }
        }
    }

    public class CreateEventViewHolder extends RecyclerView.ViewHolder
    {
        private ViewPager viewPager;

        public CreateEventViewHolder(View itemView)
        {
            super(itemView);

            final List<CreateEventModel> eventSuggestionList = CreateEventSuggestionFactory
                    .getEventSuggestions();

            viewPager = (ViewPager) itemView.findViewById(R.id.vp_list_item_create);
            viewPager.setAdapter(new CreateEventPagerAdapter(fragmentManager, eventSuggestionList));

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                {

                }

                @Override
                public void onPageSelected(int position)
                {

                }

                @Override
                public void onPageScrollStateChanged(int state)
                {
                    pagerSwipeListener.onPagerSwipe(state);
                }
            });

            Subscription subscription =
                    Observable.interval(2, TimeUnit.SECONDS)
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribe(new Subscriber<Long>()
                              {
                                  @Override
                                  public void onCompleted()
                                  {

                                  }

                                  @Override
                                  public void onError(Throwable e)
                                  {

                                  }

                                  @Override
                                  public void onNext(Long aLong)
                                  {
                                      int position = viewPager.getCurrentItem() + 1;
                                      if (position >= viewPager.getAdapter().getCount())
                                      {
                                          viewPager.setCurrentItem(0, false);
                                      }
                                      else
                                      {
                                          viewPager.setCurrentItem(position);
                                      }
                                  }
                              });

            createEventCycleHandler.addCycle(subscription);
        }
    }
}
