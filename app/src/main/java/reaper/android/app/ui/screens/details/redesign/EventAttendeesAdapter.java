package reaper.android.app.ui.screens.details.redesign;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui.util.CircleTransform;

public class EventAttendeesAdapter extends RecyclerView.Adapter<EventAttendeesAdapter.EventDetailsViewHolder>
{
    Context context;
    List<EventDetails.Attendee> attendees;
    Drawable personDrawable;

    public EventAttendeesAdapter(List<EventDetails.Attendee> attendees, Context context)
    {
        this.attendees = attendees;
        this.context = context;

        generateDrawables();
    }

    private void generateDrawables()
    {
        personDrawable = MaterialDrawableBuilder.with(context)
                                                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                                                .setColor(ContextCompat
                                                        .getColor(context, R.color.light_grey))
                                                .setSizeDp(24)
                                                .build();
    }

    @Override
    public EventDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_event_attendee_, parent, false);
        EventDetailsViewHolder eventDetailsViewHolder = new EventDetailsViewHolder(view);
        return eventDetailsViewHolder;
    }

    @Override
    public void onBindViewHolder(EventDetailsViewHolder holder, int position)
    {
        EventDetails.Attendee attendee = attendees.get(position);
        holder.render(attendee);
    }

    @Override
    public int getItemCount()
    {
        return attendees.size();
    }

    public class EventDetailsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivPic;
        TextView tvName;
        TextView tvStatus;
        View mivInvite;

        public EventDetailsViewHolder(View itemView)
        {
            super(itemView);

            ivPic = (ImageView) itemView.findViewById(R.id.ivPic);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            mivInvite = itemView.findViewById(R.id.mivInvite);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EventDetails.Attendee attendee = attendees.get(getAdapterPosition());
                    if (attendee.isInviter())
                    {}
                }
            });
        }

        public void render(EventDetails.Attendee attendee)
        {
            Picasso.with(context)
                   .load(AppConstants.FACEBOOK_END_POINT + attendee.getId() + "/picture?width=500")
                   .placeholder(personDrawable)
                   .transform(new CircleTransform())
                   .into(ivPic);

            tvName.setText(attendee.getName());

            if (attendee.getStatus() == null || attendee.getStatus().isEmpty())
            {
                tvStatus.setText(R.string.default_event_status);
            }
            else
            {
                tvStatus.setText(attendee.getStatus());
            }

            if (attendee.isInviter())
            {
                mivInvite.setVisibility(View.VISIBLE);
            }
            else
            {
                mivInvite.setVisibility(View.GONE);
            }
        }
    }
}
