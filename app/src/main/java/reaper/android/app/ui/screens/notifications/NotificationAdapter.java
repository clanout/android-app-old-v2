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
import reaper.android.app.model.NotificationWrapper;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationsViewHolder>
{
    private Context context;
    private List<NotificationWrapper> notifications;
    private NotificationClickListener notificationClickListener;

    /* Notification Icons */
    private Drawable chatDrawable;
    private Drawable rsvpDrawable;
    private Drawable friendAddedDrawable;
    private Drawable eventUpdatedDrawable;
    private Drawable eventRemovedDrawable;
    private Drawable eventInvitationDrawable;

    public NotificationAdapter(Context context, List<NotificationWrapper> notifications, NotificationClickListener notificationClickListener)
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
                                  .inflate(R.layout.item_notification, parent, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position)
    {
        NotificationWrapper notification = notifications.get(position);
        holder.render(notification);
    }

    @Override
    public int getItemCount()
    {
        return notifications.size();
    }

    public interface NotificationClickListener
    {
        void onNotificationClicked(NotificationWrapper notification);
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvTitle)
        TextView tvTitle;

        @Bind(R.id.llItem1)
        View llItem1;

        @Bind(R.id.ivIcon1)
        ImageView ivIcon1;

        @Bind(R.id.tvMessage1)
        TextView tvMessage1;

        @Bind(R.id.llItem2)
        View llItem2;

        @Bind(R.id.ivIcon2)
        ImageView ivIcon2;

        @Bind(R.id.tvMessage2)
        TextView tvMessage2;

        @Bind(R.id.llItem3)
        View llItem3;

        @Bind(R.id.ivIcon3)
        ImageView ivIcon3;

        @Bind(R.id.tvMessage3)
        TextView tvMessage3;

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

        public void render(NotificationWrapper notification)
        {
            int type = notification.getType();

            llItem1.setVisibility(View.GONE);
            llItem2.setVisibility(View.GONE);
            llItem3.setVisibility(View.GONE);

            NotificationWrapper.NotificationItem item;
            switch (type)
            {
                case NotificationWrapper.Type.EVENT_ACTIVITY:
                    tvTitle.setText(notification.getTitle());
                    int size = notification.getNotificationItems().size();
                    if (size >= 1)
                    {
                        llItem1.setVisibility(View.VISIBLE);
                        item = notification.getNotificationItems().get(0);
                        renderItem(item, ivIcon1, tvMessage1);
                    }
                    if (size >= 2)
                    {
                        llItem2.setVisibility(View.VISIBLE);
                        item = notification.getNotificationItems().get(1);
                        renderItem(item, ivIcon2, tvMessage2);
                    }
                    if (size >= 3)
                    {
                        llItem3.setVisibility(View.VISIBLE);
                        item = notification.getNotificationItems().get(2);
                        renderItem(item, ivIcon3, tvMessage3);
                    }
                    break;

                case NotificationWrapper.Type.EVENT_INVITATION:
                    tvTitle.setText(notification.getTitle());
                    llItem1.setVisibility(View.VISIBLE);
                    item = notification.getNotificationItems().get(0);
                    renderItem(item, ivIcon1, tvMessage1);
                    break;

                case NotificationWrapper.Type.EVENT_REMOVED:
                    tvTitle.setText(notification.getTitle());
                    llItem1.setVisibility(View.VISIBLE);
                    item = notification.getNotificationItems().get(0);
                    renderItem(item, ivIcon1, tvMessage1);
                    break;

                case NotificationWrapper.Type.NEW_FRIEND_JOINED_APP:
                    tvTitle.setText("New Friends");
                    llItem1.setVisibility(View.VISIBLE);
                    item = notification.getNotificationItems().get(0);
                    renderItem(item, ivIcon1, tvMessage1);
                    break;
            }
        }

        private void renderItem(NotificationWrapper.NotificationItem item, ImageView ivIcon, TextView tvMessage)
        {
            int type = item.getType();
            switch (type)
            {
                case NotificationWrapper.NotificationItem.Type.EVENT_REMOVED:
                    ivIcon.setImageDrawable(eventRemovedDrawable);
                    tvMessage.setText("This clan has been removed");
                    break;

                case NotificationWrapper.NotificationItem.Type.NEW_FRIEND_JOINED_APP:
                    ivIcon.setImageDrawable(friendAddedDrawable);
                    tvMessage.setText(item.getMessage());
                    break;

                case NotificationWrapper.NotificationItem.Type.INVITATION:
                    ivIcon.setImageDrawable(eventInvitationDrawable);
                    tvMessage.setText(item.getMessage());
                    break;

                case NotificationWrapper.NotificationItem.Type.EVENT_UPDATED:
                    ivIcon.setImageDrawable(eventUpdatedDrawable);
                    tvMessage.setText("Details updated");
                    break;

                case NotificationWrapper.NotificationItem.Type.NEW_CHAT:
                    ivIcon.setImageDrawable(chatDrawable);
                    tvMessage.setText("New chat");
                    break;

                case NotificationWrapper.NotificationItem.Type.FRIEND_JOINED_EVENT:
                    ivIcon.setImageDrawable(rsvpDrawable);
                    tvMessage.setText("New friends have joined");
                    break;
            }
        }
    }

    private void generateDrawables()
    {
        chatDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_TEXT)
                .setColor(ContextCompat.getColor(context, R.color.primary_light))
                .build();

        rsvpDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.primary_light))
                .build();

        friendAddedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.primary_light))
                .build();

        eventUpdatedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.TABLE_EDIT)
                .setColor(ContextCompat
                        .getColor(context, R.color.primary_light))
                .build();

        eventRemovedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_REMOVE)
                .setColor(ContextCompat
                        .getColor(context, R.color.primary_light))
                .build();

        eventInvitationDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL_OPEN)
                .setColor(ContextCompat
                        .getColor(context, R.color.primary_light))
                .build();
    }
}
