package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.screens.create.CreateEventDetailsFragment;
import reaper.android.app.ui.util.FragmentUtils;

public class DummyActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);


        CreateEventDetailsFragment fragment = CreateEventDetailsFragment
                .newInstance("Hello World", EventCategory.DRINKS, Event.Type.PUBLIC, null, null);

        FragmentUtils.changeFragment(getFragmentManager(), fragment);
    }
}
