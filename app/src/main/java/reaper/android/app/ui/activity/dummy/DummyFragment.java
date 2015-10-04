package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import reaper.android.R;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.EventCategory;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.home.CreateEventPagerAdapter;
import reaper.android.common.communicator.Communicator;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class DummyFragment extends BaseFragment
{
    private Bus bus;

    ViewPager viewPager;
    CreateEventPagerAdapter adapter;

    Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bus = Communicator.getInstance().getBus();
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
        models.add(new CreateEventModel(EventCategory.SPORTS, "fsdf football?"));
        models.add(new CreateEventModel(EventCategory.INDOORS, "rgrt football?"));
        models.add(new CreateEventModel(EventCategory.MOVIES, "vbc football?"));
        models.add(new CreateEventModel(EventCategory.DRINKS, "Want to go out for a beer?"));

        adapter = new CreateEventPagerAdapter(getFragmentManager(), models);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(models.size());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);
        subscription = Observable.interval(2, TimeUnit.SECONDS)
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
                                         if (position >= adapter.getCount())
                                         {
                                             viewPager.setCurrentItem(0, false);
                                         }
                                         else
                                         {
                                             viewPager.setCurrentItem(position);
                                         }
                                         Timber.v("here : " + position);
                                     }
                                 });
    }

    @Subscribe
    public void editMode(ViewPagerClickedTrigger trigger)
    {
        if (subscription != null)
        {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (subscription != null)
        {
            subscription.unsubscribe();
        }

        bus.unregister(this);
    }
}
