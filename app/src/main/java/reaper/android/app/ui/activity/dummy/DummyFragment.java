package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.screens.core.BaseFragment;

public class DummyFragment extends BaseFragment
{
    ViewPager viewPager;
    CreateEventPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_dummy, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.vp_dummy);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        List<CreateEventModel> models = new ArrayList<>();
        models.add(new CreateEventModel(EventCategory.DRINKS, "Want to go out for a beer?"));
        models.add(new CreateEventModel(EventCategory.OUTDOORS, "Want to play football?"));
        models.add(new CreateEventModel(EventCategory.EAT_OUT, "dsfsdf"));
        models.add(new CreateEventModel(EventCategory.CAFE, "dfsdf?"));
        models.add(new CreateEventModel(EventCategory.LOCAL_EVENTS, "fsdf football?"));
        models.add(new CreateEventModel(EventCategory.PARTY, "rgrt football?"));
        models.add(new CreateEventModel(EventCategory.MOVIES, "vbc football?"));

        adapter = new CreateEventPagerAdapter(getFragmentManager(), models, 2);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(models.size());
    }
}
