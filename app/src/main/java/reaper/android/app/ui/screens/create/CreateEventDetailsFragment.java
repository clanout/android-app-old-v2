package reaper.android.app.ui.screens.create;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import hotchemi.stringpicker.StringPicker;
import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.Dimensions;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Suggestion;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.invite.core.InviteUsersContainerFragment;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.DateTimeUtils;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class CreateEventDetailsFragment extends BaseFragment
        implements LocationSuggestionAdapter.SuggestionClickListener,
        CreateEventView
{
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_IS_SECRET = "arg_is_secret";
    private static final String ARG_START_DAY = "arg_start_day";
    private static final String ARG_START_TIME = "arg_start_time";

    Bus bus;
    CreateEventPresenter presenter;
    UserService userService;

    /* UI Elements */
    ScrollView parent;
    Toolbar toolbar;
    EditText etTitle;
    View llCategoryIconContainer;
    ImageView ivCategoryIcon;
    EditText etDesc;
    CheckBox cbType;
    View mivInfo;
    TextView tvTime;
    TextView tvDay;
    EditText etLocation;
    RecyclerView rvLocationSuggestions;

    ProgressDialog createProgressDialog;

    /* Data */
    DateTimeUtil dateTimeUtil;

    List<String> dayList;
    int selectedDay;
    LocalTime startTime;

    EventCategory selectedCategory;

    boolean isLocationUpdating;


    public static CreateEventDetailsFragment newInstance(String title, EventCategory category,
                                                         boolean isSecret, String startDay, LocalTime startTime)
    {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putSerializable(ARG_CATEGORY, category);
        args.putBoolean(ARG_IS_SECRET, isSecret);
        args.putString(ARG_START_DAY, startDay);
        args.putSerializable(ARG_START_TIME, startTime);

        CreateEventDetailsFragment fragment = new CreateEventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);

        EventCategory category = (EventCategory) getArguments().getSerializable(ARG_CATEGORY);
        if (category == null)
        {
            category = EventCategory.GENERAL;
        }

        presenter = new CreateEventPresenterImpl(bus, category);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_details_, container, false);

        parent = (ScrollView) view.findViewById(R.id.sv_createEvent);
        toolbar = (Toolbar) view.findViewById(R.id.tb_createEvent);
        etTitle = (EditText) view.findViewById(R.id.etTitle);
        llCategoryIconContainer = view.findViewById(R.id.llCategoryIconContainer);
        ivCategoryIcon = (ImageView) view.findViewById(R.id.ivCategoryIcon);
        etDesc = (EditText) view.findViewById(R.id.etDesc);
        cbType = (CheckBox) view.findViewById(R.id.cbType);
        mivInfo = view.findViewById(R.id.mivInfo);
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
        setHasOptionsMenu(true);

        initView();
        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        CacheManager.getGenericCache().put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.CREATE);

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
                        presenter.changeCategory(selectedCategory);
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
    }

    private void initView()
    {
        dateTimeUtil = new DateTimeUtil();

        // Title
        String inputTitle = getArguments().getString(ARG_TITLE);
        if (inputTitle != null && !inputTitle.isEmpty())
        {
            etTitle.setText(inputTitle);
            etTitle.setSelection(etTitle.getText().length());
        }

        // Type
        boolean isSecret = getArguments().getBoolean(ARG_IS_SECRET);
        cbType.setChecked(isSecret);

        // Start Time
        startTime = (LocalTime) getArguments().getSerializable(ARG_START_TIME);
        if (startTime == null)
        {
            startTime = LocalTime.now().plusHours(1).withMinuteOfHour(0);
            tvTime.setText(dateTimeUtil.formatTime(startTime));
        }

        // Start Day
        dayList = dateTimeUtil.getDayList();
        String startDay = getArguments().getString(ARG_START_DAY);
        if (startDay != null)
        {
            selectedDay = dayList.indexOf(startDay);
        }
        else
        {
            selectedDay = 0;
        }
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

        mivInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayEventTypePopUp();
            }
        });

        llCategoryIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayCategoryChangeDialog();
            }
        });
    }

    private void changeCategory(EventCategory category)
    {
        selectedCategory = category;
        ivCategoryIcon.setImageDrawable(DrawableFactory
                .get(selectedCategory, Dimensions.EVENT_ICON_SIZE));
        llCategoryIconContainer
                .setBackground(DrawableFactory.getIconBackground(selectedCategory));

        presenter.changeCategory(category);
    }

    private void initRecyclerView()
    {
        rvLocationSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLocationSuggestions
                .setAdapter(new LocationSuggestionAdapter(new ArrayList<Suggestion>(), this));
    }

    @Override
    public void onSuggestionClicked(Suggestion suggestion)
    {
        presenter.selectSuggestion(suggestion);
    }

    /* View Methods */
    @Override
    public void displaySuggestions(List<Suggestion> suggestions)
    {
        rvLocationSuggestions.setAdapter(new LocationSuggestionAdapter(suggestions, this));

        if (suggestions.isEmpty())
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
        imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);

        presenter.changeCategory(selectedCategory);
    }

    @Override
    public void showLoading()
    {
        createProgressDialog = ProgressDialog
                .show(getActivity(), "Creating your clan", "Please wait ...");
    }

    @Override
    public void displayEmptyTitleError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        Snackbar.make(getView(), "Title cannot be empty", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void displayInvalidTimeError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        Snackbar.make(getView(), "Start time cannot be before the current time", Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void navigateToInviteScreen(Event event)
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        InviteUsersContainerFragment inviteUsersContainerFragment = new InviteUsersContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT, event);
        bundle.putBoolean(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT, true);
        inviteUsersContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getFragmentManager(), inviteUsersContainerFragment);
    }

    @Override
    public void displayError()
    {
        if (createProgressDialog != null)
        {
            createProgressDialog.dismiss();
        }

        Snackbar.make(getView(), "Unable to make your plan", Snackbar.LENGTH_LONG)
                .show();
    }

    private void displayEventTypePopUp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_event_type, null);
        builder.setView(dialogView);

        builder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
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

    private void displayDayPicker()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_day_picker, null);
        builder.setView(dialogView);

        final StringPicker stringPicker = (StringPicker) dialogView
                .findViewById(R.id.dayPicker);
        stringPicker.setValues(dayList);
        stringPicker.setCurrent(selectedDay);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                tvDay.setText(stringPicker.getCurrentValue());
                selectedDay = stringPicker.getCurrent();
                Timber.d("Selected Day = " + selectedDay);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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

    private void displayCategoryChangeDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.alert_dialog_change_category, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final LinearLayout cafe = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_cafe);
        final LinearLayout movies = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_movie);
        final LinearLayout eatOut = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_eat_out);
        final LinearLayout sports = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_sports);
        final LinearLayout outdoors = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_outdoors);
        final LinearLayout indoors = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_indoors);
        final LinearLayout drinks = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_drinks);
        final LinearLayout shopping = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_shopping);
        final LinearLayout general = (LinearLayout) dialogView
                .findViewById(R.id.ll_dialog_fragment_create_event_general);

        cafe.setBackground(DrawableFactory.getIconBackground(EventCategory.CAFE));
        movies.setBackground(DrawableFactory.getIconBackground(EventCategory.MOVIES));
        eatOut.setBackground(DrawableFactory.getIconBackground(EventCategory.EAT_OUT));
        sports.setBackground(DrawableFactory.getIconBackground(EventCategory.SPORTS));
        outdoors.setBackground(DrawableFactory.getIconBackground(EventCategory.OUTDOORS));
        indoors.setBackground(DrawableFactory.getIconBackground(EventCategory.INDOORS));
        drinks.setBackground(DrawableFactory.getIconBackground(EventCategory.DRINKS));
        shopping.setBackground(DrawableFactory.getIconBackground(EventCategory.SHOPPING));
        general.setBackground(DrawableFactory.getIconBackground(EventCategory.GENERAL));

        cafe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.CAFE);
                alertDialog.dismiss();
            }
        });

        movies.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.MOVIES);
                alertDialog.dismiss();
            }
        });


        eatOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.EAT_OUT);
                alertDialog.dismiss();
            }
        });


        sports.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.SPORTS);
                alertDialog.dismiss();
            }
        });


        outdoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.OUTDOORS);
                alertDialog.dismiss();
            }
        });


        indoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.INDOORS);
                alertDialog.dismiss();
            }
        });


        drinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.DRINKS);
                alertDialog.dismiss();
            }
        });


        shopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.SHOPPING);
                alertDialog.dismiss();
            }
        });


        general.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeCategory(EventCategory.GENERAL);
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_create, menu);

        Drawable drawable = MaterialDrawableBuilder
                .with(Reaper.getReaperContext())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(Color.WHITE)
                .build();

        menu.findItem(R.id.action_create).setIcon(drawable);

        menu.findItem(R.id.action_create)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {

                    SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                    String eventTitle = etTitle.getText().toString();
                    String eventDescription = etDesc.getText().toString();
                    DateTime start = DateTimeUtils
                            .getDateTime(dateTimeUtil.getDate(dayList.get(selectedDay)), startTime);
                    DateTime end = DateTimeUtils.getEndTime(start);

                    Event.Type type = cbType
                            .isChecked() ? Event.Type.INVITE_ONLY : Event.Type.PUBLIC;

                    presenter
                            .create(eventTitle, type, eventDescription, start, end);

                    AnalyticsHelper
                            .sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.CREATE_EVENT_CLICKED_FROM_DETAILS, userService
                                    .getActiveUserId());

                    return true;
                }
            });
    }
}
