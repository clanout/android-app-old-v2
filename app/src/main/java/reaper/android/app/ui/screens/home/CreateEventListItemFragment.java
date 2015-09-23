package reaper.android.app.ui.screens.home;

import android.app.FragmentManager;
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
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventListItemFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private EditText title;
    private ImageView icon;
    private LinearLayout linearLayout;
    private CardView cardView;
    private TextView createEvent;
    private Spinner daySpinner, timeSpinner, timeCategorySpinner;

    private Bus bus;
    private EventService eventService;
    private FragmentManager fragmentManager;
    private LocationService locationService;

    private EventSuggestion eventSuggestion;
    private List<String> dayList, timeCategoryList;
    private List<Integer> timeList;
    private ArrayAdapter<String> dayAdapter;
    private ArrayAdapter<String> timeCategoryAdapter;
    private ArrayAdapter<Integer> timeAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_list_item, container, false);
        linearLayout = (LinearLayout) view.findViewById(R.id.ll_fragment_create_event_list_item);
        title = (EditText) view.findViewById(R.id.et_fragment_create_event_list_item_title);
        icon = (ImageView) view.findViewById(R.id.iv_fragment_create_event_list_item_icon);
        cardView = (CardView) view.findViewById(R.id.cv_fragment_create_event_list_item);
        createEvent = (TextView) view.findViewById(R.id.tv_fragment_create_event_list_item_create);
        daySpinner = (Spinner) view.findViewById(R.id.s_fragment_create_event_list_item_day);
        timeSpinner = (Spinner) view.findViewById(R.id.s_fragment_create_event_list_item_time);
        timeCategorySpinner = (Spinner) view.findViewById(R.id.s_fragment_create_event_list_item_time_category);
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

        title.setText(eventSuggestion.getTitle());

        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getFragmentManager();

        createDayList();
        createTimeList();
        createTimeCategorylist();

        setDaySpinner();
        setTimeSpinner();
        setTimeCategorySpinner();

        linearLayout.setOnClickListener(this);
        cardView.setOnClickListener(this);
        icon.setOnClickListener(this);
        title.setOnTouchListener(this);
        createEvent.setOnClickListener(this);
        daySpinner.setOnItemSelectedListener(this);
        timeSpinner.setOnItemSelectedListener(this);
        timeCategorySpinner.setOnItemSelectedListener(this);
    }

    private void setDaySpinner() {

        dayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private void setTimeSpinner() {

        timeAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }

    private void setTimeCategorySpinner() {

        timeCategoryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, timeCategoryList);
        timeCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeCategorySpinner.setAdapter(timeCategoryAdapter);
    }

    private void createTimeCategorylist() {

        timeCategoryList = new ArrayList<>();
        timeCategoryList.add("AM");
        timeCategoryList.add("PM");
    }

    private void createTimeList() {

        timeList = new ArrayList<>();
        timeList.add(1);
        timeList.add(2);
        timeList.add(3);
        timeList.add(4);
        timeList.add(5);
        timeList.add(6);
        timeList.add(7);
        timeList.add(8);
        timeList.add(9);
        timeList.add(10);
        timeList.add(11);
        timeList.add(12);
    }

    private void createDayList() {

        dayList = new ArrayList<>();
        dayList.add("Monday");
        dayList.add("Tuesday");
        dayList.add("Wednesday");
        dayList.add("Thursday");
        dayList.add("Friday");
        dayList.add("Saturday");
        dayList.add("Sunday");
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

            Location location = new Location();
            location.setZone(locationService.getUserLocation().getZone());

            eventService.createEvent(title.getText().toString(), Event.Type.INVITE_ONLY, EventCategory.MOVIES, "", location, eventSuggestion.getSuggestedDateTime(), eventSuggestion.getSuggestedDateTime().plusDays(1).withTimeAtStartOfDay());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

//        bus.post(new ViewPagerClickedTrigger());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
