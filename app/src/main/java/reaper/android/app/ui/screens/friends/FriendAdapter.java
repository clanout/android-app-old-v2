package reaper.android.app.ui.screens.friends;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Friend;
import reaper.android.app.root.Reaper;
import reaper.android.app.ui.screens.friends.mvp.FriendsView;
import reaper.android.app.ui.util.CircleTransform;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>
{
    private Context context;
    private List<Friend> friends;
    private BlockListener blockListener;

    private Drawable blockedDrawable;
    private Drawable unblockedDrawable;
    private Drawable personDrawable;

    public FriendAdapter(Context context, List<Friend> friends, BlockListener blockListener)
    {
        this.context = context;
        this.friends = friends;
        this.blockListener = blockListener;

        generateDrawables();
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context)
                                  .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position)
    {
        holder.render(friends.get(position));
    }

    @Override
    public int getItemCount()
    {
        return friends.size();
    }

    public interface BlockListener
    {
        void onBlockToggled(Friend friend, FriendsView.FriendListItem friendListItem);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements FriendsView.FriendListItem
    {
        ImageView userPic;
        TextView username;
        ImageView blockIcon;

        public FriendViewHolder(View itemView)
        {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tvName);
            userPic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            blockIcon = (ImageView) itemView.findViewById(R.id.ivBlock);

            blockIcon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    blockListener.onBlockToggled(friends
                            .get(getAdapterPosition()), FriendViewHolder.this);
                }
            });
        }

        @Override
        public void render(Friend friend)
        {
            Picasso.with(context)
                   .load(friend.getProfilePicUrl())
                   .placeholder(personDrawable)
                   .transform(new CircleTransform())
                   .into(userPic);

            username.setText(friend.getName());

            if (friend.isBlocked())
            {
                blockIcon.setImageDrawable(blockedDrawable);
            }
            else
            {
                blockIcon.setImageDrawable(unblockedDrawable);
            }
        }
    }

    private void generateDrawables()
    {
        blockedDrawable = MaterialDrawableBuilder
                .with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.RED)
                .setSizeDp(24)
                .build();

        unblockedDrawable = MaterialDrawableBuilder
                .with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.LTGRAY)
                .setSizeDp(24)
                .build();

        personDrawable = MaterialDrawableBuilder
                .with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(24)
                .build();
    }
}
