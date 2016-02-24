package reaper.android.app.ui.screens.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.common.notification.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationsViewHolder>
{
    private Context context;
    private List<Notification> notifications;
    private NotificationClickListener notificationClickListener;

    /* Notification Icons */
    private Drawable chatDrawable;
    private Drawable rsvpDrawable;
    private Drawable friendAddedDrawable;
    private Drawable eventUpdatedDrawable;
    private Drawable eventRemovedDrawable;
    private Drawable eventInvitationDrawable;
    private Drawable eventCreatedDrawable;
    private Drawable statusDrawable;

    public NotificationAdapter(Context context, List<Notification> notifications, NotificationClickListener notificationClickListener)
    {
        this.context = context;
        this.notifications = notifications;
        this.notificationClickListener = notificationClickListener;

        generateDrawables();
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context)
                                  .inflate(R.layout.list_item_notification, parent, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position)
    {
        Notification notification = notifications.get(position);
        holder.render(notification);
    }

    @Override
    public int getItemCount()
    {
        return notifications.size();
    }

    public interface NotificationClickListener
    {
        void onNotificationClicked(Notification notification);
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.ivIcon)
        ImageView ivIcon;

        @Bind(R.id.tvMessage)
        TextView tvMessage;

        public NotificationsViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (notificationClickListener != null)
                    {
                        notificationClickListener
                                .onNotificationClicked(notifications.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void render(Notification notification)
        {
            tvMessage.setText(notification.getMessage());

            switch (notification.getType())
            {
                case Notification.CHAT:
                    ivIcon.setImageDrawable(chatDrawable);
                    break;
                case Notification.NEW_FRIEND_ADDED:
                    ivIcon.setImageDrawable(friendAddedDrawable);
                    break;

                case Notification.EVENT_UPDATED:
                    ivIcon.setImageDrawable(eventUpdatedDrawable);
                    break;

                case Notification.EVENT_REMOVED:
                    ivIcon.setImageDrawable(eventRemovedDrawable);
                    break;

                case Notification.RSVP:
                    ivIcon.setImageDrawable(rsvpDrawable);
                    break;

                case Notification.EVENT_INVITATION:
                    ivIcon.setImageDrawable(eventInvitationDrawable);
                    break;

                case Notification.EVENT_CREATED:
                    ivIcon.setImageDrawable(eventCreatedDrawable);
                    break;

                case Notification.STATUS:
                    ivIcon.setImageDrawable(statusDrawable);
                    break;
            }
        }
    }

    private void generateDrawables()
    {
        chatDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_TEXT)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        rsvpDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        friendAddedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventUpdatedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.TABLE_EDIT)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventRemovedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_REMOVE)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventInvitationDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL_OPEN)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        eventCreatedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();

        statusDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.COMMENT_PROCESSING)
                .setColor(ContextCompat
                        .getColor(context, R.color.light_grey))
                .setSizeDp(48)
                .build();
    }
}
