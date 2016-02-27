package reaper.android.app.ui.screens.details;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service._new.FacebookService_;
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
        personDrawable = MaterialDrawableBuilder
                .with(context)
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
                                  .inflate(R.layout.item_event_attendee, parent, false);
        return new EventDetailsViewHolder(view);
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
        @Bind(R.id.ivPic)
        ImageView ivPic;

        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.tvStatus)
        TextView tvStatus;

        @Bind(R.id.mivInvite)
        View mivInvite;

        public EventDetailsViewHolder(final View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(EventDetails.Attendee attendee)
        {
            Picasso.with(context)
                   .load(FacebookService_.getFriendPicUrl(attendee.getId()))
                   .placeholder(personDrawable)
                   .transform(new CircleTransform())
                   .into(ivPic);

            tvName.setText(attendee.getName());

            if (attendee.getStatus() == null || attendee.getStatus().isEmpty())
            {
                tvStatus.setText(R.string.status_default);
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
