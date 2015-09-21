package reaper.android.app.ui.screens.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import reaper.android.R;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.EventSuggestion;
import reaper.android.app.trigger.common.ViewPagerClickedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.common.communicator.Communicator;


/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventListItemFragment extends BaseFragment implements View.OnClickListener {

    private TextView textView;
    private LinearLayout linearLayout;
    private Button createEvent;
    private Bus bus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_list_item, container, false);
        textView = (TextView) view.findViewById(R.id.tv_create_Event_list_item);
        linearLayout = (LinearLayout) view.findViewById(R.id.ll_fragment_create_event_list_item);
        createEvent = (Button) view.findViewById(R.id.b_fragment_create_event_list_item_create);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        EventSuggestion eventSuggestion = (EventSuggestion) bundle.getSerializable(BundleKeys.EVENT_SUGGESTION);

        if (eventSuggestion == null) {
            throw new IllegalStateException("Event Suggestion is null");
        }

        textView.setText(eventSuggestion.getTitle());

        linearLayout.setOnClickListener(this);
        createEvent.setOnClickListener(this);

        bus = Communicator.getInstance().getBus();
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.ll_fragment_create_event_list_item) {
            bus.post(new ViewPagerClickedTrigger());
        } else if (v.getId() == R.id.b_fragment_create_event_list_item_create) {
            bus.post(new ViewPagerClickedTrigger());
            Toast.makeText(getActivity(), textView.getText().toString(), Toast.LENGTH_LONG).show();
        }
    }
}
