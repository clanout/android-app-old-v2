package reaper.android.app.ui.screens.create;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.service.GoogleService;
import reaper.android.common.cache.Cache;

public class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable
{
    private ArrayList<GooglePlaceAutocompleteApiResponse.Prediction> resultList;
    private GoogleService googleService;
    private Double userLatitude, userLongitude;
    private String eventType;

    public GooglePlacesAutocompleteAdapter(Context context, int resource, int textViewResourceId, Bus bus, String eventType)
    {
        super(context, resource, textViewResourceId);

        googleService = new GoogleService(bus);
        userLatitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LATITUDE);
        userLongitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LONGITUDE);
        this.eventType = eventType;
    }


    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index).getDescription();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = googleService.autocomplete(userLatitude, userLongitude, eventType, constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
