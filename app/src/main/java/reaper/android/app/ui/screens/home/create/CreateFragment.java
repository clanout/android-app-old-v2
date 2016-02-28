package reaper.android.app.ui.screens.home.create;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.LocalTime;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.Dimensions;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.service.EventService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.DayPickerDialog;
import reaper.android.app.ui.dialog.EventCategorySelectionDialog;
import reaper.android.app.ui.dialog.EventTypeInfoDialog;
import reaper.android.app.ui.screens.home.HomeScreen;
import reaper.android.app.ui.screens.home.create.mvp.CreateEventPresenter;
import reaper.android.app.ui.screens.home.create.mvp.CreateEventPresenterImpl;
import reaper.android.app.ui.screens.home.create.mvp.CreateEventView;
import reaper.android.app.ui.screens.invite.InviteActivity;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.SnackbarFactory;

public class CreateFragment extends BaseFragment implements CreateEventView
{
    public static CreateFragment newInstance()
    {
        return new CreateFragment();
    }

    HomeScreen screen;

    CreateEventPresenter presenter;

    /* UI Elements */
    @Bind(R.id.mivClose)
    View mivClose;

    @Bind(R.id.etTitle)
    EditText etTitle;

    @Bind(R.id.tvTitleLimit)
    TextView tvTitleLimit;

    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.cbType)
    CheckBox cbType;

    @Bind(R.id.mivInfo)
    View mivInfo;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvDay)
    TextView tvDay;

    @Bind(R.id.fabCreate)
    FloatingActionButton fabCreate;

    @Bind(R.id.llMoreDetails)
    View llMoreDetails;

    ProgressDialog createProgressDialog;

    /* Data */
    DateTimeUtil dateTimeUtil;
    List<String> dayList;
    List<String> dateList;
    int selectedDay;
    LocalTime startTime;

    EventCategory selectedCategory;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        LocationService_ locationService = LocationService_.getInstance();
        presenter = new CreateEventPresenterImpl(eventService, locationService);
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
        screen = (HomeScreen) getActivity();

        initCreateView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presenter.detachView();
    }

    /* View Methods */
    @Override
    public void displayEmptyTitleErrorMessage()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_no_title);
    }

    @Override
    public void displayInvalidStartTimeErrorMessage()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_invalid_start_time);
    }

    @Override
    public void showLoading()
    {
        createProgressDialog = ProgressDialog
                .show(getActivity(), "Creating your clan", "Please wait ...");
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

    @Override
    public void navigateToInviteScreen(Event event)
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        startActivity(InviteActivity.callingIntent(getActivity(), true, event.getId()));
        getActivity().finish();
    }

    /* Helper Methods */
    private void createEvent()
    {
        presenter.create(
                etTitle.getText().toString(),
                selectedCategory,
                cbType.isChecked(),
                DateTimeUtil.getDateTime(dateTimeUtil
                        .getDate(dayList.get(selectedDay)), startTime));
    }

    private void initCreateView()
    {
        mivClose.setVisibility(View.GONE);

        dateTimeUtil = new DateTimeUtil();

        tvTitleLimit.setText(String.valueOf(AppConstants.TITLE_LENGTH_LIMIT));

        dayList = dateTimeUtil.getDayList();
        dateList = dateTimeUtil.getDayAndDateList();
        selectedDay = 0;
        tvDay.setText(dayList.get(selectedDay));

        startTime = LocalTime.now().plusHours(1).withMinuteOfHour(0);
        tvTime.setText(dateTimeUtil.formatTime(startTime));

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

        changeCategory(EventCategory.GENERAL);
        llCategoryIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayCategorySelectionDialog();
            }
        });

        fabCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createEvent();
            }
        });

        llMoreDetails.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                screen.navigateToCreateDetailsScreen(
                        etTitle.getText().toString(),
                        selectedCategory,
                        cbType.isChecked(),
                        dayList.get(selectedDay),
                        startTime);
            }
        });

        tvTitleLimit.setText(String.valueOf(AppConstants.TITLE_LENGTH_LIMIT));

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

    private void displayEventTypeDescriptionDialog()
    {
        EventTypeInfoDialog.show(getActivity());
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
    }

    private void changeCategory(EventCategory category)
    {
        selectedCategory = category;
        ivCategoryIcon.setImageDrawable(DrawableFactory
                .get(selectedCategory, Dimensions.EVENT_ICON_SIZE));
        llCategoryIconContainer.setBackground(DrawableFactory.getIconBackground(selectedCategory));
    }
}
