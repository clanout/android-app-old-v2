package reaper.android.app.ui.screens.create;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Suggestion;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.LocationSuggestionViewHolder>
{
    SuggestionClickListener suggestionClickListener;
    List<Suggestion> suggestions;

    public LocationSuggestionAdapter(List<Suggestion> suggestions, SuggestionClickListener suggestionClickListener)
    {
        this.suggestions = suggestions;
        this.suggestionClickListener = suggestionClickListener;
    }

    @Override
    public LocationSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_suggestion, parent, false);
        return new LocationSuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationSuggestionViewHolder holder, int position)
    {
        Suggestion suggestion = suggestions.get(position);
        holder.render(suggestion);
    }

    @Override
    public int getItemCount()
    {
        return suggestions.size();
    }

    public interface SuggestionClickListener
    {
        void onSuggestionClicked(Suggestion suggestion);
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
                            .onSuggestionClicked(suggestions.get(getAdapterPosition()));
                }
            });
        }

        public void render(Suggestion suggestion)
        {
            name.setText(suggestion.getName());
        }
    }
}
