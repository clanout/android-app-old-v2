package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import reaper.android.R;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;

public class DummyActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        FragmentUtils.changeFragment(getFragmentManager(), new HomeFragment());
    }
}
