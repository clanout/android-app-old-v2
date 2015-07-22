package reaper.android.app.ui.screens.invite;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;

/**
 * Created by harsh on 13-05-2015.
 */
public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.InviteFriendsViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<EventDetails.Invitee> invitees = Collections.emptyList();
    private List<Friend> friends;

    public InviteFriendsAdapter(Context context, List<EventDetails.Invitee> invitees, List<Friend> friends) {
        inflater = LayoutInflater.from(context);
        this.invitees = invitees;
        this.friends = friends;
        this.context = context;
    }

    @Override
    public InviteFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_invite_friends, parent, false);
        InviteFriendsViewHolder holder = new InviteFriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InviteFriendsViewHolder holder, int position) {
        Friend current = friends.get(position);

        holder.userPic.setImageResource(R.drawable.ic_local_bar_black_36dp);
        holder.username.setText(current.getName());

        EventDetails.Invitee invitee = new EventDetails.Invitee();
        invitee.setId(current.getId());

        if(invitees.contains(invitee)){
            holder.checkBox.setVisibility(View.GONE);
        }else{
            holder.alreadyInvited.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class InviteFriendsViewHolder extends RecyclerView.ViewHolder {

        ImageView userPic;
        TextView username, alreadyInvited;
        CheckBox checkBox;

        public InviteFriendsViewHolder(View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tv_list_item_invite_friends_user_name);
            userPic = (ImageView) itemView.findViewById(R.id.iv_list_item_invite_friends_user_pic);
            alreadyInvited = (TextView) itemView.findViewById(R.id.tv_list_item_invite_friends_already_invited);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_list_item_invite_friends);
        }
    }
}
