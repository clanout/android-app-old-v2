package reaper.android.app.ui.screens.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import reaper.android.R;
import reaper.android.app.ui.screens.core.BaseFragment;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventListItemFragment extends BaseFragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_list_item, container, false);
        textView = (TextView) view.findViewById(R.id.tv_create_Event_list_item);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        String category = (String) bundle.get("category");
        
        if(category == null)
        {
            throw new IllegalStateException("Category is null");
        }

        textView.setText(category);
    }
}
