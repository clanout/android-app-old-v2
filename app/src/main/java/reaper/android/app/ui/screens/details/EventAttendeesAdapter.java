package reaper.android.app.ui.screens.details;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

public class EventAttendeesAdapter extends RecyclerView.Adapter<EventAttendeesAdapter.EventDetailsViewHolder>
{
    private Context context;
    List<EventDetails.Attendee> attendees;
    private AttendeeClickCommunicator attendeeClickCommunicator;
    private Drawable goingDrawable, maybeDrawable, invitedDrawable;
    private Drawable personDrawable;

    public EventAttendeesAdapter(List<EventDetails.Attendee> attendees, Context context)
    {
        this.attendees = attendees;
        this.context = context;

        generateDrawables();
    }

    private void generateDrawables()
    {
        goingDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(context.getResources().getColor(R.color.green))
                .setSizeDp(18)
                .build();

        maybeDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.HELP)
                .setColor(context.getResources().getColor(R.color.yellow))
                .setSizeDp(18)
                .build();

        invitedDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL)
                .setColor(context.getResources().getColor(R.color.primary))
                .setSizeDp(18)
                .build();

        personDrawable = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT)
                .setColor(Color.BLACK)
                .setSizeDp(24)
                .build();
    }

    @Override
    public EventDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event_attendee, parent, false);
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

    public void setAttendeeClickCommunicator(AttendeeClickCommunicator attendeeClickCommunicator)
    {
        this.attendeeClickCommunicator = attendeeClickCommunicator;
    }

    public class EventDetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ImageView pic, inviter, rsvp;
        private TextView name;

        public EventDetailsViewHolder(View itemView)
        {
            super(itemView);

            pic = (ImageView) itemView.findViewById(R.id.iv_event_attendee_pic);
            inviter = (ImageView) itemView.findViewById(R.id.iv_event_attendee_inviter);
            rsvp = (ImageView) itemView.findViewById(R.id.iv_event_attendee_rsvp);
            name = (TextView) itemView.findViewById(R.id.tv_event_attendee_name);

            itemView.setOnClickListener(this);
        }

        public void render(EventDetails.Attendee attendee)
        {
            Picasso.with(context)
                    .load(AppConstants.FACEBOOK_END_POINT + attendee.getId() + "/picture")
                    .placeholder(personDrawable)
                    .fit()
                    .centerInside()
                    .into(pic);

            name.setText(attendee.getName());

            if (attendee.isInviter())
            {
                inviter.setVisibility(View.VISIBLE);
                inviter.setImageDrawable(invitedDrawable);
            } else
            {
                inviter.setVisibility(View.INVISIBLE);
            }

            switch (attendee.getRsvp())
            {
                case YES:
                    rsvp.setImageDrawable(goingDrawable);
                    break;
                case MAYBE:
                    rsvp.setImageDrawable(maybeDrawable);
                    break;
            }
        }

        @Override
        public void onClick(View v)
        {
            if (attendeeClickCommunicator != null)
            {
                if (attendees.get(getAdapterPosition()).isInviter())
                {
                    attendeeClickCommunicator.onAttendeeClicked(attendees.get(getAdapterPosition()).getName());
                }
            }
        }
    }
}
