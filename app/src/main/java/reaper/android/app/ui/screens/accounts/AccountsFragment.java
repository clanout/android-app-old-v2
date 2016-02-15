package reaper.android.app.ui.screens.accounts;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import de.hdodenhof.circleimageview.CircleImageView;
import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.facebook.FacebookCoverPicFetchedTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.accounts.friends.ManageFriendsFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class AccountsFragment extends BaseFragment implements AccountsAdapter.AccountsItemClickListener
{
    //    private TextView userName;
    private RecyclerView recyclerView;
    private ImageView userPic;
    private CircleImageView userProfilePic;
    private Drawable homeDrawable, personDrawable;
    private Toolbar toolbar;
    private TextView userName;

    private FragmentManager fragmentManager;
    private UserService userService;
    private FacebookService facebookService;
    private Bus bus;

    private GenericCache genericCache;

    private AccountsAdapter accountsAdapter;

    // TODO -- X invited you to join ---- event details screen -- onClick invite icon

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);

        userPic = (ImageView) view.findViewById(R.id.iv_account_fragment_user_pic);
        userProfilePic = (CircleImageView) view
                .findViewById(R.id.iv_account_fragment_user_profile_pic);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_accounts_fragment);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_accounts);
        userName = (TextView) view.findViewById(R.id.tv_account_fragment_user_name);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        fragmentManager = getActivity().getFragmentManager();
        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        facebookService = new FacebookService(bus);

        genericCache = CacheManager.getGenericCache();

        generateDrawables();

        accountsAdapter = new AccountsAdapter(getActivity(), AccountsService.getmenuList());
        accountsAdapter.setAccountItemClickListener(this);
        recyclerView.setAdapter(accountsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void generateDrawables()
    {
        homeDrawable = MaterialDrawableBuilder.with(getActivity())
                                              .setIcon(MaterialDrawableBuilder.IconValue.HOME)
                                              .setColor(ContextCompat
                                                      .getColor(getActivity(), R.color.white))
                                              .setSizeDp(36)
                                              .build();

        personDrawable = MaterialDrawableBuilder.with(getActivity())
                                                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                                                .setColor(ContextCompat
                                                        .getColor(getActivity(), R.color.light_grey))
                                                .setSizeDp(36)
                                                .build();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.ACCOUNTS_FRAGMENT);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_account);

        bus.register(this);

        userName.setText(userService.getActiveUserName());

        if (genericCache.get(CacheKeys.USER_COVER_PIC) != null)
        {

            Picasso.with(getActivity())
                   .load(genericCache.get(CacheKeys.USER_COVER_PIC))
                   .placeholder(personDrawable)
                   .fit()
                   .centerCrop()
                   .noFade()
                   .into(userPic);

        }
        else
        {
            facebookService.getUserCoverPic();
        }

        Picasso.with(getActivity())
               .load("https://graph.facebook.com/v2.4/" + userService
                       .getActiveUserId() + "/picture?height=1000")
               .placeholder(personDrawable)
               .fit()
               .centerCrop()
               .noFade()
               .into(userProfilePic);

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.ACCOUNTS);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(true);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);

        menu.findItem(R.id.action_home).setIcon(homeDrawable);

        menu.findItem(R.id.action_home)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    AccountsFragment.this.getActivity().onBackPressed();
                    return true;
                }
            });
    }

    @Override
    public void onAccountItemClicked(View view, int position)
    {
        if (position == 0)
        {
            FragmentUtils.changeFragment(fragmentManager, new ManageFriendsFragment());
        }
        else if (position == 1)
        {

            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.UPDATE_PHONE_CLICKED_ACCOUNTS_FRAGMENT, userService
                            .getActiveUserId());

            displayUpdatePhoneDialog();
        }
        else if (position == 2)
        {
            boolean isWhatsappInstalled = AccountsService
                    .appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {

                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.WHATSAPP_INVITE_CLICKED_ACCOUNTS_FRAGMENT, userService
                                .getActiveUserId());

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, userService
                        .getActiveUserName() + AppConstants.WHATSAPP_INVITATION_MESSAGE + AppConstants.APP_LINK);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);

            }
            else
            {
                Snackbar.make(getView(), R.string.whatsapp_not_installed, Snackbar.LENGTH_LONG)
                        .show();
            }
        }
        else if (position == 3)
        {

            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.SHARE_FEEDBACK_CLICKED, userService
                            .getActiveUserId());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_share_feedback, null);
            builder.setView(dialogView);

            final EditText commentMessage = (EditText) dialogView
                    .findViewById(R.id.et_alert_dialog_share_feedback_comment);
            RadioButton bug = (RadioButton) dialogView.findViewById(R.id.rb_share_feedback_bug);
            RadioButton newFeature = (RadioButton) dialogView
                    .findViewById(R.id.rb_share_feedback_new_feature);
            RadioButton suggestion = (RadioButton) dialogView
                    .findViewById(R.id.rb_share_feedback_other);
            final RadioGroup radioGroup = (RadioGroup) dialogView
                    .findViewById(R.id.rg_share_feedback);

            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                       .setOnClickListener(new View.OnClickListener()
                       {
                           @Override
                           public void onClick(View view)
                           {

                               int type = 0;

                               switch (radioGroup.getCheckedRadioButtonId())
                               {
                                   case R.id.rb_share_feedback_bug:
                                       type = 0;
                                       break;
                                   case R.id.rb_share_feedback_new_feature:
                                       type = 1;
                                       break;
                                   case R.id.rb_share_feedback_other:
                                       type = 2;
                                       break;
                               }

                               String comment = commentMessage.getText().toString();
                               Boolean wantToCloseDialog = false;

                               if (comment == null || comment.isEmpty())
                               {
                                   Snackbar.make(getView(), R.string.empty_rating, Snackbar.LENGTH_SHORT)
                                           .show();
                                   wantToCloseDialog = false;
                               }
                               else
                               {

                                   AnalyticsHelper
                                           .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.FEEDBACK_SHARED, userService
                                                   .getActiveUserId());

                                   userService.shareFeedback(type, comment);
                                   Snackbar.make(getView(), R.string.feedback_submitted, Snackbar.LENGTH_SHORT)
                                           .show();
                                   wantToCloseDialog = true;
                               }

                               if (wantToCloseDialog)
                               {
                                   alertDialog.dismiss();
                               }
                           }
                       });
        }
        else if (position == 4)
        {

            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.FAQ_CLICKED_ACCOUNTS_FRAGMENT, userService
                            .getActiveUserId());
        }
    }

    private void displayUpdatePhoneDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView
                .findViewById(R.id.et_alert_dialog_add_phone);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                   .setOnClickListener(new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                           Boolean wantToCloseDialog = false;
                           String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText()
                                                                                 .toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                           if (parsedPhone == null)
                           {
                               Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG)
                                       .show();
                               wantToCloseDialog = false;
                           }
                           else
                           {

                               AnalyticsHelper
                                       .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.PHONE_NUMBER_UPDATED, userService
                                               .getActiveUserId());

                               userService.updatePhoneNumber(parsedPhone);


                               InputMethodManager inputManager = (InputMethodManager) getActivity()
                                       .getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.hideSoftInputFromWindow(dialogView
                                       .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                               wantToCloseDialog = true;
                           }

                           if (wantToCloseDialog)
                           {
                               alertDialog.dismiss();
                           }
                       }
                   });

    }

    @Subscribe
    public void onCoverPicFetched(final FacebookCoverPicFetchedTrigger trigger)
    {

        genericCache.put(CacheKeys.USER_COVER_PIC, trigger.getSource());

        Picasso.with(getActivity())
               .load(trigger.getSource())
               .placeholder(personDrawable)
               .fit()
               .centerCrop()
               .noFade()
               .into(userPic);
    }
}
