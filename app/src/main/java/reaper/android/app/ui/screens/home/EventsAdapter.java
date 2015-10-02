package reaper.android.app.ui.screens.home;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

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
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.common.ViewPagerStateChangedTrigger;
import reaper.android.app.trigger.event.EventClickTrigger;
import reaper.android.app.trigger.event.RsvpChangeTrigger;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.common.communicator.Communicator;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Bus bus;
    private UserService userService;
    private Context context;
    private Drawable generalEventDrawable, eatoutDrawable, drinksDrawable, cafeDrawable, movieDrawable, outdoorsDrawable, partyDrawable, eventsDrawable, shoppingDrawable, goingDrawable, maybeDrawable;
    private FragmentManager fragmentManager;

    // Data
    private List<Event> events;
    private List<SwipedState> state;

    public static final int CREATE_VIEW_TYPE = 0;
    public static final int EVENT_VIEW_TYPE = 1;

    public EventsAdapter(Bus bus, List<Event> events, Context context, FragmentManager fragmentManager) {

        this.context = context;
        this.bus = bus;
        this.events = events;
        this.userService = new UserService(bus);
        this.fragmentManager = fragmentManager;

        state = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            state.add(SwipedState.SHOWING_PRIMARY_CONTENT);
        }

        generateDrawables();
    }

    private void generateDrawables() {

        generalEventDrawable = DrawableFactory.get(EventCategory.GENERAL, Dimensions.EVENT_FEED_ICON_SIZE, R.color.general);
        eatoutDrawable = DrawableFactory.get(EventCategory.EAT_OUT, Dimensions.EVENT_FEED_ICON_SIZE, R.color.eat_out);
        drinksDrawable = DrawableFactory.get(EventCategory.DRINKS, Dimensions.EVENT_FEED_ICON_SIZE, R.color.drinks);
        cafeDrawable = DrawableFactory.get(EventCategory.CAFE, Dimensions.EVENT_FEED_ICON_SIZE, R.color.cafe);
        movieDrawable = DrawableFactory.get(EventCategory.MOVIES, Dimensions.EVENT_FEED_ICON_SIZE, R.color.movies);
        outdoorsDrawable = DrawableFactory.get(EventCategory.OUTDOORS, Dimensions.EVENT_FEED_ICON_SIZE, R.color.outdoors);
        partyDrawable = DrawableFactory.get(EventCategory.PARTY, Dimensions.EVENT_FEED_ICON_SIZE, R.color.party);
        eventsDrawable = DrawableFactory.get(EventCategory.LOCAL_EVENTS, Dimensions.EVENT_FEED_ICON_SIZE, R.color.local_events);
        shoppingDrawable = DrawableFactory.get(EventCategory.SHOPPING, Dimensions.EVENT_FEED_ICON_SIZE, R.color.shopping);

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
    public int getItemViewType(int position) {
        if (position == 0) {
            return CREATE_VIEW_TYPE;
        } else {
            return EVENT_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return events.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case EVENT_VIEW_TYPE:
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
            case CREATE_VIEW_TYPE:
                View view = LayoutInflater.from(context).inflate(R.layout.list_item_create, parent, false);
                CreateViewHolder createViewHolder = new CreateViewHolder(view);
                return createViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case EVENT_VIEW_TYPE:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                handleEventViewType(eventViewHolder, position - 1);
                break;
            case CREATE_VIEW_TYPE:
                CreateViewHolder createViewHolder = (CreateViewHolder) holder;
                break;
        }
    }

    private void handleEventViewType(EventViewHolder eventViewHolder, final int position) {
        final ViewPager viewPager = eventViewHolder.viewPager;
        viewPager.setCurrentItem(state.get(position).getPosition());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int previousPagePosition = 0;

            @Override
            public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
                if (pagePosition == previousPagePosition) {
                    return;
                }

                switch (pagePosition) {
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
            public void onPageSelected(int pagePosition) {
                //This method keep incorrectly firing as the RecyclerView scrolls.
                //Use the one above instead
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                bus.post(new ViewPagerStateChangedTrigger(state));
            }
        });

        Event event = events.get(position);
        eventViewHolder.render(event);
    }

    private enum SwipedState {
        SHOWING_PRIMARY_CONTENT(0),
        SHOWING_SECONDARY_CONTENT(1);

        private int position;

        private SwipedState(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private CardView cardView;
        private ImageView eventIcon, rsvpIcon;
        private TextView alreadyStartedMessage;
        private TextView title, timeLocation, attendees, date;
        private ImageButton going, mayBe, notGoing;

        public EventViewHolder(View itemView) {
            super(itemView);

            viewPager = (ViewPager) itemView;
            viewPager.setAdapter(new EventViewPagerAdapter());

            cardView = (CardView) itemView.findViewById(R.id.cv_list_item_event);
            eventIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_icon);
            rsvpIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_rsvp);
            alreadyStartedMessage = (TextView) itemView.findViewById(R.id.tv_list_item_event_started);
            title = (TextView) itemView.findViewById(R.id.tv_list_item_event_title);
            timeLocation = (TextView) itemView.findViewById(R.id.tv_list_item_event_time_location);
            attendees = (TextView) itemView.findViewById(R.id.tv_list_item_event_attendees);
            date = (TextView) itemView.findViewById(R.id.tv_list_item_event_date);
            going = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_going);
            mayBe = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_maybe);
            notGoing = (ImageButton) itemView.findViewById(R.id.btn_list_item_event_not_going);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bus.post(new EventClickTrigger(events.get(getAdapterPosition() - 1
                    )));
                }
            });

            going.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (events.get(getAdapterPosition() - 1).getOrganizerId().equals(userService.getActiveUserId())) {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else {
                        state.set(getAdapterPosition() - 1, SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition() - 1).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition() - 1).getRsvp();
                        events.get(getAdapterPosition() - 1).setRsvp(Event.RSVP.YES);
                        render(events.get(getAdapterPosition() - 1));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition() - 1), oldRsvp));
                    }
                }
            });

            mayBe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (events.get(getAdapterPosition() - 1).getOrganizerId().equals(userService.getActiveUserId())) {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else {
                        state.set(getAdapterPosition() - 1, SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition() - 1).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition() - 1).getRsvp();
                        events.get(getAdapterPosition() - 1).setRsvp(Event.RSVP.MAYBE);
                        render(events.get(getAdapterPosition() - 1));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition() - 1), oldRsvp));
                    }
                }
            });

            notGoing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (events.get(getAdapterPosition() - 1).getOrganizerId().equals(userService.getActiveUserId())) {
                        Toast.makeText(context, R.string.cannot_change_rsvp, Toast.LENGTH_LONG).show();
                    } else {
                        state.set(getAdapterPosition() - 1, SwipedState.SHOWING_PRIMARY_CONTENT);
                        viewPager.setCurrentItem(state.get(getAdapterPosition() - 1).getPosition());

                        Event.RSVP oldRsvp = events.get(getAdapterPosition() - 1).getRsvp();
                        events.get(getAdapterPosition() - 1).setRsvp(Event.RSVP.NO);
                        render(events.get(getAdapterPosition() - 1));

                        bus.post(new RsvpChangeTrigger(events.get(getAdapterPosition() - 1), oldRsvp));
                    }
                }
            });
        }

        public void render(Event event) {

            // Title
            if (event.getTitle().length() <= 20) {
                title.setText(event.getTitle());
            } else {
                title.setText(event.getTitle().substring(0, 18) + "...");
            }

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            switch (category) {
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
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

            int dayToday = DateTime.now().getDayOfWeek();

            if(dateTime.getDayOfWeek() == dayToday)
            {
                date.setText("Today");
            }else if(dateTime.getDayOfWeek() == (dayToday + 1))
            {
                date.setText("Tommorrow");
            }else if(dateTime.getDayOfWeek() == 1)
            {
                date.setText("Monday");
            }else if(dateTime.getDayOfWeek() == 2)
            {
                date.setText("Tuesday");
            }else if(dateTime.getDayOfWeek() == 3)
            {
                date.setText("Wednesday");
            }else if(dateTime.getDayOfWeek() == 4)
            {
                date.setText("Thursday");
            }else if(dateTime.getDayOfWeek() == 5)
            {
                date.setText("Friday");
            }else if(dateTime.getDayOfWeek() == 6)
            {
                date.setText("Saturday");
            }else if(dateTime.getDayOfWeek() == 7)
            {
                date.setText("Sunday");
            }

            if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty()) {
                timeLocation.setText(dateTime.toString(timeFormatter) + ", (Location Not Specified)");
            } else {
                if (event.getLocation().getName().length() <= 23) {
                    timeLocation.setText(dateTime.toString(timeFormatter) + ", " + event.getLocation().getName());
                } else {
                    timeLocation.setText(dateTime.toString(timeFormatter) + ", " + event.getLocation().getName().substring(0, 22) + "..");
                }
            }

            // Friends Attending
            if (event.getFriendCount() == 0) {
                attendees.setText("No friends are going");
            } else if (event.getFriendCount() == 1) {
                attendees.setText("1 friend is going");
            } else {
                attendees.setText(event.getFriendCount() + " friends are going");
            }

            // RSVP
            if (event.getRsvp() == Event.RSVP.YES) {
                rsvpIcon.setVisibility(View.VISIBLE);
                rsvpIcon.setImageDrawable(goingDrawable);
            } else if (event.getRsvp() == Event.RSVP.MAYBE) {
                rsvpIcon.setVisibility(View.VISIBLE);
                rsvpIcon.setImageDrawable(maybeDrawable);
            } else {
                rsvpIcon.setVisibility(View.INVISIBLE);
            }

            // already started
            if (DateTime.now().isAfter(event.getStartTime())) {
                alreadyStartedMessage.setVisibility(View.VISIBLE);
                alreadyStartedMessage.setText("Started");
            } else {
                alreadyStartedMessage.setVisibility(View.GONE);
            }
        }
    }

    public class CreateViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private Bus bus;
        private Subscriber<Integer> subscriber;
        private List<CreateEventModel> eventSuggestionList;

        public CreateViewHolder(View itemView) {
            super(itemView);

            bus = Communicator.getInstance().getBus();
            bus.register(this);
            eventSuggestionList = CreateEventSuggestionFactory.getEventSuggestions();

            viewPager = (ViewPager) itemView.findViewById(R.id.vp_list_item_create);
            viewPager.setAdapter(new CreateEventPagerAdapter(fragmentManager, eventSuggestionList));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    bus.post(new ViewPagerStateChangedTrigger(state));
                }
            });

            subscriber = new Subscriber<Integer>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Integer integer) {

                }
            };

            rx.Observable.interval(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).map(new Func1<Long, Integer>() {
                @Override
                public Integer call(Long aLong) {
                    int index = (int) (aLong % 4);
                    viewPager.setCurrentItem(index);
                    return null;

                }
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
        }

        @Subscribe
        public void clickOnViewPagerDetected(ViewPagerClickedTrigger trigger) {

            subscriber.unsubscribe();
        }
    }

}
