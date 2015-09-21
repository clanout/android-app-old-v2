package reaper.android.app.ui.screens.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> categories;

    public CreateEventPagerAdapter(FragmentManager fm) {
        super(fm);

        categories = new ArrayList<>();
        categories.add("Movie");
        categories.add("Eat Out");
        categories.add("Drinks");
        categories.add("Sports");
    }

    @Override
    public Fragment getItem(int position) {

        CreateEventListItemFragment createEventListItemFragment = new CreateEventListItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category", categories.get(position));
        createEventListItemFragment.setArguments(bundle);
        return createEventListItemFragment;
    }

    @Override
    public int getCount() {
        return categories.size();
    }
}
