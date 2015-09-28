package reaper.android.app.ui.create_event;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.FragmentUtils;

public class DummyActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        FragmentUtils.changeFragment(getFragmentManager(), CreateEventFragment
                .newInstance(new CreateEventModel(EventCategory.DRINKS, "Want to go out for a beer?")));
    }
}
