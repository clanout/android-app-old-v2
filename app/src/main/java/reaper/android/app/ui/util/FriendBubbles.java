package reaper.android.app.ui.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Friend;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.FacebookService_;
import reaper.android.app.service._new.LocationService_;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FriendBubbles
{
    public static void render(final Context context, final View friendBubbles, final String title)
    {
        final TextView tvTitle = (TextView) friendBubbles.findViewById(R.id.tvTitle);
        final View friendBubblesContainer = friendBubbles.findViewById(R.id.bubblesContainer);
        final TextView tvOtherFriends = (TextView) friendBubbles.findViewById(R.id.tvOtherFriends);

        final ImageView ivFriend1 = (ImageView) friendBubblesContainer.findViewById(R.id.ivFriend1);
        final ImageView ivFriend2 = (ImageView) friendBubblesContainer.findViewById(R.id.ivFriend2);
        final ImageView ivFriend3 = (ImageView) friendBubblesContainer.findViewById(R.id.ivFriend3);

        friendBubblesContainer.setVisibility(View.GONE);
        tvOtherFriends.setVisibility(View.GONE);

        UserService userService = UserService.getInstance();
        userService._fetchLocalFacebookFriendsCache()
                   .subscribeOn(Schedulers.newThread())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Subscriber<List<Friend>>()
                   {
                       @Override
                       public void onCompleted()
                       {
                       }

                       @Override
                       public void onError(Throwable e)
                       {

                       }

                       @Override
                       public void onNext(List<Friend> friends)
                       {
                           Collections.shuffle(friends);

                           int i = 0;
                           for (Friend friend : friends)
                           {
                               if (i == 0)
                               {
                                   ivFriend1.setVisibility(View.VISIBLE);

                                   Picasso.with(context)
                                          .load(FacebookService_.getProfilePicUrl(friend
                                                  .getId(), Dimensions.PROFILE_PIC_DEFAULT))
                                          .placeholder(MaterialDrawableBuilder
                                                  .with(context)
                                                  .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                                                  .setColor(ContextCompat
                                                          .getColor(context, R.color.light_grey))
                                                  .setSizeDp(24)
                                                  .build())
                                          .transform(new CircleTransform())
                                          .into(ivFriend1);
                               }
                               else if (i == 1)
                               {
                                   ivFriend2.setVisibility(View.VISIBLE);

                                   Picasso.with(context)
                                          .load(FacebookService_.getProfilePicUrl(friend
                                                  .getId(), Dimensions.PROFILE_PIC_DEFAULT))
                                          .placeholder(MaterialDrawableBuilder
                                                  .with(context)
                                                  .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                                                  .setColor(ContextCompat
                                                          .getColor(context, R.color.light_grey))
                                                  .setSizeDp(24)
                                                  .build())
                                          .transform(new CircleTransform())
                                          .into(ivFriend2);
                               }
                               else if (i == 2)
                               {
                                   ivFriend3.setVisibility(View.VISIBLE);

                                   Picasso.with(context)
                                          .load(FacebookService_.getProfilePicUrl(friend
                                                  .getId(), Dimensions.PROFILE_PIC_DEFAULT))
                                          .placeholder(MaterialDrawableBuilder
                                                  .with(context)
                                                  .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                                                  .setColor(ContextCompat
                                                          .getColor(context, R.color.light_grey))
                                                  .setSizeDp(24)
                                                  .build())
                                          .transform(new CircleTransform())
                                          .into(ivFriend3);

                                   break;
                               }

                               i++;
                           }

                           if (i > 0)
                           {
                               String zone = LocationService_.getInstance().getCurrentLocation()
                                                             .getZone();
                               tvTitle.setText(String.format(title, zone));
                               friendBubblesContainer.setVisibility(View.VISIBLE);

                               if (friends.size() > 3)
                               {
                                   int other = friends.size() - 3;
                                   tvOtherFriends.setText("+" + other + " other friends");
                                   tvOtherFriends.setVisibility(View.VISIBLE);
                               }
                           }
                       }
                   });
    }
}
