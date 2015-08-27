package reaper.android.app.ui.screens.accounts.friends;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.Friend;

/**
 * Created by harsh on 13-05-2015.
 */
public class ManageFriendsAdapter extends RecyclerView.Adapter<ManageFriendsAdapter.ManageFriendsViewHolder>
{

    private LayoutInflater inflater;
    private List<Friend> friends;
    private BlockListCommunicator blockListCommunicator;
    private Context context;
    private List<Boolean> blockStatusList;

    public ManageFriendsAdapter(Context context, List<Friend> friends, BlockListCommunicator blockListCommunicator)
    {
        inflater = LayoutInflater.from(context);
        this.friends = friends;
        this.context = context;
        this.blockListCommunicator = blockListCommunicator;
        blockStatusList = new ArrayList<>();
    }

    @Override
    public ManageFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.list_item_manage_friends, parent, false);
        ManageFriendsViewHolder holder = new ManageFriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ManageFriendsViewHolder holder, int position)
    {
        Friend current = friends.get(position);

        Picasso.with(context)
                .load(AppConstants.FACEBOOK_END_POINT + current.getId() + "/picture")
                .placeholder(R.drawable.ic_person_black_36dp)
                .fit()
                .centerInside()
                .into(holder.userPic);

        holder.username.setText(current.getName());

        if (current.isBlocked())
        {
            holder.blockIcon.setImageResource(R.drawable.ic_action_important);
            blockStatusList.add(position, true);
        }
        else
        {
            holder.blockIcon.setImageResource(R.drawable.ic_action_not_important);
            blockStatusList.add(position, false);
        }

    }

    @Override
    public int getItemCount()
    {
        return friends.size();
    }

    class ManageFriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        ImageView userPic;
        TextView username;
        ImageButton blockIcon;

        public ManageFriendsViewHolder(View itemView)
        {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tv_list_item_manage_friends_user_name);
            userPic = (ImageView) itemView.findViewById(R.id.iv_list_item_manage_friends_user_pic);
            blockIcon = (ImageButton) itemView.findViewById(R.id.ib_list_item_manage_friends_block);

            blockIcon.setOnClickListener(this);
        }


        @Override
        public void onClick(View view)
        {
            if (blockStatusList.get(getAdapterPosition()))
            {
                blockStatusList.set(getAdapterPosition(), false);
                blockIcon.setImageResource(R.drawable.ic_action_not_important);
            }
            else
            {
                blockStatusList.set(getAdapterPosition(), true);
                blockIcon.setImageResource(R.drawable.ic_action_important);
            }

            if(blockListCommunicator!=null)
            {
                blockListCommunicator.toggleBlock(friends.get(getAdapterPosition()).getId());
            }

        }
    }
}
