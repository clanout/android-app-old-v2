package reaper.android.app.ui.screens.edit;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.DayPickerDialog;
import reaper.android.app.ui.dialog.DefaultDialog;
import reaper.android.app.ui.screens.edit.mvp.EditEventPresenter;
import reaper.android.app.ui.screens.edit.mvp.EditEventPresenterImpl;
import reaper.android.app.ui.screens.edit.mvp.EditEventView;
import reaper.android.app.ui.util.CategoryIconFactory;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;

public class EditEventFragment extends BaseFragment implements EditEventView,
        LocationSuggestionAdapter.SuggestionClickListener
{
    private static final String ARG_EVENT = "arg_event";
    private static final String ARG_EVENT_DETAILS = "arg_event_details";

    public static EditEventFragment newInstance(Event event, EventDetails eventDetails)
    {
        EditEventFragment fragment = new EditEventFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putSerializable(ARG_EVENT_DETAILS, eventDetails);
        fragment.setArguments(args);

        return fragment;
    }

    EditEventScreen screen;

    EditEventPresenter presenter;

    /* UI Elements */
    @Bind(R.id.svEdit)
    ScrollView svEdit;

    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.tvType)
    TextView tvType;

    @Bind(R.id.etDescription)
    EditText etDesc;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvDay)
    TextView tvDay;

    @Bind(R.id.etLocation)
    EditText etLocation;

    @Bind(R.id.rvLocationSuggestions)
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
    boolean isLocationUpdating;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        UserService userService = UserService.getInstance();
        LocationService_ locationService = LocationService_.getInstance();
        PlacesService placesService = PlacesService.getInstance();

        Event event = (Event) getArguments().getSerializable(ARG_EVENT);
        EventDetails eventDetails = (EventDetails) getArguments()
                .getSerializable(ARG_EVENT_DETAILS);

        presenter = new EditEventPresenterImpl(eventService, userService, locationService, placesService, event, eventDetails);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (EditEventScreen) getActivity();
        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initLocationBox();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        etLocation.setOnFocusChangeListener(null);
        etLocation.addTextChangedListener(null);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_edit, menu);

        finalize = menu.findItem(R.id.action_finalize);
        delete = menu.findItem(R.id.action_delete);

        finalize.setVisible(false);
        delete.setVisible(false);

        MenuItem edit = menu.findItem(R.id.action_edit);
        Drawable editDrawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(36)
                .build();
        edit.setIcon(editDrawable);
        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                SoftKeyboardHandler.hideKeyboard(getActivity(), getView());
                edit();
                return true;
            }
        });


        if (isDeleteOptionVisible)
        {
            Drawable deleteDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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
            Drawable lockedDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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
            Drawable unlockedDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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
                    displayUnfinalizationDialog();
                }
                else
                {
                    displayFinalizationDialog();
                }
                return true;
            }
        });

        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                displayDeleteDialog();
                return true;
            }
        });
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
        selectedDay = 0;
        String dateStr = dateTimeUtil.formatDate(event.getStartTime().toLocalDate());
        if (dayList.contains(dateStr))
        {
            selectedDay = dayList.indexOf(dateStr);
        }

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
        ivCategoryIcon.setImageDrawable(CategoryIconFactory
                .get(category, Dimensions.EVENT_ICON_SIZE));
        llCategoryIconContainer
                .setBackground(CategoryIconFactory.getIconBackground(category));

        // DateTime Pickers
        tvTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayTimePickerDialog();
            }
        });

        tvDay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayDayPickerDialog();
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
            Drawable deleteDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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
            Drawable lockedDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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
            Drawable unlockedDrawable = MaterialDrawableBuilder
                    .with(getActivity())
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

        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

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
    public void navigateToDetailsScreen(String eventId)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        screen.navigateToDetailsScreen(eventId);
    }

    @Override
    public void navigateToHomeScreen()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        screen.navigateToHomeScreen();
    }

    private void edit()
    {
        if (presenter != null)
        {
            presenter.setDescription(etDesc.getText().toString());
            presenter.edit();
        }
    }

    @Override
    public void onSuggestionClicked(LocationSuggestion locationSuggestion)
    {
        presenter.selectSuggestion(locationSuggestion);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvLocationSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLocationSuggestions
                .setAdapter(new LocationSuggestionAdapter(new ArrayList<LocationSuggestion>(), this));

        rvLocationSuggestions.setVisibility(View.GONE);
    }

    private void initLocationBox()
    {
        etLocation.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    svEdit.scrollTo(0, rvLocationSuggestions.getBottom());
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
                svEdit.scrollTo(0, rvLocationSuggestions.getBottom());

                if (presenter != null)
                {
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
            }
        });
    }

    private void displayTimePickerDialog()
    {
        TimePickerDialog dialog = TimePickerDialog
                .newInstance(
                        new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
                            {
                                startTime = new LocalTime(hourOfDay, minute);
                                if (presenter != null)
                                {
                                    LocalDate startDate = dateTimeUtil
                                            .getDate(dayList.get(selectedDay));
                                    presenter.updateTime(DateTimeUtil
                                            .getDateTime(startDate, startTime));
                                }
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

    private void displayDayPickerDialog()
    {
        DayPickerDialog.show(getActivity(), dateList, selectedDay, new DayPickerDialog.Listener()
        {
            @Override
            public void onDaySelected(int position)
            {
                selectedDay = position;
                tvDay.setText(dayList.get(selectedDay));

                if (presenter != null)
                {
                    LocalDate startDate = dateTimeUtil.getDate(dayList.get(selectedDay));
                    presenter.updateTime(DateTimeUtil.getDateTime(startDate, startTime));
                }

            }
        });
    }

    private void displayDeleteDialog()
    {
        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());
        DefaultDialog.show(getActivity(),
                R.string.event_delete_title,
                R.string.event_delete_message,
                R.string.event_delete_positive_button,
                R.string.event_delete_negative_button,
                true,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                        if (presenter != null)
                        {
                            presenter.delete();
                        }
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {

                    }
                });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_DISMISS_DIALOG);
        /* Analytics */
    }

    private void displayFinalizationDialog()
    {
        DefaultDialog.show(getActivity(),
                R.string.event_lock_title,
                R.string.event_lock_message,
                R.string.event_lock_positive_button,
                R.string.event_lock_negative_button,
                true,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                        if (presenter != null)
                        {
                            presenter.finalizeEvent();
                        }
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {

                    }
                });
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_FINALIZE_DIALOG);
    }

    private void displayUnfinalizationDialog()
    {
        DefaultDialog.show(getActivity(),
                R.string.event_unlock_title,
                R.string.event_unlock_message,
                R.string.event_unlock_positive_button,
                R.string.event_unlock_negative_button,
                true,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                        if (presenter != null)
                        {
                            presenter.unfinalizeEvent();
                        }
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {

                    }
                });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_UNFINALIZE_DIALOG);
        /* Analytics */
    }
}
