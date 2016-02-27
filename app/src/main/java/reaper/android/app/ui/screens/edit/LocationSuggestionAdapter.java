package reaper.android.app.ui.screens.edit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.LocationSuggestion;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.LocationSuggestionViewHolder>
{
    SuggestionClickListener suggestionClickListener;
    List<LocationSuggestion> locationSuggestions;

    public LocationSuggestionAdapter(List<LocationSuggestion> locationSuggestions, SuggestionClickListener suggestionClickListener)
    {
        this.locationSuggestions = locationSuggestions;
        this.suggestionClickListener = suggestionClickListener;
    }

    @Override
    public LocationSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_location_suggestion, parent, false);
        return new LocationSuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationSuggestionViewHolder holder, int position)
    {
        LocationSuggestion locationSuggestion = locationSuggestions.get(position);
        holder.render(locationSuggestion);
    }

    @Override
    public int getItemCount()
    {
        return locationSuggestions.size();
    }

    public interface SuggestionClickListener
    {
        void onSuggestionClicked(LocationSuggestion locationSuggestion);
    }

    public class LocationSuggestionViewHolder extends RecyclerView.ViewHolder
    {
        View container;
        TextView name;

        public LocationSuggestionViewHolder(View itemView)
        {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tv_locationSuggestion_name);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    suggestionClickListener
                            .onSuggestionClicked(locationSuggestions.get(getAdapterPosition()));
                }
            });
        }

        public void render(LocationSuggestion locationSuggestion)
        {
            name.setText(locationSuggestion.getName());
        }
    }
}
