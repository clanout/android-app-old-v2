package reaper.android.app.ui.screens.friends;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.ui._core.BaseActivity;

public class FriendsActivity extends BaseActivity implements FriendsScreen
{
    public static Intent callingIntent(Context context)
    {
        return new Intent(context, FriendsActivity.class);
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Setup UI */
        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_friends);
        setActionBarBackVisibility(true);

        /* Notification View */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, FriendsFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        navigateToAccountScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            navigateToAccountScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */

    @Override
    public void navigateToAccountScreen()
    {
        finish();
    }
}