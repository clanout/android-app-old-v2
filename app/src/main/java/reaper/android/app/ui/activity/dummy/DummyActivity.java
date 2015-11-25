package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui.util.CircleTransform;
import timber.log.Timber;

public class DummyActivity extends AppCompatActivity
{
    Switch sRsvp;
    TextView tvRsvp;

    ImageView ivPic;

    RecyclerView rvAttendees;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Timber.v(">>>> HERE");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event_details_);

        sRsvp = (Switch) findViewById(R.id.sRsvp);
        tvRsvp = (TextView) findViewById(R.id.tvRsvp);
        rvAttendees = (RecyclerView) findViewById(R.id.rvAttendees);
        ivPic = (ImageView) findViewById(R.id.ivPic);

        tvRsvp.setGravity(View.GONE);

        Picasso.with(this)
               .load(AppConstants.FACEBOOK_END_POINT + "10207377866064846/picture?width=500")
               .transform(new CircleTransform())
               .into(ivPic);

        EventDetails.Attendee attendee = new EventDetails.Attendee();
        attendee.setName("Harsh Pokharna");
        attendee.setFriend(true);
        attendee.setId("977526725631911");
        attendee.setRsvp(Event.RSVP.YES);
        attendee.setStatus("Blue hai pani pani!");
        attendee.setInviter(true);

        EventDetails.Attendee attendee1 = new EventDetails.Attendee();
        attendee1.setName("Gaurav Kunwar");
        attendee1.setFriend(true);
        attendee1.setId("976303355745864");
        attendee1.setRsvp(Event.RSVP.YES);
        attendee1.setStatus("Babe I'm gonna leave you");
        attendee1.setInviter(false);

        List<EventDetails.Attendee> attendees = Arrays.asList(attendee, attendee1);

        rvAttendees.setLayoutManager(new LinearLayoutManager(this));
        rvAttendees.setAdapter(new EventAttendeesAdapter(attendees, this));

        sRsvp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    tvRsvp.setVisibility(View.VISIBLE);
                }
                else
                {
                    tvRsvp.setVisibility(View.GONE);
                }
            }
        });
    }
}
