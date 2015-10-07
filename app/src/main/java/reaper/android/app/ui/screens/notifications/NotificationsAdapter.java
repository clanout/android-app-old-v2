package reaper.android.app.ui.screens.notifications;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import reaper.android.R;
import reaper.android.common.notification.Notification;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {
    private Context context;
    private List<Notification> notifications;
    private Drawable chatDrawable;
    private NotificationClickCommunicator communicator;
    private Drawable personDrawable;
    private Drawable rsvpDrawable;

    public NotificationsAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
        generateDrawables();
    }

    private void generateDrawables() {
        chatDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_TEXT)
                .setColor(ContextCompat.getColor(context, R.color.grey))
                .setSizeDp(24)
                .build();

        personDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT)
                .setColor(ContextCompat.getColor(context, R.color.grey))
                .setSizeDp(24)
                .build();

        rsvpDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(ContextCompat.getColor(context, R.color.grey))
                .setSizeDp(24)
                .build();
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false);
        NotificationsViewHolder holder = new NotificationsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.render(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setCommunicator(NotificationClickCommunicator communicator) {
        this.communicator = communicator;
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView notificationIcon;
        private TextView notificationMessage;

        public NotificationsViewHolder(View itemView) {
            super(itemView);
            notificationIcon = (CircleImageView) itemView.findViewById(R.id.iv_list_item_notifiactions);
            notificationMessage = (TextView) itemView.findViewById(R.id.tv_list_item_notifications);

            notificationIcon.setImageDrawable(personDrawable);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (communicator != null) {
                communicator.onNotificationClicked(notifications.get(getAdapterPosition()));
            }
        }

        public void render(Notification notification) {
            notificationMessage.setText(notification.getMessage());

            switch (notification.getType()) {
                case Notification.CHAT:
                    notificationIcon.setImageDrawable(chatDrawable);
                    break;
                case Notification.NEW_FRIEND_ADDED:

                    Picasso.with(context)
                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?height=500")
                            .placeholder(personDrawable)
                            .fit()
                            .centerCrop()
                            .noFade()
                            .into(notificationIcon);
                    break;
                case Notification.EVENT_UPDATED:

                    Picasso.with(context)
                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?height=500")
                            .placeholder(personDrawable)
                            .fit()
                            .centerCrop()
                            .noFade()
                            .into(notificationIcon);
                    break;
                case Notification.EVENT_REMOVED:

                    Picasso.with(context)
                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?height=500")
                            .placeholder(personDrawable)
                            .fit()
                            .centerCrop()
                            .noFade()
                            .into(notificationIcon);
                    break;
                case Notification.RSVP:

                    notificationIcon.setImageDrawable(rsvpDrawable);
                    break;
                case Notification.EVENT_INVITATION:

                    Picasso.with(context)
                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?height=500")
                            .placeholder(personDrawable)
                            .fit()
                            .centerCrop()
                            .noFade()
                            .into(notificationIcon);
                    break;
                case Notification.EVENT_CREATED:

                    Picasso.with(context)
                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?height=500")
                            .placeholder(personDrawable)
                            .fit()
                            .centerCrop()
                            .noFade()
                            .into(notificationIcon);
                    break;
            }
        }
    }
}
