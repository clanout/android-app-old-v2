package reaper.android.app.ui.screens.accounts;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.User;
import reaper.android.app.service._new.WhatsappService_;
import reaper.android.app.service.UserService;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.FeedbackDialog;
import reaper.android.app.ui.dialog.UpdateMobileDialog;
import reaper.android.app.ui.util.CircleTransform;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.common.analytics.AnalyticsHelper;

public class AccountFragment extends BaseFragment
{
    public static AccountFragment newInstance()
    {
        return new AccountFragment();
    }

    AccountScreen screen;

    /* UI Elements */
    @Bind(R.id.ivCoverPic)
    ImageView ivCoverPic;

    @Bind(R.id.ivProfilePic)
    ImageView ivProfilePic;

    @Bind(R.id.tvName)
    TextView tvName;

    @Bind(R.id.llBlockFriends)
    View llBlockFriends;

    /* Lifecycle Methods */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        screen = (AccountScreen) getActivity();

        /* User Service */
        UserService userService = UserService.getInstance();
        User sessionUser = userService.getSessionUser();

        /* Init User Details */
        tvName.setText(sessionUser.getName());

        Drawable personDrawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(getActivity(), R.color.light_grey))
                .setSizeDp(36)
                .build();

        Picasso.with(getActivity())
                .load(sessionUser.getCoverPicUrl())
                .placeholder(personDrawable)
                .fit()
                .centerCrop()
                .noFade()
                .into(ivCoverPic);

        Picasso.with(getActivity())
                .load(sessionUser.getProfilePicUrl())
                .placeholder(personDrawable)
                .fit()
                .centerCrop()
                .noFade()
                .transform(new CircleTransform())
                .into(ivProfilePic);

        llBlockFriends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                screen.navigateToFriendsScreen();
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.llUpdateMobileNumber)
    public void onUpdateMobileClicked()
    {
        UpdateMobileDialog.show(getActivity(), new UpdateMobileDialog.Listener()
        {
            @Override
            public void onSuccess(String mobileNumber)
            {

            }
        });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_UPDATE_PHONE_DIALOG_FROM_ACCOUNTS);
    }

    @OnClick(R.id.llWhatsAppInvite)
    public void onWhatsAppInviteClicked()
    {
        WhatsappService_ accountsService = WhatsappService_.getInstance();
        if (accountsService.isWhatsAppInstalled(getActivity())) {
            startActivity(accountsService.getWhatsAppIntent());
        }
        else {
            SnackbarFactory.create(getActivity(), R.string.error_no_whatsapp);
        }
    }

    @OnClick(R.id.llFeedback)
    public void onFeedbackClicked()
    {
        FeedbackDialog.show(getActivity(), new FeedbackDialog.Listener()
        {
            @Override
            public void onSuccess(int feedbackType, String comment)
            {

            }

            @Override
            public void onCancel()
            {

            }
        });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_FEEDBACK_DIALOG);
        /* Analytics */
    }

    @OnClick(R.id.llFaq)
    public void onFaqClicked()
    {

    }
}
