package reaper.android.app.ui.screens.accounts.friends;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Friend;
import reaper.android.app.root.Reaper;
import reaper.android.common.analytics.AnalyticsHelper;

/**
 * Created by harsh on 13-05-2015.
 */
public class ManageFriendsAdapter extends RecyclerView.Adapter<ManageFriendsAdapter.ManageFriendsViewHolder> {

    private LayoutInflater inflater;
    private List<Friend> friends;
    private BlockListCommunicator blockListCommunicator;
    private Context context;
    private Drawable blockedDrawable, unblockedDrawable, personDrawable;

    public ManageFriendsAdapter(Context context, List<Friend> friends, BlockListCommunicator blockListCommunicator) {
        inflater = LayoutInflater.from(context);
        this.friends = friends;
        this.context = context;
        this.blockListCommunicator = blockListCommunicator;

        generateDrawables();
    }

    private void generateDrawables() {
        blockedDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.RED)
                .setSizeDp(24)
                .build();

        unblockedDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.LTGRAY)
                .setSizeDp(24)
                .build();

        personDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT)
                .setColor(Color.BLACK)
                .setSizeDp(24)
                .build();
    }

    @Override
    public ManageFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_manage_friends, parent, false);
        ManageFriendsViewHolder holder = new ManageFriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ManageFriendsViewHolder holder, int position) {
        Friend current = friends.get(position);

        Picasso.with(context)
                .load(AppConstants.FACEBOOK_END_POINT + current.getId() + "/picture?width=500")
                .placeholder(personDrawable)
                .fit()
                .centerInside()
                .into(holder.userPic);

        holder.username.setText(current.getName());

        if (current.isBlocked()) {
            holder.blockIcon.setImageDrawable(blockedDrawable);
        } else {
            holder.blockIcon.setImageDrawable(unblockedDrawable);
        }

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ManageFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircularImageView userPic;
        TextView username;
        ImageView blockIcon;

        public ManageFriendsViewHolder(View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tv_list_item_manage_friends_user_name);
            userPic = (CircularImageView) itemView.findViewById(R.id.iv_list_item_manage_friends_user_pic);
            blockIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_manage_friends_block);

            blockIcon.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (friends.get(getAdapterPosition()).isBlocked()) {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.PERSON_UNBLOCKED, null);
                blockIcon.setImageDrawable(unblockedDrawable);
                friends.get(getAdapterPosition()).setBlocked(false);
                blockListCommunicator.toggleBlock(friends.get(getAdapterPosition()).getId(), false);
            } else {
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.PERSON_BLOCKED, null);
                blockIcon.setImageDrawable(blockedDrawable);
                friends.get(getAdapterPosition()).setBlocked(true);
                blockListCommunicator.toggleBlock(friends.get(getAdapterPosition()).getId(), true);
            }
        }
    }
}
