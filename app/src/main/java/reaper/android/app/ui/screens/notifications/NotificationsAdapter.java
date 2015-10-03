package reaper.android.app.ui.screens.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import reaper.android.R;
import reaper.android.common.notification.Notification;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder>
{
    private Context context;
    private List<Notification> notifications;
    private Drawable eventCreatedDrawable;
    private NotificationClickCommunicator communicator;

    public NotificationsAdapter(Context context, List<Notification> notifications)
    {
        this.context = context;
        this.notifications = notifications;

        generateDrawables();
    }

    private void generateDrawables()
    {
        eventCreatedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD)
                .setColor(context.getResources().getColor(R.color.cyan))
                .setSizeDp(36)
                .build();
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false);
        NotificationsViewHolder holder = new NotificationsViewHolder(view);
        return holder;
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

    public void setCommunicator(NotificationClickCommunicator communicator)
    {
        this.communicator = communicator;
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ImageView notificationIcon;
        private TextView notificationMessage;

        public NotificationsViewHolder(View itemView)
        {
            super(itemView);
            notificationIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_notifiactions);
            notificationMessage = (TextView) itemView.findViewById(R.id.tv_list_item_notifications);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(communicator != null)
            {
                communicator.onNotificationClicked(notifications.get(getAdapterPosition()));
            }
        }

        public void render(Notification notification)
        {
            notificationMessage.setText(notification.getMessage());

            switch (notification.getType())
            {
                case Notification.CHAT:
                    notificationIcon.setImageDrawable(eventCreatedDrawable);
                    break;
                case Notification.NEW_FRIEND_ADDED:
                    break;
                case Notification.EVENT_UPDATED:
                    break;
                case Notification.EVENT_REMOVED:
                    break;
                case Notification.RSVP:
                    break;
                case Notification.EVENT_INVITATION:
                    break;
                case Notification.EVENT_CREATED:
                    break;
            }
        }
    }
}
