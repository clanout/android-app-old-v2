package reaper.android.app.ui.screens.create;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Suggestion;

/**
 * Created by harsh on 13-05-2015.
 */
public class EventSuggestionsAdapter extends RecyclerView.Adapter<EventSuggestionsAdapter.EventSuggestionsViewHolder>
{

    private LayoutInflater inflater;
    private List<Suggestion> data = Collections.emptyList();
    private Context context;
    private EventSuggestionsClickListener eventSuggestionsClickListener;

    public EventSuggestionsAdapter(Context context, List<Suggestion> data)
    {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public EventSuggestionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.list_item_event_suggestions, parent, false);
        EventSuggestionsViewHolder holder = new EventSuggestionsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(EventSuggestionsViewHolder holder, int position)
    {
        Suggestion current = data.get(position);

        holder.placeName.setText(current.getName());

        Picasso.with(context)
                .load(current.getIconUrl())
                .placeholder(R.drawable.ic_event_black_36dp)
                .error(R.drawable.ic_event_black_36dp)
                .fit()
                .centerCrop()
                .into(holder.placeIcon);

        holder.placeAddress.setText(current.getDescription());

        try
        {
            if (Double.parseDouble(current.getRating()) > 4.0)
            {
                holder.ratingIcon.setImageResource(R.drawable.ic_action_important);
                holder.placeRating.setText(current.getRating() + "/5.0");
            }
            else
            {
                holder.ratingIcon.setImageResource(R.drawable.ic_action_not_important);
                holder.placeRating.setText(current.getRating() + "/5.0");
            }
        }
        catch (Exception e)
        {
            holder.ratingIcon.setImageResource(R.drawable.ic_action_not_important);
            holder.placeRating.setText("NA");
        }
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    public void setEventSuggestionsClickListener(EventSuggestionsClickListener eventSuggestionsClickListener)
    {
        this.eventSuggestionsClickListener = eventSuggestionsClickListener;
    }

    class EventSuggestionsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        ImageView ratingIcon;
        ImageView placeIcon;
        TextView placeName, placeAddress, placeRating;

        public EventSuggestionsViewHolder(View itemView)
        {
            super(itemView);

            itemView.setOnClickListener(this);
            placeIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_suggestions_place_pic);
            placeName = (TextView) itemView.findViewById(R.id.tv_list_item_event_suggestions_place_name);
            ratingIcon = (ImageView) itemView.findViewById(R.id.iv_list_item_event_suggestions_place_rating);
            placeAddress = (TextView) itemView.findViewById(R.id.tv_list_item_event_suggestions_place_vicinity);
            placeRating = (TextView) itemView.findViewById(R.id.tv_list_item_event_suggestions_place_rating);
        }

        @Override
        public void onClick(View v)
        {
            if (eventSuggestionsClickListener != null)
            {
                eventSuggestionsClickListener.onEventSuggestionsClicked(v, getPosition());
            }
        }
    }

    public interface EventSuggestionsClickListener
    {
        public void onEventSuggestionsClicked(View view, int position);
    }
}
