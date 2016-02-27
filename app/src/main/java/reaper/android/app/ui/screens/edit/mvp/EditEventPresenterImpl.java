package reaper.android.app.ui.screens.edit.mvp;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.EventUtils;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditEventPresenterImpl implements EditEventPresenter
{
    /* Services */
    private EventService eventService;
    private UserService userService;
    private LocationService_ locationService;
    private PlacesService placesService;

    /* Data */
    private Event originalEvent;
    private String originalDescription;

    private DateTime updatedStartTime;
    private Location updatedLocation;
    private String updatedDescription;

    private boolean isTimeUpdated;
    private boolean isLocationUpdated;
    private boolean isDescriptionUpdated;

    private boolean isAutocompleteInProgress;
    private boolean isLocationFetchInProgress;
    private boolean isEditInitiated;

    private CompositeSubscription subscriptions;

    /* View */
    private EditEventView view;

    public EditEventPresenterImpl(EventService eventService, UserService userService, LocationService_ locationService,
                                  PlacesService placesService, Event originalEvent, EventDetails originalEventDetails)
    {
        this.eventService = eventService;
        this.userService = userService;
        this.locationService = locationService;
        this.placesService = placesService;

        this.originalEvent = originalEvent;
        this.originalDescription = originalEventDetails.getDescription();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditEventView view)
    {
        this.view = view;
        this.view.init(originalEvent, originalDescription);


        if (EventUtils.canDeleteEvent(originalEvent, userService.getSessionUserId()))
        {
            this.view.displayDeleteOption();
        }

        if (EventUtils.canFinaliseEvent(originalEvent, userService.getSessionUserId()))
        {
            if (originalEvent.isFinalized())
            {
                this.view.displayUnfinalizationOption();
            }
            else
            {
                this.view.displayFinalizationOption();
            }
        }

        fetchSuggestions();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void finalizeEvent()
    {
        Subscription subscription =
                eventService
                        ._finaliseEvent(originalEvent, true)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>()
                        {
                            @Override
                            public void onCompleted()
                            {
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                            }

                            @Override
                            public void onNext(Boolean isFinalized)
                            {
                                if (isFinalized)
                                {
                                    view.navigateToDetailsScreen(originalEvent.getId());
                                }
                                else
                                {
                                    view.displayError();
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void unfinalizeEvent()
    {
        Subscription subscription = eventService
                ._finaliseEvent(originalEvent, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Boolean isUnfinalized)
                    {
                        if (!isUnfinalized)
                        {
                            view.displayError();
                        }
                        else
                        {
                            view.displayFinalizationOption();
                        }
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void delete()
    {
        Subscription subscription = eventService
                ._deleteEvent(originalEvent.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Boolean isSuccessful)
                    {
                        if (isSuccessful)
                        {
                            view.navigateToHomeScreen();
                        }
                        else
                        {
                            view.displayError();
                        }
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void updateTime(DateTime newTime)
    {
        updatedStartTime = newTime;
        isTimeUpdated = true;
    }

    @Override
    public void setLocationName(String locationName)
    {
        Location userLocation = locationService.getCurrentLocation();

        isLocationUpdated = true;
        updatedLocation = new Location();
        updatedLocation.setZone(userLocation.getZone());
        updatedLocation.setName(locationName);
    }

    @Override
    public void selectSuggestion(final LocationSuggestion locationSuggestion)
    {
        isLocationFetchInProgress = true;

        final Location userLocation = locationService.getCurrentLocation();
        if (locationSuggestion.getLatitude() != null && locationSuggestion.getLongitude() != null)
        {
            isLocationUpdated = true;

            updatedLocation = new Location();
            updatedLocation.setZone(userLocation.getZone());
            updatedLocation.setLatitude(locationSuggestion.getLatitude());
            updatedLocation.setLongitude(locationSuggestion.getLongitude());
            updatedLocation.setName(locationSuggestion.getName());
            view.setLocation(updatedLocation.getName());

            isLocationFetchInProgress = false;
            if (isEditInitiated)
            {
                editEvent();
            }
        }
        else
        {
            if (locationSuggestion.getId() != null)
            {
                Subscription subscription = placesService
                        ._getPlaceDetails(locationSuggestion.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Location>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isLocationFetchInProgress = false;

                                if (isEditInitiated)
                                {
                                    editEvent();
                                }
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                isLocationUpdated = true;

                                updatedLocation = new Location();
                                updatedLocation.setZone(userLocation.getZone());
                                updatedLocation.setName(locationSuggestion.getName());
                                view.setLocation(updatedLocation.getName());

                                isLocationFetchInProgress = false;

                                if (isEditInitiated)
                                {
                                    editEvent();
                                }
                            }

                            @Override
                            public void onNext(Location location)
                            {
                                isLocationUpdated = true;

                                updatedLocation = location;
                                view.setLocation(location.getName());
                            }
                        });

                subscriptions.add(subscription);
            }
            else
            {
                if (view != null)
                {
                    isLocationUpdated = true;

                    updatedLocation = new Location();
                    updatedLocation.setZone(userLocation.getZone());
                    updatedLocation.setName(locationSuggestion.getName());
                    view.setLocation(updatedLocation.getName());
                }

                isLocationFetchInProgress = false;
                if (isEditInitiated)
                {
                    editEvent();
                }
            }
        }
    }

    @Override
    public void setDescription(String description)
    {
        isDescriptionUpdated = true;
        updatedDescription = description;
    }

    @Override
    public void autocomplete(final String input)
    {
        if (isAutocompleteInProgress)
        {
            return;
        }

        isAutocompleteInProgress = true;

        Location userLocation = locationService.getCurrentLocation();
        Subscription subscription =
                placesService
                        ._autocomplete(userLocation.getLatitude(), userLocation
                                .getLongitude(), input)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<LocationSuggestion>>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isAutocompleteInProgress = false;
                            }

                            @Override
                            public void onError(Throwable e)
                            {

                            }

                            @Override
                            public void onNext(List<LocationSuggestion> suggestions)
                            {
                                view.displaySuggestions(suggestions);
                            }
                        });
        subscriptions.add(subscription);
    }

    @Override
    public void fetchSuggestions()
    {
        Location userLocation = locationService.getCurrentLocation();
        Subscription subscription = eventService
                ._fetchLocationSuggestions(EventCategory.valueOf(originalEvent.getCategory()), userLocation
                        .getLatitude(), userLocation
                        .getLongitude())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<LocationSuggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displaySuggestions(new ArrayList<LocationSuggestion>());
                    }

                    @Override
                    public void onNext(List<LocationSuggestion> suggestions)
                    {
                        List<LocationSuggestion> locationSuggestionList = new ArrayList<LocationSuggestion>();
                        int count = 0;
                        for (LocationSuggestion suggestion : suggestions)
                        {
                            if (count == 5)
                            {
                                break;
                            }
                            locationSuggestionList.add(suggestion);
                            count++;
                        }
                        view.displaySuggestions(locationSuggestionList);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void edit()
    {
        view.showLoading();

        isEditInitiated = true;

        if (!isLocationFetchInProgress)
        {
            editEvent();
        }
    }

    private void editEvent()
    {
        DateTime startTime = null;
        DateTime endTime = null;
        if (isTimeUpdated)
        {
            startTime = updatedStartTime;
            endTime = DateTimeUtil.getEndTime(startTime);
        }

        Location location = new Location();
        if (isLocationUpdated)
        {
            location = updatedLocation;
        }

        String description = originalDescription;
        if (isDescriptionUpdated)
        {
            description = updatedDescription;
        }

        Subscription subscription = eventService
                ._editEvent(originalEvent.getId(), startTime, endTime, location, description)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>()
                {
                    @Override
                    public void onCompleted()
                    {
                        isEditInitiated = false;
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Integer responseCode)
                    {
                        if (responseCode == 0)
                        {
                            view.navigateToDetailsScreen(originalEvent.getId());
                        }
                        else if (responseCode == -1)
                        {
                            view.displayError();
                        }
                        else if (responseCode == -2)
                        {
                            view.displayEventLockedError();
                        }
                    }
                });

        subscriptions.add(subscription);
    }
}
