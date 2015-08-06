package reaper.android.app.ui.screens.invite.core;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.trigger.user.ManageFacebookFriendsTrigger;
import reaper.android.app.trigger.user.ManagePhoneContactsTrigger;

/**
 * Created by harsh on 13-05-2015.
 */
public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.InviteFriendsViewHolder>
{

    private LayoutInflater inflater;
    private Context context;
    private List<EventDetails.Invitee> invitees;
    private List<Friend> friends;
    private boolean isFacebookAdapter;
    private Bus bus;

    public InviteFriendsAdapter(Context context, List<EventDetails.Invitee> invitees, List<Friend> friends, boolean isFacebookAdapter, Bus bus)
    {
        inflater = LayoutInflater.from(context);
        this.invitees = invitees;
        this.friends = friends;
        this.context = context;
        this.isFacebookAdapter = isFacebookAdapter;
        this.bus = bus;
    }

    @Override
    public InviteFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.list_item_invite_friends, parent, false);
        InviteFriendsViewHolder holder = new InviteFriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InviteFriendsViewHolder holder, int position)
    {
        Friend current = friends.get(position);

        if (isFacebookAdapter)
        {
            holder.userPic.setVisibility(View.VISIBLE);
            holder.userPic.setImageResource(R.drawable.ic_local_bar_black_36dp);
        }
        else
        {
            holder.userPic.setVisibility(View.GONE);
        }

        holder.username.setText(current.getName());

        EventDetails.Invitee invitee = new EventDetails.Invitee();
        invitee.setId(current.getId());

        if (current.isBlocked())
        {
            holder.checkBox.setVisibility(View.GONE);
            holder.alreadyInvited.setText("Blocked");
            holder.alreadyInvited.setVisibility(View.VISIBLE);
        }
        else
        {
            if (invitees.contains(invitee))
            {
                holder.checkBox.setVisibility(View.GONE);
                holder.alreadyInvited.setText("Already Invited");
                holder.alreadyInvited.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.alreadyInvited.setVisibility(View.GONE);
                holder.checkBox.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return friends.size();
    }

    class InviteFriendsViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
    {

        ImageView userPic;
        TextView username, alreadyInvited;
        CheckBox checkBox;

        public InviteFriendsViewHolder(View itemView)
        {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tv_list_item_invite_friends_user_name);
            userPic = (ImageView) itemView.findViewById(R.id.iv_list_item_invite_friends_user_pic);
            alreadyInvited = (TextView) itemView.findViewById(R.id.tv_list_item_invite_friends_already_invited);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_list_item_invite_friends);

            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b)
        {
            if (isFacebookAdapter)
            {
               bus.post(new ManageFacebookFriendsTrigger(friends.get(getAdapterPosition()).getId()));
            }
            else
            {
               bus.post(new ManagePhoneContactsTrigger(friends.get(getAdapterPosition()).getId()));
            }
        }
    }
}
