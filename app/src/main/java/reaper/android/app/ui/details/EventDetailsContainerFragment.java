package reaper.android.app.ui.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.common.communicator.Communicator;

public class EventDetailsContainerFragment extends Fragment
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Data
    private List<Event> events;
    private int activePosition;

    // UI Elements
    private ViewPager viewPager;

    private PagerAdapter pagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_event_details_container);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            events = (List<Event>) savedInstanceState.get("events");
            activePosition = savedInstanceState.getInt("active_event");
        }
        else
        {
            Bundle bundle = getArguments();
            events = (List<Event>) bundle.get("events");
            activePosition = bundle.getInt("active_event");
        }

        if (events == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();

        pagerAdapter = new EventDetailsPagerAdapter(getChildFragmentManager(), events);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setPageTransformer(true, new ViewPagerTransformer(ViewPagerTransformer.TransformType.ZOOM));

        viewPager.setCurrentItem(activePosition);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                activePosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("events", (ArrayList<Event>) events);
        outState.putInt("active_event", activePosition);
    }
}
