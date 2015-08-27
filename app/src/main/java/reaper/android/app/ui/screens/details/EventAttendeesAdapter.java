package reaper.android.app.ui.screens.details;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.EventDetails;

public class EventAttendeesAdapter extends RecyclerView.Adapter<EventAttendeesAdapter.EventDetailsViewHolder>
{
    List<EventDetails.Attendee> attendees;
    private AttendeeClickCommunicator attendeeClickCommunicator;

    public EventAttendeesAdapter(List<EventDetails.Attendee> attendees)
    {
        this.attendees = attendees;
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
            pic.setImageResource(R.drawable.ic_person_black_36dp);
            name.setText(attendee.getName());

            if (attendee.isInviter())
            {
                inviter.setVisibility(View.VISIBLE);
                inviter.setImageResource(R.drawable.ic_person_add_black_24dp);
            }
            else
            {
                inviter.setVisibility(View.INVISIBLE);
            }

            switch (attendee.getRsvp())
            {
                case YES:
                    rsvp.setImageResource(R.drawable.ic_check_circle_black_24dp);
                    break;
                case MAYBE:
                    rsvp.setImageResource(R.drawable.ic_help_black_24dp);
                    break;
            }
        }

        @Override
        public void onClick(View v)
        {
            if(attendeeClickCommunicator != null)
            {
                attendeeClickCommunicator.onAttendeeClicked(attendees.get(getAdapterPosition()).getName());
            }
        }
    }
}
