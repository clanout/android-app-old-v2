package reaper.android.app.ui.screens.create;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.service.EventService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.dialog.EventCategorySelectionDialog;
import reaper.android.app.ui.dialog.EventTypeInfoDialog;
import reaper.android.app.ui.screens.create.mvp.CreateEventPresenter;
import reaper.android.app.ui.screens.create.mvp.CreateEventPresenterImpl;
import reaper.android.app.ui.screens.create.mvp.CreateEventView;
import reaper.android.app.ui.util.CategoryIconFactory;
import reaper.android.app.model.util.DateTimeUtil;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.app.ui.util.VisibilityAnimationUtil;
import reaper.android.common.analytics.AnalyticsHelper;

public class CreateFragment extends BaseFragment implements
        CreateEventView
{
    private static final String ARG_CATEGORY = "arg_category";

    public static CreateFragment newInstance(EventCategory category)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);

        CreateFragment fragment = new CreateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    CreateScreen screen;

    CreateEventPresenter presenter;

    /* UI Elements */
    @Bind(R.id.tilTitle)
    TextInputLayout tilTitle;

    @Bind(R.id.etTitle)
    EditText etTitle;

    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.cbType)
    CheckBox cbType;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvDay)
    TextView tvDay;

    @Bind(R.id.llMoreDetails)
    View llMoreDetails;

    @Bind(R.id.llMoreDetailsContainer)
    View llMoreDetailsContainer;

    @Bind(R.id.tvLocation)
    TextView tvLocation;

    @Bind(R.id.etDescription)
    EditText etDescription;

    ProgressDialog createProgressDialog;

    /* Data */
    LocalTime startTime;
    LocalDate startDate;
    EventCategory selectedCategory;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

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
        screen = (CreateScreen) getActivity();

        initView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_create, menu);

        MenuItem create = menu.findItem(R.id.action_create);
        Drawable drawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();

        create.setIcon(drawable);
        create.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                createEvent();
                return true;
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.llMoreDetails)
    public void onMoreDetailsToggled()
    {
        llMoreDetails.setVisibility(View.GONE);
        if (llMoreDetailsContainer.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llMoreDetailsContainer, 200);
        }
    }

    @OnClick(R.id.llTime)
    public void onTimeClicked()
    {
        displayTimePicker();
    }

    @OnClick(R.id.llDay)
    public void onDayClicked()
    {
        displayDayPicker();
    }

    @OnClick(R.id.mivInfo)
    public void onTypeInfoClicked()
    {
        displayEventTypeDescriptionDialog();
    }

    @OnClick(R.id.llCategoryIconContainer)
    public void onCategoryClicked()
    {
        displayCategorySelectionDialog();
    }

    /* View Methods */
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

        tilTitle.setError(getString(R.string.error_no_title));
        tilTitle.setErrorEnabled(true);
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
        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

        String eventTitle = etTitle.getText().toString();
        String eventDescription = etDescription.getText().toString();

        DateTime start = DateTimeUtil.getDateTime(startDate, startTime);

        Event.Type type = cbType.isChecked() ? Event.Type.INVITE_ONLY : Event.Type.PUBLIC;

        if (presenter != null)
        {
            // TODO : Location selection (create)
            presenter.create(eventTitle, type, selectedCategory, eventDescription, start, null);
        }
    }

    private void initView()
    {
        // Start Time
        startTime = LocalTime.now().plusHours(1).withMinuteOfHour(0);
        tvTime.setText(DateTimeUtil.formatTime(startTime));

        // Start Date
        startDate = LocalDate.now();
        tvDay.setText(DateTimeUtil.formatDate(startDate));

        // Category
        selectedCategory = (EventCategory) getArguments().getSerializable(ARG_CATEGORY);
        if (selectedCategory == null)
        {
            selectedCategory = EventCategory.GENERAL;
        }
        changeCategory(selectedCategory);

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
                tilTitle.setErrorEnabled(false);
            }
        });
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
                                tvTime.setText(DateTimeUtil.formatTime(startTime));
                            }
                        },
                        startTime.getHourOfDay(),
                        startTime.getMinuteOfHour(),
                        false);

        dialog.dismissOnPause(true);
        dialog.vibrate(false);
        dialog.setTitle("Start Time");
        dialog.show(getFragmentManager(), "TimePicker");
    }

    private void displayDayPicker()
    {
        DatePickerDialog dialog = DatePickerDialog
                .newInstance(
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth)
                            {
                                startDate = new LocalDate(year, (monthOfYear + 1), dayOfMonth);
                                tvDay.setText(DateTimeUtil.formatDate(startDate));
                            }
                        },
                        startDate.getYear(),
                        (startDate.getMonthOfYear() - 1),
                        startDate.getDayOfMonth());

        dialog.dismissOnPause(true);
        dialog.vibrate(false);

        LocalDate today = LocalDate.now();

        int startYear = today.getYear();
        int endYear = today.getYear();
        dialog.setYearRange(startYear, endYear);

        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.DAY_OF_MONTH, today.getDayOfMonth());
        minDate.set(Calendar.MONTH, (today.getMonthOfYear() - 1));
        minDate.set(Calendar.YEAR, today.getYear());
        dialog.setMinDate(minDate);

        dialog.show(getFragmentManager(), "DatePicker");
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
        AnalyticsHelper
                .sendScreenNames(GoogleAnalyticsConstants.SCREEN_PLAN_CATEGORY_SELECTION_DIALOG);
        /* Analytics */
    }

    private void changeCategory(EventCategory category)
    {
        selectedCategory = category;
        ivCategoryIcon.setImageDrawable(CategoryIconFactory
                .get(selectedCategory, Dimensions.CATEGORY_ICON_LARGE));
        llCategoryIconContainer
                .setBackground(CategoryIconFactory.getIconBackground(selectedCategory));
    }
}