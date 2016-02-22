package reaper.android.app.ui.screens.details;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;

public class EventDetailsContainerFragment extends BaseFragment
{
    private GenericCache genericCache;
    private Bus bus;

    // Data
    private ArrayList<Event> events;
    private int activePosition;
    private boolean shouldPopupStatusDialog;

    // UI Elements
    private Toolbar toolbar;

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    // Service
    private UserService userService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EVENT_DETAILS_CONTAINER_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details_container, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_event_details_container);
        viewPager = (ViewPager) view.findViewById(R.id.vp_event_details_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            events = (ArrayList<Event>) savedInstanceState
                    .get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = savedInstanceState
                    .getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
        }
        else
        {
            Bundle bundle = getArguments();
            events = (ArrayList<Event>) bundle
                    .get(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS);
            activePosition = bundle
                    .getInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION);
            shouldPopupStatusDialog = bundle.getBoolean(BundleKeys.POPUP_STATUS_DIALOG);
        }

        if (events == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        genericCache = CacheManager.getGenericCache();

        pagerAdapter = new EventDetailsPagerAdapter(getChildFragmentManager(), events, activePosition, shouldPopupStatusDialog);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(activePosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                activePosition = i;
                setActionBarTitle();
                ActivityCompat.invalidateOptionsMenu(getActivity());
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });

        initXmppConnection();
    }

    private void initXmppConnection()
    {
        try
        {
            if (ChatHelper.getXmppConnection() == null)
            {
                ChatHelper.init(userService.getActiveUserId());
            }
        }
        catch (IOException | XMPPException | SmackException ignored)
        {
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        setActionBarTitle();
        genericCache.put(GenericCacheKeys.ACTIVE_FRAGMENT, BackstackTags.EVENT_DETAILS_CONTAINER);

        bus.register(this);

        handleInAppRating();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        bus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, events);
        outState.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void backPressed(BackPressedTrigger trigger)
    {
        if (trigger.getActiveFragment().equals(BackstackTags.EVENT_DETAILS_CONTAINER))
        {
            HomeFragment homeFragment = new HomeFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, events);
            bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);

            homeFragment.setArguments(bundle);

            FragmentUtils.changeFragment(getFragmentManager(), homeFragment);
        }
    }


    private void setActionBarTitle()
    {
        switch (EventCategory.valueOf(events.get(activePosition).getCategory()))
        {
            case CAFE:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Cafe");
                break;
            case MOVIES:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Movie");
                break;
            case SHOPPING:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Shopping");
                break;
            case SPORTS:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Sports");
                break;
            case INDOORS:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Indoors");
                break;
            case EAT_OUT:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Eat Out");
                break;
            case DRINKS:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Drinks");
                break;
            case OUTDOORS:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Outdoors");
                break;
            case GENERAL:
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("General");
                break;
        }
    }

    private void handleInAppRating()
    {

        if (genericCache.get(GenericCacheKeys.HAS_GIVEN_FEEDBACK) == null)
        {
            int timesAppOpened;

            try
            {
                timesAppOpened = Integer
                        .parseInt(genericCache.get(GenericCacheKeys.TIMES_APPLICATION_OPENED));
            }
            catch (Exception e)
            {
                timesAppOpened = 0;
            }

            if (timesAppOpened > 10)
            {

                double random = Math.random();

                if (random < 0.1)
                {
                    displayShareFeedbackDialog();
                }
            }
        }
    }

    private void displayShareFeedbackDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_share_feedback, null);
        builder.setView(dialogView);

        final EditText commentMessage = (EditText) dialogView
                .findViewById(R.id.et_alert_dialog_share_feedback_comment);
        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.rg_share_feedback);
        final TextInputLayout tilFeedbackMessage = (TextInputLayout) dialogView
                .findViewById(R.id.tilFeedbackMessage);

        commentMessage.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                tilFeedbackMessage.setError("");
                tilFeedbackMessage.setErrorEnabled(false);
            }
        });

        builder.setPositiveButton(R.string.feedback_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
            }
        });

        builder.setNegativeButton(R.string.feedback_remind_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                   .setOnClickListener(new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View view)
                       {
                           int type = 0;
                           switch (radioGroup.getCheckedRadioButtonId())
                           {
                               case R.id.rb_share_feedback_bug:
                                   type = 0;
                                   break;
                               case R.id.rb_share_feedback_new_feature:
                                   type = 1;
                                   break;
                               case R.id.rb_share_feedback_other:
                                   type = 2;
                                   break;
                           }

                           String comment = commentMessage.getText().toString();
                           Boolean wantToCloseDialog = false;

                           if (TextUtils.isEmpty(comment))
                           {
                               tilFeedbackMessage.setError(getString(R.string.feedback_empty_comment));
                               tilFeedbackMessage.setErrorEnabled(true);
                               wantToCloseDialog = false;
                           }
                           else
                           {

                               AnalyticsHelper
                                       .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.FEEDBACK_SHARED, userService
                                               .getActiveUserId());

                               userService.shareFeedback(type, comment);

                               genericCache.put(GenericCacheKeys.HAS_GIVEN_FEEDBACK, true);
                               wantToCloseDialog = true;
                           }

                           if (wantToCloseDialog)
                           {
                               alertDialog.dismiss();
                           }
                       }
                   });
    }
}
