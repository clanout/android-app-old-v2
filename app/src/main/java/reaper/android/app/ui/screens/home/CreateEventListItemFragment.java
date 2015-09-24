package reaper.android.app.ui.screens.home;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventSuggestion;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventListItemFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener {

    private EditText title;
    private ImageView icon;
    private LinearLayout linearLayout;
    private CardView cardView;
    private ImageView decreaseTimeButton, increaseTimeButton, expand, increaseDayButton;
    private TextView time, createEvent, day;
    private Drawable generalDrawable, eatOutDrawable, drinksDrawable, cafeDrawable, moviesDrawable, outdorsDrawable, partyDrawable, localEventsDrawable, shoppingDrawable, increaseTimeDrawable, decreaseTimeDrawable, expandDrawable;

    private Bus bus;
    private EventService eventService;
    private FragmentManager fragmentManager;
    private LocationService locationService;

    private EventSuggestion eventSuggestion;
    private Event.Type eventType;
    private EventCategory eventCategory;
    private String eventDescription;
    private Location placeLocation;
    private DateTime startDateTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_list_item, container, false);
        linearLayout = (LinearLayout) view.findViewById(R.id.ll_fragment_create_event_list_item);
        title = (EditText) view.findViewById(R.id.et_fragment_create_event_list_item_title);
        icon = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_icon);
        cardView = (CardView) view.findViewById(R.id.cv_fragment_create_event_list_item);
        time = (TextView) view.findViewById(R.id.tv_fragment_create_event_list_item_time);
        createEvent = (TextView) view.findViewById(R.id.tv_fragment_create_event_list_item_create);
        decreaseTimeButton = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_decrease_time);
        increaseDayButton = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_increase_day);
        increaseTimeButton = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_increase_time);
        expand = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_expand);
        day = (TextView) view.findViewById(R.id.tv_fragment_create_event_list_item_day);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        eventSuggestion = (EventSuggestion) bundle.getSerializable(BundleKeys.EVENT_SUGGESTION);

        if (eventSuggestion == null) {
            throw new IllegalStateException("Event Suggestion is null");
        }

        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getFragmentManager();

        linearLayout.setOnClickListener(this);
        cardView.setOnClickListener(this);
        icon.setOnClickListener(this);
        title.setOnTouchListener(this);
        time.setOnClickListener(this);
        createEvent.setOnClickListener(this);
        expand.setOnClickListener(this);
        increaseTimeButton.setOnClickListener(this);
        decreaseTimeButton.setOnClickListener(this);

        generateDrawables();
        initialiseEventSuggestion();
        render();
    }

    private void generateDrawables() {

        generalDrawable = DrawableFactory.getGeneralDrawable();
        eatOutDrawable = DrawableFactory.getEatOutDrawable();
        drinksDrawable = DrawableFactory.getDrinksDrawable();
        cafeDrawable = DrawableFactory.getCafeDrawable();
        moviesDrawable = DrawableFactory.getMoviesDrawable();
        outdorsDrawable = DrawableFactory.getOutdorsDrawable();
        partyDrawable = DrawableFactory.getPartyDrawable();
        localEventsDrawable = DrawableFactory.getLocalEventsDrawable();
        shoppingDrawable = DrawableFactory.getShoppingDrawable();
        increaseTimeDrawable = DrawableFactory.getIncreaseTimeDrawable();
        decreaseTimeDrawable = DrawableFactory.getDecreaseTimeDrawable();
        expandDrawable = DrawableFactory.getExpandDrawable();

    }

    private void initialiseEventSuggestion() {

        eventType = Event.Type.INVITE_ONLY;

        switch (EventCategory.valueOf(eventSuggestion.getCategory())) {
            case GENERAL:
                eventCategory = EventCategory.GENERAL;
                break;
            case EAT_OUT:
                eventCategory = EventCategory.EAT_OUT;
                break;
            case DRINKS:
                eventCategory = EventCategory.DRINKS;
                break;
            case PARTY:
                eventCategory = EventCategory.PARTY;
                break;
            case OUTDOORS:
                eventCategory = EventCategory.OUTDOORS;
                break;
            case CAFE:
                eventCategory = EventCategory.CAFE;
                break;
            case LOCAL_EVENTS:
                eventCategory = EventCategory.LOCAL_EVENTS;
                break;
            case SHOPPING:
                eventCategory = EventCategory.SHOPPING;
                break;
            case MOVIES:
                eventCategory = EventCategory.MOVIES;
                break;
        }

        eventDescription = "";

        startDateTime = eventSuggestion.getSuggestedDateTime();

        placeLocation = new Location();
        placeLocation.setZone(locationService.getUserLocation().getZone());
    }

    private void render() {

        title.setHint(eventSuggestion.getTitle());

        switch (eventCategory) {
            case GENERAL:
                icon.setImageDrawable(generalDrawable);
                break;
            case EAT_OUT:
                icon.setImageDrawable(eatOutDrawable);
                break;
            case DRINKS:
                icon.setImageDrawable(drinksDrawable);
                break;
            case CAFE:
                icon.setImageDrawable(cafeDrawable);
                break;
            case MOVIES:
                icon.setImageDrawable(moviesDrawable);
                break;
            case OUTDOORS:
                icon.setImageDrawable(outdorsDrawable);
                break;
            case PARTY:
                icon.setImageDrawable(partyDrawable);
                break;
            case LOCAL_EVENTS:
                icon.setImageDrawable(localEventsDrawable);
                break;
            case SHOPPING:
                icon.setImageDrawable(shoppingDrawable);
                break;
        }

        increaseTimeButton.setImageDrawable(increaseTimeDrawable);
        decreaseTimeButton.setImageDrawable(decreaseTimeDrawable);
        expand.setImageDrawable(expandDrawable);
        increaseDayButton.setImageDrawable(increaseTimeDrawable);

        int hour = startDateTime.getHourOfDay();

        if (hour < 12) {
            time.setText(hour + " AM");
        } else if(hour > 12 && hour < 24){
            time.setText((hour-12) + " PM");
        }else if(hour == 12)
        {
            time.setText(hour + " noon");
        }else if(hour == 24)
        {
            time.setText(hour + " midnight");
        }

        day.setText("Monday");
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onClick(View v) {

        bus.post(new ViewPagerClickedTrigger());

        if (v.getId() == R.id.tv_fragment_create_event_list_item_create) {
            eventService.createEvent(title.getText().toString(), eventType, eventCategory, eventDescription, placeLocation, startDateTime, startDateTime.plusDays(1).withTimeAtStartOfDay());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        bus.post(new ViewPagerClickedTrigger());
        return false;
    }

    @Subscribe
    public void onEventCreated(EventCreatedTrigger trigger) {

        InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, trigger.getEvent());
        bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, true);
        inviteUsersContainerFragment.setArguments(bundle);

        FragmentUtils.changeFragment(fragmentManager, inviteUsersContainerFragment);
    }

}
