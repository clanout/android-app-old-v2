package reaper.android.app.ui.screens.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.CircleTransform;
import reaper.android.app.ui.util.DrawableFactory;
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
    private Drawable friendAddedDrawable;
    private Drawable eventUpdatedDrawable;
    private Drawable eventRemovedDrawable;
    private Drawable eventInvitationDrawable;
    private Drawable eventCreatedDrawable;
    private Drawable statusDrawable;

    public NotificationsAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
        generateDrawables();
    }

    private void generateDrawables() {
        chatDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_TEXT)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        personDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_BOX)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        rsvpDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        friendAddedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_PLUS)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventUpdatedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.TABLE_EDIT)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventRemovedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_REMOVE)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventInvitationDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL_OPEN)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventCreatedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_PLUS)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        statusDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.COMMENT_PROCESSING)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
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
//        private LinearLayout notificationIconContainer;
        private ImageView notificationIcon;
        private TextView notificationMessage;

        public NotificationsViewHolder(View itemView) {
            super(itemView);
            notificationIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_notifications);
            notificationMessage = (TextView) itemView.findViewById(R.id.tv_list_item_notifications);
//            notificationIconContainer = (LinearLayout) itemView.findViewById(R.id.llNotificationIconContainer);
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
//            notificationIconContainer.setBackground(DrawableFactory.getIconBackground(context, R.color.primary_light, 4));

            switch (notification.getType()) {
                case Notification.CHAT:
                    notificationIcon.setImageDrawable(chatDrawable);
                    break;
                case Notification.NEW_FRIEND_ADDED:

//                    Picasso.with(context)
//                            .load("https://graph.facebook.com/v2.4/" + notification.getUserId() + "/picture?width=500")
//                            .placeholder(personDrawable)
//                            .transform(new CircleTransform())
//                            .into(notificationIcon);

                    notificationIcon.setImageDrawable(friendAddedDrawable);
                    break;

                case Notification.EVENT_UPDATED:

                    notificationIcon.setImageDrawable(eventUpdatedDrawable);
                    break;
                case Notification.EVENT_REMOVED:

                    notificationIcon.setImageDrawable(eventRemovedDrawable);
                    break;
                case Notification.RSVP:

                    notificationIcon.setImageDrawable(rsvpDrawable);
                    break;
                case Notification.EVENT_INVITATION:

                    notificationIcon.setImageDrawable(eventInvitationDrawable);
                    break;
                case Notification.EVENT_CREATED:

                    notificationIcon.setImageDrawable(eventCreatedDrawable);
                    break;
                case Notification.STATUS:

                    notificationIcon.setImageDrawable(statusDrawable);
                    break;
            }
        }
    }
}
