package reaper.android.app.ui.screens.pending_invites;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.DrawableFactory;

public class PendingInviteAdapter extends RecyclerView.Adapter<PendingInviteAdapter.PendingInviteViewHolder>
{
    private Context context;
    private List<Event> pendingInvites;
    private PendingInviteClickListener clickListener;

    public PendingInviteAdapter(Context context, List<Event> pendingInvites, PendingInviteClickListener clickListener)
    {
        this.context = context;
        this.pendingInvites = pendingInvites;
        this.clickListener = clickListener;
    }

    @Override
    public PendingInviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context)
                                  .inflate(R.layout.item_pending_invite, parent, false);
        return new PendingInviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PendingInviteViewHolder holder, int position)
    {
        holder.render(pendingInvites.get(position));
    }

    @Override
    public int getItemCount()
    {
        return pendingInvites.size();
    }

    public interface PendingInviteClickListener
    {
        void onPendingInviteClicked(Event event);
    }

    public class PendingInviteViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.llCategoryIconContainer)
        View llCategoryIconContainer;

        @Bind(R.id.ivCategoryIcon)
        ImageView ivCategoryIcon;

        @Bind(R.id.tvTitle)
        TextView tvTitle;

        @Bind(R.id.tvInvites)
        TextView tvInvites;

        public PendingInviteViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    clickListener.onPendingInviteClicked(pendingInvites.get(getAdapterPosition()));
                }
            });
        }

        public void render(Event event)
        {
            // Title
            tvTitle.setText(event.getTitle());

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(DrawableFactory
                    .get(category, Dimensions.EVENT_FEED_ICON_SIZE));
            llCategoryIconContainer.setBackground(DrawableFactory.getIconBackground(category));

            // Inviter count
            int inviteCount = event.getInviterCount();
            if (inviteCount == 1)
            {
                tvInvites.setText("You were invited by 1 person for this plan");
            }
            else
            {
                tvInvites.setText("You were invited by " + inviteCount + " people for this event");
            }
        }
    }
}
