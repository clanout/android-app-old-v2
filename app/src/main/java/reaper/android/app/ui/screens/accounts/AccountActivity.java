package reaper.android.app.ui.screens.accounts;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.friends.FriendsActivity;
import reaper.android.common.analytics.AnalyticsHelper;

public class AccountActivity extends BaseActivity implements AccountScreen
{
    public static Intent callingIntent(Context context)
    {
        return new Intent(context, AccountActivity.class);
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_ACCOUNTS_ACTIVITY);
        /* Analytics */

        /* Setup UI */
        setContentView(R.layout.activity_accounts);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_account);
        setActionBarBackVisibility(false);

        /* Accounts View */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, AccountFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        navigateToHomeScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_account, menu);

        Drawable homeDrawable = MaterialDrawableBuilder
                .with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.HOME)
                .setColor(ContextCompat.getColor(this, R.color.white))
                .setSizeDp(36)
                .build();

        menu.findItem(R.id.action_home).setIcon(homeDrawable);
        menu.findItem(R.id.action_home)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    navigateToHomeScreen();
                    return true;
                }
            });

        return super.onCreateOptionsMenu(menu);
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        // Account activity is always opened from home screen (so it is always in the backstack)
        finish();
    }

    @Override
    public void navigateToFriendsScreen()
    {
        startActivity(FriendsActivity.callingIntent(this));
    }
}
