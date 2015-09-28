package reaper.android.app.ui.screens.edit;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import reaper.android.R;
import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.GoogleService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventEditedTrigger;
import reaper.android.app.trigger.event.EventLocationFetchedTrigger;
import reaper.android.app.trigger.event.EventSuggestionsTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.create.EventSuggestionsAdapter;
import reaper.android.app.ui.screens.create.GooglePlacesAutocompleteAdapter;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;


public class EditEventFragment extends BaseFragment implements AdapterView.OnItemClickListener, EventSuggestionsAdapter.EventSuggestionsClickListener, View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private EventService eventService;
    private LocationService locationService;
    private UserService userService;
    private GoogleService googleService;
    private GenericCache genericCache;
    private EventCache eventCache;

    // Data
    private Event event, editedEvent;
    private EventDetails eventDetails;

    // UI Elements
    private TextView title;
    private TextView type;
    private MaterialIconView icon;
    private AutoCompleteTextView eventLocation;
    private TextView schedule;
    private EditText description;
    private RecyclerView recommendationList;
    private TextView noSuggestions;
    private ImageButton save;
    private Drawable deleteDrawable, lockedDrawable, unlockedDrawable;
    private Toolbar toolbar;

    private boolean isPlaceDetailsRunning, isSaveButtonClicked, shouldDeleteEventFromCache;

    private List<Suggestion> suggestionList;
    private EventSuggestionsAdapter eventSuggestionsAdapter;
    private Drawable checkDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        title = (TextView) view.findViewById(R.id.tv_edit_event_title);
        type = (TextView) view.findViewById(R.id.tv_edit_event_type);
        icon = (MaterialIconView) view.findViewById(R.id.miv_edit_event_icon);
        eventLocation = (AutoCompleteTextView) view.findViewById(R.id.actv_edit_event_location);
        schedule = (TextView) view.findViewById(R.id.tv_edit_event_date_time);
        description = (EditText) view.findViewById(R.id.et_edit_event_description);
        recommendationList = (RecyclerView) view.findViewById(R.id.rv_edit_event_suggestions);
        noSuggestions = (TextView) view.findViewById(R.id.tv_edit_event_no_suggestions);
        save = (ImageButton) view.findViewById(R.id.ib_edit_event_save);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_edit);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        event = (Event) bundle.get(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT);
        eventDetails = (EventDetails) bundle.get(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT_DETAILS);

        if (event == null || eventDetails == null)
        {
            throw new IllegalStateException("Event/EventDetails cannot be null while creating EditEventFragment instance");
        }

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        editedEvent = new Event();
        editedEvent.setId(event.getId());
        editedEvent.setIsFinalized(event.isFinalized());
        editedEvent.setEndTime(event.getEndTime());
        editedEvent.setStartTime(event.getStartTime());
        editedEvent.setLocation(event.getLocation());

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getFragmentManager();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        userService = new UserService(bus);
        googleService = new GoogleService(bus);

        genericCache = CacheManager.getGenericCache();
        eventCache = CacheManager.getEventCache();

        suggestionList = new ArrayList<>();

        isPlaceDetailsRunning = false;
        isSaveButtonClicked = false;
        shouldDeleteEventFromCache = false;

        schedule.setOnClickListener(this);
        save.setOnClickListener(this);

        generateDrawables();
        save.setImageDrawable(checkDrawable);

        render();
        initGoogleAutocompleteAdapter();
        initRecyclerView();
    }

    private void generateDrawables()
    {
        deleteDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        lockedDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        unlockedDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        checkDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(24)
                .build();
    }

    private void render()
    {
        EventCategory category = EventCategory.valueOf(event.getCategory());
        switch (category)
        {
            case GENERAL:
                icon.setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
                break;
            case EAT_OUT:
                icon.setIcon(MaterialDrawableBuilder.IconValue.FOOD);
                break;
            case DRINKS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.MARTINI);
                break;
            case CAFE:
                icon.setIcon(MaterialDrawableBuilder.IconValue.COFFEE);
                break;
            case MOVIES:
                icon.setIcon(MaterialDrawableBuilder.IconValue.MOVIE);
                break;
            case OUTDOORS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.TENNIS);
                break;
            case PARTY:
                icon.setIcon(MaterialDrawableBuilder.IconValue.GIFT);
                break;
            case LOCAL_EVENTS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.CITY);
                break;
            case SHOPPING:
                icon.setIcon(MaterialDrawableBuilder.IconValue.SHOPPING);
                break;
            default:
                icon.setIcon(MaterialDrawableBuilder.IconValue.SHOPPING);
        }


        title.setText(event.getTitle());

        if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
        {
            eventLocation.setText("");
        } else
        {
            eventLocation.setText(event.getLocation().getName());
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        schedule.setText(event.getStartTime().toString(timeFormatter) + " (" + event.getStartTime().toString(dateFormatter) + ")");

        if (event.getType() == Event.Type.PUBLIC)
        {
            type.setText(R.string.event_details_type_public);
        } else
        {
            type.setText(R.string.event_details_type_invite_only);
        }

        description.setText(eventDetails.getDescription());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        bus.unregister(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EDIT_EVENT_FRAGMENT);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Edit");

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.EDIT);

        bus.register(this);

        Location userLocation = locationService.getUserLocation();
        eventService.fetchEventSuggestions(EventCategory.valueOf(event.getCategory()), String.valueOf(userLocation.getLatitude()), String.valueOf(userLocation.getLongitude()));
    }

    private void initGoogleAutocompleteAdapter()
    {
        eventLocation.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), R.layout.list_item_autocomplete, R.id.tv_list_item_autocomplete, bus, locationService));
        eventLocation.setOnItemClickListener(this);
    }

    private void initRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recommendationList.setAdapter(eventSuggestionsAdapter);
        recommendationList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recommendationList.setAdapter(eventSuggestionsAdapter);

        if (suggestionList.size() == 0)
        {
            noSuggestions.setText(R.string.no_suggestions);
            noSuggestions.setVisibility(View.VISIBLE);
            recommendationList.setVisibility(View.GONE);

        } else
        {
            recommendationList.setVisibility(View.VISIBLE);
            noSuggestions.setVisibility(View.GONE);
        }
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);

        if (EventUtils.canDeleteEvent(event, userService.getActiveUserId()))
        {
            menu.findItem(R.id.action_delete_event).setVisible(true);
            menu.findItem(R.id.action_delete_event).setIcon(deleteDrawable);
            menu.findItem(R.id.action_delete_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.event_delete_heading);

                    builder.setPositiveButton(R.string.even_delete_positive_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            eventService.deleteEvent(event.getId());
                            Toast.makeText(getActivity(), R.string.event_delete_confirmation, Toast.LENGTH_LONG).show();
                            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
                        }
                    });

                    builder.setNegativeButton(R.string.even_delete_negative_button, new DialogInterface.OnClickListener()
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
        } else
        {
            menu.findItem(R.id.action_delete_event).setVisible(false);
        }

        if (EventUtils.canFinaliseEvent(event, userService.getActiveUserId()))
        {
            if (editedEvent.isFinalized())
            {
                menu.findItem(R.id.action_finalize_event).setIcon(lockedDrawable);
                menu.findItem(R.id.action_finalize_event).setVisible(true);
            } else
            {
                menu.findItem(R.id.action_finalize_event).setIcon(unlockedDrawable);
                menu.findItem(R.id.action_finalize_event).setVisible(true);
            }

            menu.findItem(R.id.action_finalize_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {

                    if (editedEvent.isFinalized())
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle(R.string.event_unlock_heading);
                        builder.setMessage(R.string.unfinalise_event);

                        builder.setPositiveButton(R.string.event_unlock_positive_button, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                shouldDeleteEventFromCache = false;
                                event.setIsFinalized(false);
                                eventCache.save(event);

                                eventService.finaliseEvent(event, false);

                                eventService.fetchEvents(locationService.getUserLocation().getZone());
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

                    } else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle(R.string.event_lock_heading);
                        builder.setMessage(R.string.finalise_event);

                        builder.setPositiveButton(R.string.event_lock_positive_button, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                shouldDeleteEventFromCache = false;
                                event.setIsFinalized(true);
                                eventCache.save(event);

                                eventService.finaliseEvent(event, true);

                                eventService.fetchEvents(locationService.getUserLocation().getZone());
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
        } else
        {
            menu.findItem(R.id.action_finalize_event).setVisible(false);
        }
    }

    @Subscribe
    public void onEventSuggestionsFetched(EventSuggestionsTrigger trigger)
    {
        suggestionList = trigger.getRecommendations();

        refreshRecyclerView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        GooglePlacesAutocompleteAdapter adapter = (GooglePlacesAutocompleteAdapter) parent.getAdapter();
        GooglePlaceAutocompleteApiResponse.Prediction prediction = adapter.getItem(position);

        googleService.getPlaceDetails(prediction.getPlaceId());
    }

    @Override
    public void onEventSuggestionsClicked(View view, int position)
    {
        eventLocation.setText(suggestionList.get(position).getName());

        Location editedEventLocation = new Location();
        editedEventLocation.setName(suggestionList.get(position).getName());
        editedEventLocation.setLatitude(suggestionList.get(position).getLatitude());
        editedEventLocation.setLongitude(suggestionList.get(position).getLongitude());
        editedEventLocation.setZone(locationService.getUserLocation().getZone());

        editedEvent.setLocation(editedEventLocation);

    }

    @Subscribe
    public void onEventLocationFetched(EventLocationFetchedTrigger trigger)
    {
        Location editedEventLocation = new Location();

        editedEventLocation.setName(trigger.getLocation().getName());
        editedEventLocation.setLatitude(trigger.getLocation().getLatitude());
        editedEventLocation.setLongitude(trigger.getLocation().getLongitude());
        editedEventLocation.setZone(locationService.getUserLocation().getZone());

        editedEvent.setLocation(editedEventLocation);

        isPlaceDetailsRunning = false;

        if (isSaveButtonClicked)
        {
            sendEditEventRequest();
        }
    }

    @Subscribe
    public void onEventLocationNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENT_LOCATION_FETCH_FAILURE)
        {
            Snackbar.make(getView(), R.string.location_not_found, Snackbar.LENGTH_LONG).show();
            eventLocation.setText("");

            Location editedEventLocation = new Location();
            editedEventLocation.setName(null);
            editedEventLocation.setLatitude(null);
            editedEventLocation.setLongitude(null);
            editedEventLocation.setZone(null);

            editedEvent.setLocation(editedEventLocation);

            isPlaceDetailsRunning = false;

            if (isSaveButtonClicked)
            {
                sendEditEventRequest();
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.ib_edit_event_save)
        {

            if (isPlaceDetailsRunning)
            {
                isSaveButtonClicked = true;
            } else
            {
                isSaveButtonClicked = false;
                sendEditEventRequest();
            }
        }

        if (v.getId() == R.id.tv_edit_event_date_time)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View selectTimingsDialogView = inflater.inflate(R.layout.alert_dialog_select_date_time, null);
            builder.setView(selectTimingsDialogView);

            final TimePicker startTimePicker = (TimePicker) selectTimingsDialogView.findViewById(R.id.tp_select_time_start);
            final DatePicker startDatePicker = (DatePicker) selectTimingsDialogView.findViewById(R.id.dp_select_date_start);

            renderDateAndTimePickers(startDatePicker, startTimePicker);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    DateTime startDateTime = getTime(startDatePicker, startTimePicker);
                    DateTime endDateTime = startDateTime.plusDays(1).withTimeAtStartOfDay();

                    renderEventTimings(startDateTime, endDateTime);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }


    }

    private void renderEventTimings(DateTime _startDateTime, DateTime _endDateTime)
    {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        if (_startDateTime.isAfter(_endDateTime))
        {
            schedule.setText("Select event timings");

            editedEvent.setEndTime(null);
            editedEvent.setStartTime(null);
            editedEvent.setStartTime(null);
        } else
        {
            editedEvent.setStartTime(_startDateTime);
            editedEvent.setEndTime(_endDateTime);


            schedule.setText(_startDateTime.toString(timeFormatter) + " (" + _startDateTime.toString(dateFormatter) + ")");

        }
    }

    private DateTime getTime(DatePicker datePicker, TimePicker timePicker)
    {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int date = datePicker.getDayOfMonth();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        String timeZone = DateTime.now().toString(DateTimeFormat.forPattern("Z"));

        StringBuilder formattedDate = new StringBuilder();
        formattedDate.append(year);
        formattedDate.append("-");
        formattedDate.append(String.format("%02d", month));
        formattedDate.append("-");
        formattedDate.append(String.format("%02d", date));
        formattedDate.append("T");
        formattedDate.append(String.format("%02d", hour));
        formattedDate.append(":");
        formattedDate.append(String.format("%02d", minute));
        formattedDate.append(":00.000");
        formattedDate.append(timeZone);
        DateTime dateTime = DateTime.parse(formattedDate.toString());

        return dateTime;
    }

    private void renderDateAndTimePickers(DatePicker startDatePicker, TimePicker startTimePicker)
    {
        startTimePicker.setIs24HourView(true);

        hideYearinDatePicker(startDatePicker);
        startDatePicker.setMinDate(System.currentTimeMillis() - 1000);

        if (editedEvent.getStartTime() == null || editedEvent.getEndTime() == null)
        {
            startTimePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        } else
        {
            startTimePicker.setCurrentHour(editedEvent.getStartTime().getHourOfDay());
            startTimePicker.setCurrentMinute(editedEvent.getStartTime().getMinuteOfHour());

            startDatePicker.updateDate(editedEvent.getStartTime().getYear(), editedEvent.getStartTime().getMonthOfYear() - 1, editedEvent.getStartTime().getDayOfMonth());
        }
    }

    private void hideYearinDatePicker(DatePicker datePicker)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int yearSpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
            if (yearSpinnerId != 0)
            {
                View yearSpinner = datePicker.findViewById(yearSpinnerId);
                if (yearSpinner != null)
                {
                    yearSpinner.setVisibility(View.GONE);
                }
            }
        } else
        {
            try
            {
                Field f[] = datePicker.getClass().getDeclaredFields();
                for (Field field : f)
                {
                    if (field.getName().equals("mYearPicker"))
                    {
                        field.setAccessible(true);
                        Object yearPicker = new Object();
                        yearPicker = field.get(datePicker);
                        ((View) yearPicker).setVisibility(View.GONE);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendEditEventRequest()
    {
        String descriptionEvent = description.getText().toString();

        if (descriptionEvent == null)
        {
            descriptionEvent = "";
        }

        if (eventLocation.getText().toString().isEmpty() || eventLocation.getText().toString() == null)
        {
            if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
            {
                Location editedEventLocation = new Location();
                editedEventLocation.setName(null);
                editedEventLocation.setLatitude(null);
                editedEventLocation.setLongitude(null);
                editedEventLocation.setZone(null);

                editedEvent.setLocation(editedEventLocation);
            } else
            {
                Location editedEventLocation = new Location();
                editedEventLocation.setName(null);
                editedEventLocation.setLatitude(null);
                editedEventLocation.setLongitude(null);
                editedEventLocation.setZone(locationService.getUserLocation().getZone());

                editedEvent.setLocation(editedEventLocation);
            }
        } else
        {
            if (event.getLocation().getName() == null || event.getLocation().getLatitude() == null || event.getLocation().getLongitude() == null || event.getLocation().getZone() == null)
            {
                if (event.getLocation().getName() == null)
                {
                    if (editedEvent.getLocation().getLongitude() == null || editedEvent.getLocation().getLatitude() == null)
                    {
                        Location editedEventLocation = new Location();
                        editedEventLocation.setZone(locationService.getUserLocation().getZone());
                        editedEventLocation.setLongitude(null);
                        editedEventLocation.setLatitude(null);
                        editedEventLocation.setName(eventLocation.getText().toString());

                        editedEvent.setLocation(editedEventLocation);
                    }

                } else
                {
                    if (editedEvent.getLocation().getLongitude() == null || editedEvent.getLocation().getLatitude() == null)
                    {
                        if (event.getLocation().getName().equals(eventLocation.getText().toString()))
                        {
                            Location editedEventLocation = new Location();
                            editedEventLocation.setZone(null);
                            editedEventLocation.setLongitude(null);
                            editedEventLocation.setLatitude(null);
                            editedEventLocation.setName(null);

                            editedEvent.setLocation(editedEventLocation);
                        } else
                        {
                            Location editedEventLocation = new Location();
                            editedEventLocation.setZone(locationService.getUserLocation().getZone());
                            editedEventLocation.setLongitude(null);
                            editedEventLocation.setLatitude(null);
                            editedEventLocation.setName(eventLocation.getText().toString());

                            editedEvent.setLocation(editedEventLocation);
                        }
                    }
                }
            } else
            {
                if ((event.getLocation().getName().equals(editedEvent.getLocation().getName())) && (event.getLocation().getLatitude().equals(editedEvent.getLocation().getLatitude())) && (event.getLocation().getLongitude().equals(editedEvent.getLocation().getLongitude())) && (event.getLocation().getZone().equals(editedEvent.getLocation().getZone())))
                {
                    Location editedEventLocation = new Location();
                    editedEventLocation.setZone(null);
                    editedEventLocation.setLongitude(null);
                    editedEventLocation.setLatitude(null);
                    editedEventLocation.setName(null);

                    editedEvent.setLocation(editedEventLocation);
                }
            }
        }

        if ((event.getStartTime().equals(editedEvent.getStartTime())) && (event.getEndTime().equals(editedEvent.getEndTime())))
        {
            editedEvent.setEndTime(null);
            editedEvent.setStartTime(null);
        }

        shouldDeleteEventFromCache = true;
        eventService.editEvent(event.getId(), editedEvent.getStartTime(), editedEvent.getEndTime(), editedEvent.getLocation(), descriptionEvent);
    }

    @Subscribe
    public void onEventEdited(EventEditedTrigger trigger)
    {
        event = trigger.getEvent();
        eventService.fetchEvents(locationService.getUserLocation().getZone());
    }

    @Subscribe
    public void onEventsFetchedEdit(EventsFetchTrigger trigger)
    {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(event.getId());

        int activePosition = events.indexOf(activeEvent);

        events.set(activePosition, event);

        if (shouldDeleteEventFromCache)
        {
            eventCache.deleteCompletely(event.getId());
            eventCache.save(event);
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }

    @Subscribe
    public void onEventEditFailed(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENT_EDIT_FAILURE)
        {
            Snackbar.make(getView(), R.string.messed_up, Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void backPressed(BackPressedTrigger trigger)
    {
        if (trigger.getActiveFragment().equals(BackstackTags.EDIT))
        {
            shouldDeleteEventFromCache = false;
            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }
}
