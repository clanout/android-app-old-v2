package reaper.android.app.ui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service._new.FacebookService_;
import reaper.android.app.ui.util.CircleTransform;

/**
 * Created by harsh on 02/04/16.
 */
public class AttendeeDialog
{

    public static void show(Context context, EventDetails.Attendee attendee, Drawable personDrawable)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_attendee, null);
        builder.setView(dialogView);

        final ImageView userPic = (ImageView) dialogView
                .findViewById(R.id.ivPic);
        final ImageView frindIcon = (ImageView) dialogView
                .findViewById(R.id.ivFriendIcon);
        final TextView userName = (TextView) dialogView
                .findViewById(R.id.tvName);
        final TextView isFriend = (TextView) dialogView
                .findViewById(R.id.tvFriend);
        final TextView hasInvited = (TextView) dialogView
                .findViewById(R.id.tvInvite);
        final TextView status = (TextView) dialogView
                .findViewById(R.id.tvStatus);

        Picasso.with(context)
                .load(FacebookService_.getProfilePicUrl(attendee.getId(), Dimensions.PROFILE_PIC_DEFAULT))
                .placeholder(personDrawable)
                .transform(new CircleTransform())
                .into(userPic);

        userName.setText(attendee.getName());

        if(attendee.isFriend())
        {
            isFriend.setVisibility(View.VISIBLE);
            frindIcon.setVisibility(View.VISIBLE);
        }else{

            isFriend.setVisibility(View.GONE);
            frindIcon.setVisibility(View.GONE);
        }

        if(attendee.isInviter())
        {
            hasInvited.setVisibility(View.VISIBLE);
        }else{

            hasInvited.setVisibility(View.GONE);
        }


        if(attendee.getStatus() == null)
        {
            status.setText(R.string.no_status);
        }else if(attendee.getStatus().isEmpty())
        {
            status.setText(R.string.no_status);
        }else{

            status.setText(attendee.getStatus());
        }

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
