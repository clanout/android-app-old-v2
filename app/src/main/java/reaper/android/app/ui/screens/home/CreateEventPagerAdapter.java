package reaper.android.app.ui.screens.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.CreateEventModel;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {

    private List<CreateEventModel> eventSuggestionList;

    public CreateEventPagerAdapter(FragmentManager fm, List<CreateEventModel> eventSuggestionList) {
        super(fm);

        this.eventSuggestionList = eventSuggestionList;
    }

    @Override
    public Fragment getItem(int position) {

        CreateEventFragment createEventListItemFragment = new CreateEventFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_SUGGESTION, eventSuggestionList.get(position));
        createEventListItemFragment.setArguments(bundle);
        return createEventListItemFragment;
    }

    @Override
    public int getCount() {
        return eventSuggestionList.size();
    }
}
