package reaper.android.app.ui.screens.create;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.DayPickerDialog;
import reaper.android.app.ui.dialog.EventCategorySelectionDialog;
import reaper.android.app.ui.dialog.EventTypeInfoDialog;
import reaper.android.app.ui.screens.create.mvp.CreateEventPresenter;
import reaper.android.app.ui.screens.create.mvp.CreateEventPresenterImpl;
import reaper.android.app.ui.screens.create.mvp.CreateEventView;
import reaper.android.app.ui.util.CategoryIconFactory;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;

public class CreateDetailsFragment extends BaseFragment implements
        CreateEventView, LocationSuggestionAdapter.SuggestionClickListener
{
    private static final String ARG_CATEGORY = "arg_category";

    public static CreateDetailsFragment newInstance(EventCategory category)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);

        CreateDetailsFragment fragment = new CreateDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    CreateScreen screen;

    CreateEventPresenter presenter;

    /* UI Elements */
    @Bind(R.id.svCreate)
    ScrollView svCreate;

    @Bind(R.id.etTitle)
    EditText etTitle;

    @Bind(R.id.tvTitleLimit)
    TextView tvTitleLimit;

    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.etDescription)
    EditText etDescription;

    @Bind(R.id.cbType)
    CheckBox cbType;

    @Bind(R.id.mivInfo)
    View mivInfo;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvDay)
    TextView tvDay;

    @Bind(R.id.etLocation)
    EditText etLocation;

    @Bind(R.id.rvLocationSuggestions)
    RecyclerView rvLocationSuggestions;

    @Bind(R.id.llMoreDetailsContainer)
    View llMoreDetailsContainer;

    @Bind(R.id.llMoreDetails)
    View llMoreDetails;

    @Bind(R.id.focusThief)
    View focusThief;

    ProgressDialog createProgressDialog;
//    CreateEventLoadingDialog createProgressDialog;

    /* Data */
    DateTimeUtil dateTimeUtil;
    List<String> dayList;
    List<String> dateList;
    int selectedDay;
    LocalTime startTime;

    EventCategory selectedCategory;

    boolean isLocationUpdating;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        LocationService_ locationService = LocationService_.getInstance();
        PlacesService placesService = PlacesService.getInstance();

        presenter = new CreateEventPresenterImpl(eventService, locationService, placesService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (CreateScreen) getActivity();

        initView();
        initLocationBox();
        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        presenter.attachView(this);
        focusThief.requestFocus();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        etLocation.setOnFocusChangeListener(null);
        etLocation.addTextChangedListener(null);
    }

    /* Listeners */
    @Override
    public void onSuggestionClicked(LocationSuggestion locationSuggestion)
    {
        if (presenter != null)
        {
            presenter.selectSuggestion(locationSuggestion);
        }
    }

    @OnClick(R.id.llMoreDetails)
    public void onMoreDetailsToggled()
    {
        llMoreDetails.setVisibility(View.GONE);
        if (llMoreDetailsContainer.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llMoreDetailsContainer, 200);
        }
    }

    @OnClick(R.id.fabCreate)
    public void onCreateClicked()
    {
        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());
        createEvent();
    }

    /* View Methods */
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

        presenter.changeCategory(selectedCategory);
    }

    @Override
    public void showLoading()
    {
        createProgressDialog = ProgressDialog
                .show(getActivity(), "Creating your plan", "Please wait ...");
    }

    @Override
    public void displayEmptyTitleError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_no_title);
    }

    @Override
    public void displayInvalidTimeError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_invalid_start_time);
    }

    @Override
    public void navigateToInviteScreen(String eventId)
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        screen.navigateToInviteScreen(eventId);
    }

    @Override
    public void displayError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_default);
    }

    /* Helper Methods */
    private void createEvent()
    {
        String eventTitle = etTitle.getText().toString();
        String eventDescription = etDescription.getText().toString();
        DateTime start = DateTimeUtil
                .getDateTime(dateTimeUtil.getDate(dayList.get(selectedDay)), startTime);
        DateTime end = DateTimeUtil.getEndTime(start);

        Event.Type type = cbType.isChecked() ? Event.Type.INVITE_ONLY : Event.Type.PUBLIC;

        if (presenter != null)
        {
            presenter.create(eventTitle, type, eventDescription, start, end);
        }

    }

    private void initView()
    {
        dateTimeUtil = new DateTimeUtil();

        // Title Limit
        tvTitleLimit.setText(String.valueOf(AppConstants.TITLE_LENGTH_LIMIT));

        // Start Time
        startTime = LocalTime.now().plusHours(1).withMinuteOfHour(0);
        tvTime.setText(dateTimeUtil.formatTime(startTime));

        // Start Day
        dayList = dateTimeUtil.getDayList();
        dateList = dateTimeUtil.getDayAndDateList();
        selectedDay = 0;
        tvDay.setText(dayList.get(selectedDay));

        // Category
        selectedCategory = (EventCategory) getArguments().getSerializable(ARG_CATEGORY);
        if (selectedCategory == null)
        {
            selectedCategory = EventCategory.GENERAL;
        }
        changeCategory(selectedCategory);

        tvTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayTimePicker();
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

        mivInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayEventTypeDescriptionDialog();
            }
        });

        llCategoryIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayCategorySelectionDialog();
            }
        });

        etTitle.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    tvTitleLimit.setVisibility(View.VISIBLE);
                }
                else
                {
                    tvTitleLimit.setVisibility(View.INVISIBLE);
                }
            }
        });

        etTitle.addTextChangedListener(new TextWatcher()
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
                int remaining = AppConstants.TITLE_LENGTH_LIMIT - s.length();
                tvTitleLimit.setText(String.valueOf(remaining));
            }
        });
    }

    private void initRecyclerView()
    {
        rvLocationSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLocationSuggestions
                .setAdapter(new LocationSuggestionAdapter(new ArrayList<LocationSuggestion>(), this));

        rvLocationSuggestions.setVisibility(View.GONE);
    }

    private void displayEventTypeDescriptionDialog()
    {
        EventTypeInfoDialog.show(getActivity());

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EVENT_TYPE_DIALOG);
        /* Analytics */
    }

    private void displayTimePicker()
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

    private void displayDayPicker()
    {
        DayPickerDialog.show(getActivity(), dateList, selectedDay, new DayPickerDialog.Listener()
        {
            @Override
            public void onDaySelected(int position)
            {
                selectedDay = position;
                tvDay.setText(dayList.get(selectedDay));
            }
        });
    }

    private void displayCategorySelectionDialog()
    {
        EventCategorySelectionDialog.show(getActivity(), new EventCategorySelectionDialog.Listener()
        {
            @Override
            public void onCategorySelected(EventCategory category)
            {
                changeCategory(category);
            }
        });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_PLAN_CATEGORY_SELECTION_DIALOG);
        /* Analytics */
    }

    private void changeCategory(EventCategory category)
    {
        selectedCategory = category;
        ivCategoryIcon.setImageDrawable(CategoryIconFactory
                .get(selectedCategory, Dimensions.EVENT_ICON_SIZE));
        llCategoryIconContainer
                .setBackground(CategoryIconFactory.getIconBackground(selectedCategory));

        if (presenter != null)
        {
            presenter.changeCategory(category);
        }
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
                    svCreate.scrollTo(0, rvLocationSuggestions.getBottom());
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
                svCreate.scrollTo(0, rvLocationSuggestions.getBottom());

                if (presenter != null)
                {
                    if (!isLocationUpdating)
                    {
                        if (s.length() == 0)
                        {
                            presenter.changeCategory(selectedCategory);
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
}