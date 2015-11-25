package reaper.android.app.ui.activity.dummy;

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
    private Context context;
    List<EventDetails.Attendee> attendees;
    private Drawable personDrawable;

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

    public class EventDetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ImageView inviter;
        private ImageView pic;
        private TextView name;
        private TextView status;

        public EventDetailsViewHolder(View itemView)
        {
            super(itemView);

            pic = (ImageView) itemView.findViewById(R.id.ivPic);
            inviter = (ImageView) itemView.findViewById(R.id.mivInvite);
            name = (TextView) itemView.findViewById(R.id.tvName);
            status = (TextView) itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(this);
        }

        public void render(EventDetails.Attendee attendee)
        {
            Picasso.with(context)
                   .load(AppConstants.FACEBOOK_END_POINT + attendee.getId() + "/picture?width=500")
                   .placeholder(personDrawable)
                   .transform(new CircleTransform())
                   .into(pic);

            name.setText(attendee.getName());

            if (attendee.getStatus() == null || attendee.getStatus().isEmpty())
            {
                status.setText(R.string.default_event_status);
            }
            else
            {
                status.setText(attendee.getStatus());
            }

            if (attendee.isInviter())
            {
                inviter.setVisibility(View.VISIBLE);
            }
            else
            {
                inviter.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v)
        {
        }
    }
}
