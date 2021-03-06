package reaper.android.app.ui.screens.accounts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.User;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.FacebookService_;
import reaper.android.app.service._new.WhatsappService_;
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
                .setSizeDp(Dimensions.PROFILE_PIC_LARGE)
                .build();

        Picasso.with(getActivity())
               .load(sessionUser.getCoverPicUrl())
               .placeholder(personDrawable)
               .fit()
               .centerCrop()
               .noFade()
               .into(ivCoverPic);

        Picasso.with(getActivity())
               .load(FacebookService_
                       .getProfilePicUrl(sessionUser.getId(), Dimensions.PROFILE_PIC_LARGE))
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
                        /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_FRIENDS);
                /* Analytics */

                screen.navigateToFriendsScreen();
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.llUpdateMobileNumber)
    public void onUpdateMobileClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendScreenNames(GoogleAnalyticsConstants.SCREEN_UPDATE_PHONE_DIALOG_FROM_ACCOUNTS);
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_UPDATE_MOBILE);
        /* Analytics */

        UpdateMobileDialog.show(getActivity(), new UpdateMobileDialog.Listener()
        {
            @Override
            public void onSuccess(String mobileNumber)
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_UPDATE_PHONE, GoogleAnalyticsConstants.LABEL_SUCCESS);
                /* Analytics */
            }
        });

    }

    @OnClick(R.id.llWhatsAppInvite)
    public void onWhatsAppInviteClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_WHATSAPP_INVITE);
        /* Analytics */


        WhatsappService_ accountsService = WhatsappService_.getInstance();
        if (accountsService.isWhatsAppInstalled(getActivity()))
        {
            /* Analytics */
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_WHATSAPP_INVITE, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */

            startActivity(accountsService.getWhatsAppIntent());
        }
        else
        {
            /* Analytics */
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_WHATSAPP_INVITE, GoogleAnalyticsConstants.LABEL_FAILURE);
            /* Analytics */

            SnackbarFactory.create(getActivity(), R.string.error_no_whatsapp);
        }
    }

    @OnClick(R.id.llFeedback)
    public void onFeedbackClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_FEEDBACK_DIALOG);
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_FEEDBACK);
        /* Analytics */

        FeedbackDialog.show(getActivity(), new FeedbackDialog.Listener()
        {
            @Override
            public void onSuccess(int feedbackType, String comment)
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_FEEDBACK, GoogleAnalyticsConstants.LABEL_SUCCESS + "-" + String
                                .valueOf(feedbackType));
                /* Analytics */

            }

            @Override
            public void onCancel()
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_FEEDBACK, GoogleAnalyticsConstants.LABEL_CANCEL);
                /* Analytics */
            }
        });
    }

    @OnClick(R.id.llFaq)
    public void onFaqClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_FAQ);
        /* Analytics */

        String url = "http://www.clanout.com/faq.html";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
