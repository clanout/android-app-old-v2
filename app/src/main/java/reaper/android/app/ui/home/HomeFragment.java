package reaper.android.app.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.trigger.EventClickTrigger;
import reaper.android.app.trigger.EventsFetchTrigger;
import reaper.android.app.trigger.RsvpChangeTrigger;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class HomeFragment extends Fragment
{
    private FragmentManager fragmentManager;
    private Bus bus;
    private EventService eventService;

    // Data
    private List<Event> events;
    private List<String> eventUpdates;
    private List<String> chatUpdates;

    // UI Elements
    private TextView noEventsMessage;
    private RecyclerView eventList;
    private LinearLayout buttonBar;
    private Button filter, sort;

    private EventsAdapter eventsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        eventList = (RecyclerView) view.findViewById(R.id.rv_event_list);
        noEventsMessage = (TextView) view.findViewById(R.id.tv_event_list_empty);
        buttonBar = (LinearLayout) view.findViewById(R.id.ll_event_list_btn_bar);
        filter = (Button) view.findViewById(R.id.btn_event_list_filter);
        sort = (Button) view.findViewById(R.id.btn_event_list_sort);

//        sort.setOnClickListener(this);
//        filter.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();

        fragmentManager = getActivity().getSupportFragmentManager();

        eventService = new EventService(bus);

        filter.setText("All Events");
        sort.setText("Relevance");

        events = new ArrayList<>();
        eventUpdates = new ArrayList<>();
        chatUpdates = new ArrayList<>();

        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);
        eventService.fetchEvents("Bengaluru");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Cache.commit(getActivity(), AppConstants.CACHE_FILE);
    }

    @Subscribe
    public void onEventsFetchTrigger(EventsFetchTrigger eventsFetchTrigger)
    {
        events = eventsFetchTrigger.getEvents();
        Log.d("reap3r", "Event Count = " + events.size());
        refreshRecyclerView(events, eventUpdates, chatUpdates);
    }

    @Subscribe
    public void onEventClickTrigger(EventClickTrigger eventClickTrigger)
    {
        Event event = eventClickTrigger.getEvent();
        Toast.makeText(getActivity(), event.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onRsvpChangeTrigger(RsvpChangeTrigger rsvpChangeTrigger)
    {
        Event event = rsvpChangeTrigger.getEvent();
        Event.RSVP rsvp = rsvpChangeTrigger.getRsvp();

        Toast.makeText(getActivity(), event.getTitle() + " -> " + String.valueOf(rsvp), Toast.LENGTH_LONG).show();
    }

    private void initRecyclerView()
    {
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventsAdapter = new EventsAdapter(bus, new ArrayList<Event>(), new ArrayList<String>(), new ArrayList<String>());
        eventList.setAdapter(eventsAdapter);
    }

    private void refreshRecyclerView(List<Event> events, List<String> updates, List<String> chatUpdates)
    {
        if (events.size() == 0)
        {
            setNoEventsView();
        }
        else
        {
            setNormalView();
        }

        eventsAdapter = new EventsAdapter(bus, events, updates, chatUpdates);
        eventList.setAdapter(eventsAdapter);
    }

    public void setNormalView()
    {
        noEventsMessage.setVisibility(View.GONE);
        eventList.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
    }

    public void setNoEventsView()
    {
        noEventsMessage.setText("No events to show");
        noEventsMessage.setVisibility(View.VISIBLE);
        eventList.setVisibility(View.GONE);
        buttonBar.setVisibility(View.GONE);
    }
}
