package reaper.android.app.ui.screens.edit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import hotchemi.stringpicker.StringPicker;
import reaper.android.R;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.create.LocationSuggestionAdapter;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class EditEventFragment extends BaseFragment implements EditEventView, LocationSuggestionAdapter.SuggestionClickListener
{
    private static final String ARG_EVENT = "arg_event";
    private static final String ARG_EVENT_DETAILS = "arg_event_details";

    EditEventPresenter presenter;

    Bus bus;
    UserService userService;

    /* UI Elements */
    ScrollView parent;
    Toolbar toolbar;
    View llCategoryIconContainer;
    ImageView ivCategoryIcon;
    TextView tvTitle;
    TextView tvType;
    EditText etDesc;
    TextView tvTime;
    TextView tvDay;
    EditText etLocation;
    RecyclerView rvLocationSuggestions;

    ProgressDialog progressDialog;

    boolean isFinalizeOptionVisible;
    boolean isUnfinalizeOptionVisible;
    MenuItem finalize;

    boolean isDeleteOptionVisible;
    MenuItem delete;

    /* Data */
    DateTimeUtil dateTimeUtil;

    List<String> dayList;
    List<String> dateList;
    int selectedDay;
    LocalTime startTime;
    boolean isFinalized;

    Event event;

    boolean isLocationUpdating;

    public static EditEventFragment newInstance(Event event, EventDetails eventDetails)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putSerializable(ARG_EVENT_DETAILS, eventDetails);

        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EDIT_EVENT_FRAGMENT);

        bus = Communicator.getInstance().getBus();
        userService = UserService.getInstance();

        event = (Event) getArguments().getSerializable(ARG_EVENT);
        EventDetails eventDetails = (EventDetails) getArguments()
                .getSerializable(ARG_EVENT_DETAILS);
        if (event == null || eventDetails == null)
        {
            throw new IllegalStateException("event or details cannot be null");
        }

        presenter = new EditEventPresenterImpl(bus, event, eventDetails);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit_, container, false);

        parent = (ScrollView) view.findViewById(R.id.sv_editEvent);
        toolbar = (Toolbar) view.findViewById(R.id.tb_editEvent);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvType = (TextView) view.findViewById(R.id.tvType);
        llCategoryIconContainer = view.findViewById(R.id.llCategoryIconContainer);
        ivCategoryIcon = (ImageView) view.findViewById(R.id.ivCategoryIcon);
        etDesc = (EditText) view.findViewById(R.id.etDesc);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvDay = (TextView) view.findViewById(R.id.tvDay);
        etLocation = (EditText) view.findViewById(R.id.etLocation);
        rvLocationSuggestions = (RecyclerView) view.findViewById(R.id.rvLocationSuggestions);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        setActionBarTitle(event);

        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit);

        bus.register(this);

        CacheManager.getGenericCache().put(GenericCacheKeys.ACTIVE_FRAGMENT, BackstackTags.EDIT);

        etLocation.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    parent.scrollTo(0, rvLocationSuggestions.getBottom());
                }
            }
        });

        etLocation.addTextChangedListener(new TextWatcher()
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
                parent.scrollTo(0, rvLocationSuggestions.getBottom());

                if (!isLocationUpdating)
                {
                    if (s.length() == 0)
                    {
                        presenter.fetchSuggestions();
                    }
                    else if (s.length() >= 3)
                    {
                        presenter.autocomplete(s.toString());
                    }

                    presenter.setLocationName(s.toString());
                }
            }
        });

        presenter.attachView(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        etLocation.setOnFocusChangeListener(null);
        etLocation.addTextChangedListener(null);

        presenter.detachView();

        bus.unregister(this);
    }

    private void initRecyclerView()
    {
        rvLocationSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLocationSuggestions
                .setAdapter(new LocationSuggestionAdapter(new ArrayList<LocationSuggestion>(), this));

        rvLocationSuggestions.setVisibility(View.GONE);
    }

    private void displayDayPicker()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_day_picker, null);
        builder.setView(dialogView);

        final StringPicker stringPicker = (StringPicker) dialogView
                .findViewById(R.id.dayPicker);
        stringPicker.setValues(dateList);
        stringPicker.setCurrent(selectedDay);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                selectedDay = stringPicker.getCurrent();
                tvDay.setText(dayList.get(selectedDay));
                Timber.d("Selected Day = " + selectedDay);
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        /* Set Width */
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int width = (int) (displayRectangle.width() * 0.80f);
        alertDialog.getWindow().setLayout(width, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
    }

    /* View Methods */
    @Override
    public void init(Event event, String description)
    {
        isFinalized = event.isFinalized();

        dateTimeUtil = new DateTimeUtil();

        // Title
        tvTitle.setText(event.getTitle());

        // Type
        if (event.getType() == Event.Type.INVITE_ONLY)
        {
            tvType.setText(R.string.event_type_secret);
        }
        else
        {
            tvType.setText(R.string.event_type_open);
        }

        //Description
        if (description != null)
        {
            etDesc.setText(description);
        }


        // Start Time
        startTime = event.getStartTime().toLocalTime();
        tvTime.setText(dateTimeUtil.formatTime(startTime));

        // Start Day
        tvDay.setText(dateTimeUtil.formatDate(event.getStartTime().toLocalDate()));

        dayList = dateTimeUtil.getDayList();
        dateList = dateTimeUtil.getDayAndDateList();
        selectedDay = -1;

        // Location
        Location location = event.getLocation();
        if (location != null)
        {
            String locationName = location.getName();
            if (locationName != null)
            {
                etLocation.setText(locationName);
            }
        }


        // Category
        EventCategory category = EventCategory.valueOf(event.getCategory());
        ivCategoryIcon.setImageDrawable(DrawableFactory
                .get(category, Dimensions.EVENT_ICON_SIZE));
        llCategoryIconContainer
                .setBackground(DrawableFactory.getIconBackground(category));

        tvTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimePickerDialog dialog = TimePickerDialog
                        .newInstance(
                                new TimePickerDialog.OnTimeSetListener()
                                {
                                    @Override
                                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
                                    {
                                        startTime = new LocalTime(hourOfDay, minute);
                                        tvTime.setText(dateTimeUtil.formatTime(startTime));
                                    }
                                },
                                startTime.getHourOfDay(),
                                startTime.getMinuteOfHour(),
                                false);

                dialog.dismissOnPause(true);
                dialog.vibrate(false);
                dialog.show(getFragmentManager(), "TimePicker");
            }
        });

        tvDay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayDayPicker();
            }
        });
    }

    @Override
    public void showLoading()
    {
        progressDialog = ProgressDialog.show(getActivity(), "Updating Clan", "Please Wait..");
    }

    @Override
    public void displayDeleteOption()
    {
        if (delete != null && !isDeleteOptionVisible)
        {
            Drawable deleteDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                                                             .setColor(ContextCompat
                                                                     .getColor(getActivity(), R.color.white))
                                                             .setSizeDp(36)
                                                             .build();
            delete.setIcon(deleteDrawable);
            delete.setVisible(true);
        }

        isDeleteOptionVisible = true;
    }

    @Override
    public void displayFinalizationOption()
    {
        if (finalize != null && !isFinalizeOptionVisible)
        {
            Drawable lockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                                                             .setColor(ContextCompat
                                                                     .getColor(getActivity(), R.color.white))
                                                             .setSizeDp(36)
                                                             .build();

            finalize.setIcon(lockedDrawable);
            finalize.setVisible(true);
        }

        isFinalizeOptionVisible = true;
    }

    @Override
    public void displayUnfinalizationOption()
    {
        if (finalize != null && !isUnfinalizeOptionVisible)
        {
            Drawable unlockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                               .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                                                               .setColor(ContextCompat
                                                                       .getColor(getActivity(), R.color.white))
                                                               .setSizeDp(36)
                                                               .build();

            finalize.setIcon(unlockedDrawable);
            finalize.setVisible(true);
        }

        isUnfinalizeOptionVisible = true;
    }

    @Override
    public void displaySuggestions(List<LocationSuggestion> locationSuggestions)
    {
        rvLocationSuggestions.setAdapter(new LocationSuggestionAdapter(locationSuggestions, this));

        if (locationSuggestions.isEmpty())
        {
            rvLocationSuggestions.setVisibility(View.GONE);
        }
        else
        {
            rvLocationSuggestions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLocation(String locationName)
    {
        isLocationUpdating = true;
        etLocation.setText(locationName);
        etLocation.setSelection(etLocation.length());
        isLocationUpdating = false;

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tvTitle.getWindowToken(), 0);

        presenter.fetchSuggestions();
    }

    @Override
    public void displayEventLockedError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_edit_finalized);
    }

    @Override
    public void displayError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_default);
    }

    @Override
    public void navigateToDetailsScreen(List<Event> events, int activePosition)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getFragmentManager(), eventDetailsContainerFragment);
    }

    @Override
    public void navigateToHomeScreen()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        FragmentUtils.changeFragment(getFragmentManager(), new HomeFragment());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_edit, menu);

        Drawable drawable = MaterialDrawableBuilder
                .with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(Color.WHITE)
                .build();

        menu.findItem(R.id.action_edit)
            .setIcon(drawable);

        menu.findItem(R.id.action_edit)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {

                    SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                    edit();
                    return true;
                }
            });

        finalize = menu.findItem(R.id.action_finalize);
        delete = menu.findItem(R.id.action_delete);

        finalize.setVisible(false);
        delete.setVisible(false);

        if (isDeleteOptionVisible)
        {
            Drawable deleteDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                                                             .setColor(ContextCompat
                                                                     .getColor(getActivity(), R.color.white))
                                                             .setSizeDp(36)
                                                             .build();
            delete.setIcon(deleteDrawable);
            delete.setVisible(true);
        }

        if (isFinalizeOptionVisible)
        {
            Drawable lockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                             .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                                                             .setColor(ContextCompat
                                                                     .getColor(getActivity(), R.color.white))
                                                             .setSizeDp(36)
                                                             .build();

            finalize.setIcon(lockedDrawable);
            finalize.setVisible(true);
        }
        else if (isUnfinalizeOptionVisible)
        {
            Drawable unlockedDrawable = MaterialDrawableBuilder.with(getActivity())
                                                               .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                                                               .setColor(ContextCompat
                                                                       .getColor(getActivity(), R.color.white))
                                                               .setSizeDp(36)
                                                               .build();

            finalize.setIcon(unlockedDrawable);
            finalize.setVisible(true);
        }

        finalize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                if (isFinalized)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);

                    LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                    View dialogView = layoutInflater.inflate(R.layout.dialog_default, null);
                    builder.setView(dialogView);

                    TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
                    TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

                    tvTitle.setText(R.string.event_unlock_title);
                    tvMessage.setText(R.string.event_unlock_message);

                    builder.setPositiveButton(R.string.event_unlock_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            presenter.unfinalizeEvent();

                            AnalyticsHelper
                                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_UNFINALIZED, userService
                                            .getSessionUserId());
                        }
                    });

                    builder.setNegativeButton(R.string.event_unlock_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);

                    LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                    View dialogView = layoutInflater.inflate(R.layout.dialog_default, null);
                    builder.setView(dialogView);

                    TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
                    TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

                    tvTitle.setText(R.string.event_lock_title);
                    tvMessage.setText(R.string.event_lock_message);

                    builder.setPositiveButton(R.string.event_lock_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            presenter.finalizeEvent();

                            AnalyticsHelper
                                    .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_FINALIZED, userService
                                            .getSessionUserId());
                        }
                    });

                    builder.setNegativeButton(R.string.event_lock_negative_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                return true;
            }
        });

        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);

                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_default, null);
                builder.setView(dialogView);

                TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
                TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

                tvTitle.setText(R.string.event_delete_title);
                tvMessage.setText(R.string.event_delete_message);

                builder.setPositiveButton(R.string.event_delete_positive_button, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        presenter.delete();

                        AnalyticsHelper
                                .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_DELETED, userService
                                        .getSessionUserId());
                    }
                });

                builder.setNegativeButton(R.string.event_delete_negative_button, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });
    }

    private void edit()
    {
        presenter.setDescription(etDesc.getText().toString());
        presenter.edit();

        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.EVENT_EDITED, userService
                        .getSessionUserId());
    }

    @Override
    public void onSuggestionClicked(LocationSuggestion locationSuggestion)
    {
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.LIST_ITEM_CLICK, GoogleAnalyticsConstants.SUGGESTION_CLICKED_EDIT, userService
                        .getSessionUserId());

        presenter.selectSuggestion(locationSuggestion);
    }

    private void setActionBarTitle(Event event)
    {
        switch (event.getCategory())
        {
            case "CAFE":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Cafe");
                break;
            case "MOVIES":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Movie");
                break;
            case "SHOPPING":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Shopping");
                break;
            case "SPORTS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Sports");
                break;
            case "INDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Indoors");
                break;
            case "EAT_OUT":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Eat Out");
                break;
            case "DRINKS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Drinks");
                break;
            case "OUTDOORS":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("Outdoors");
                break;
            case "GENERAL":
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("General");
                break;
        }
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
    public void onBackPressedTrigger(BackPressedTrigger trigger)
    {
        if (trigger.getActiveFragment() == BackstackTags.EDIT)
        {
            presenter.initiateEventDetailsNavigation();
        }
    }
}
