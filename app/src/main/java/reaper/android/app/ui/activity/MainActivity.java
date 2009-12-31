package reaper.android.app.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import reaper.android.R;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.app.ui.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class MainActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        FragmentUtils.changeFragment(fragmentManager, new HomeFragment(), false);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getSupportActionBar().setTitle("reap3r");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Communicator.getInstance().getBus().post(new CacheCommitTrigger());
    }
}
