package reaper.android.app.ui.screens.invite.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.trigger.user.ManageAppFriendsTrigger;
import reaper.android.app.ui.util.CircleTransform;

/**
 * Created by harsh on 13-05-2015.
 */
public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.InviteFriendsViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private List<EventDetails.Invitee> invitees;
    private List<Friend> friends;
    private Bus bus;
    private Event event;
    private Drawable personDrawable, goingDrawable, blockedDrawable;
    private ArrayList<EventDetails.Attendee> attendeeList;

    public InviteFriendsAdapter(Context context, List<EventDetails.Invitee> invitees, List<Friend> friends, Bus bus, Event event, ArrayList<EventDetails.Attendee> attendeeList) {
        inflater = LayoutInflater.from(context);
        this.invitees = invitees;
        this.friends = friends;
        this.context = context;
        this.bus = bus;
        this.event = event;
        this.attendeeList = attendeeList;

        generateDrawables();
    }

    private void generateDrawables() {
        personDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(24)
                .build();

        goingDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CHECKBOX_MARKED_CIRCLE_OUTLINE)
                .setColor(ContextCompat.getColor(context, R.color.green))
                .setSizeDp(24)
                .build();

        blockedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.RED)
                .setSizeDp(24)
                .build();
    }

    @Override
    public InviteFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_invite_friends, parent, false);
        InviteFriendsViewHolder holder = new InviteFriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InviteFriendsViewHolder holder, final int position) {
        final Friend current = friends.get(position);


        holder.userPic.setVisibility(View.VISIBLE);

        Picasso.with(context)
                .load(AppConstants.BASE_URL_FACEBOOK_API + current.getId() + "/picture?width=500")
                .placeholder(personDrawable)
                .transform(new CircleTransform())
                .into(holder.userPic);


        holder.username.setText(current.getName());

        EventDetails.Invitee invitee = new EventDetails.Invitee();
        invitee.setId(current.getId());

        if (event.getOrganizerId().equals(current.getId())) {

            holder.checkBox.setVisibility(View.GONE);
            holder.alreadyInvited.setImageDrawable(goingDrawable);
            holder.alreadyInvited.setVisibility(View.VISIBLE);
        } else {

            if (current.isBlocked()) {
                holder.checkBox.setVisibility(View.GONE);
                holder.alreadyInvited.setImageDrawable(blockedDrawable);
                holder.alreadyInvited.setVisibility(View.VISIBLE);
            } else {
                EventDetails.Attendee attendee = new EventDetails.Attendee();
                attendee.setId(current.getId());

                if (attendeeList.contains(attendee)) {
                    holder.checkBox.setVisibility(View.GONE);
                    holder.alreadyInvited.setImageDrawable(goingDrawable);
                    holder.alreadyInvited.setVisibility(View.VISIBLE);
                } else {

                    if (invitees.contains(invitee)) {
                        holder.checkBox.setVisibility(View.VISIBLE);
                        holder.checkBox.setChecked(true);
                        holder.checkBox.setEnabled(false);
                        holder.checkBox.setClickable(false);
                        //holder.alreadyInvited.setText("Invited");
                        holder.alreadyInvited.setVisibility(View.GONE);
                    } else {
                        holder.alreadyInvited.setVisibility(View.GONE);
                        holder.checkBox.setVisibility(View.VISIBLE);

                        holder.checkBox.setChecked(current.isChecked());
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class InviteFriendsViewHolder extends RecyclerView.ViewHolder {

        ImageView userPic, alreadyInvited;
        TextView username;
        CheckBox checkBox;

        public InviteFriendsViewHolder(View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tv_list_item_invite_friends_user_name);
            userPic = (ImageView) itemView.findViewById(R.id.iv_list_item_invite_friends_user_pic);
            alreadyInvited = (ImageView) itemView.findViewById(R.id.iv_list_item_invite_friends_already_invited);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_list_item_invite_friends);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    friends.get(getAdapterPosition()).setIsChecked(isChecked);

                    bus.post(new ManageAppFriendsTrigger(friends.get(getAdapterPosition()).getId(), isChecked));

                }
            });
        }
    }
}
