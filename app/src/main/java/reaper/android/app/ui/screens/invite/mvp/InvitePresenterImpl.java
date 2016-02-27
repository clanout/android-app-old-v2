package reaper.android.app.ui.screens.invite.mvp;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Friend;
import reaper.android.app.model.PhonebookContact;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.service._new.PhonebookService_;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class InvitePresenterImpl implements InvitePresenter
{
    private InviteView view;
    private UserService userService;
    private EventService eventService;
    private PhonebookService_ phonebookService;
    private LocationService_ locationService;

    private String eventId;
    private boolean isReadContactsPermissionGranted;

    private List<String> invitedFriends;
    private List<String> invitedContacts;

    private CompositeSubscription subscriptions;

    public InvitePresenterImpl(UserService userService, EventService eventService,
                               PhonebookService_ phonebookService, LocationService_ locationService, String eventId)
    {
        this.userService = userService;
        this.eventService = eventService;
        this.phonebookService = phonebookService;
        this.locationService = locationService;
        this.eventId = eventId;

        invitedFriends = new ArrayList<>();
        invitedContacts = new ArrayList<>();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(InviteView view)
    {
        this.view = view;

        isReadContactsPermissionGranted = phonebookService.isReadContactsPermissionGranted();
        if (!isReadContactsPermissionGranted)
        {
            this.view.handleReadContactsPermission();
        }

        if (userService.getSessionUser().getMobileNumber() == null)
        {
            this.view.showAddPhoneOption();
        }
        else
        {
            this.view.hideAddPhoneOption();
        }

        init();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void retry()
    {
        init();
    }

    @Override
    public void select(FriendInviteWrapper friend, boolean isInvited)
    {
        friend.setSelected(isInvited);

        String friendId = friend.getFriend().getId();
        if (isInvited && !invitedContacts.contains(friendId))
        {
            invitedFriends.add(friendId);
        }
        else if (!isInvited && invitedFriends.contains(friendId))
        {
            invitedFriends.remove(friendId);
        }

        int inviteCount = invitedFriends.size() + invitedContacts.size();
        if (inviteCount == 0)
        {
            view.hideInviteButton();
        }
        else
        {
            view.showInviteButton(inviteCount);
        }
    }

    @Override
    public void select(PhonebookContactInviteWrapper contact, boolean isInvited)
    {
        contact.setSelected(isInvited);

        String mobileNumber = contact.getPhonebookContact().getPhone();
        if (isInvited && !invitedContacts.contains(mobileNumber))
        {
            invitedContacts.add(mobileNumber);
        }
        else if (!isInvited && invitedContacts.contains(mobileNumber))
        {
            invitedContacts.remove(mobileNumber);
        }

        int inviteCount = invitedFriends.size() + invitedContacts.size();
        if (inviteCount == 0)
        {
            view.hideInviteButton();
        }
        else
        {
            view.showInviteButton(inviteCount);
        }
    }

    @Override
    public void sendInvitations()
    {
        if (view != null)
        {
            view.navigateToDetailsScreen();
        }

        if (!invitedFriends.isEmpty())
        {
            eventService._inviteAppFriends(eventId, invitedFriends);
        }

        if (!invitedContacts.isEmpty())
        {
            eventService._invitePhonebookContacts(eventId, invitedContacts);
        }
    }

    @Override
    public void refresh()
    {
        view.showRefreshing();

        Subscription subscription
                = getRefreshObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.hideRefreshing();
                    }

                    @Override
                    public void onNext(Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> inviteList)
                    {
                        List<FriendInviteWrapper> friends = inviteList.first;
                        List<PhonebookContactInviteWrapper> phonebookContacts = inviteList.second;
                        view.displayInviteList(locationService.getCurrentLocation().getZone(),
                                friends, phonebookContacts);
                        view.hideRefreshing();

                        invitedFriends = new ArrayList<String>();
                        invitedContacts = new ArrayList<String>();
                    }
                });

        subscriptions.add(subscription);
    }

    /* Helper Methods */
    private void init()
    {
        view.hideInviteButton();
        view.showLoading();

        Subscription subscription
                = getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        e.printStackTrace();
                        view.displayError();
                    }

                    @Override
                    public void onNext(Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> inviteList)
                    {
                        List<FriendInviteWrapper> friends = inviteList.first;
                        List<PhonebookContactInviteWrapper> phonebookContacts = inviteList.second;
                        view.displayInviteList(locationService.getCurrentLocation().getZone(),
                                friends, phonebookContacts);
                    }
                });

        subscriptions.add(subscription);
    }

    private Observable<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>> getObservable()
    {
        if (isReadContactsPermissionGranted)
        {
            return Observable
                    .zip(userService._fetchLocalAppFriends(),
                            eventService._fetchDetails(eventId),
                            phonebookService.fetchAllContacts(),
                            new Func3<List<Friend>, EventDetails, List<PhonebookContact>, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> appFriends, EventDetails eventDetails, List<PhonebookContact> phonebookContacts)
                                {
                                    List<FriendInviteWrapper> friendInviteWrappers = prepareFriendInviteWrappers(eventDetails, appFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = preparePhonebookContactInviteWrappers(phonebookContacts);
                                    return new Pair<>(friendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
        else
        {
            return Observable
                    .zip(userService._fetchLocalFacebookFriends(),
                            eventService._fetchDetails(eventId),
                            new Func2<List<Friend>, EventDetails, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> facebookFriends, EventDetails eventDetails)
                                {
                                    List<FriendInviteWrapper> facebookFriendInviteWrappers = prepareFriendInviteWrappers(eventDetails, facebookFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();
                                    return new Pair<>(facebookFriendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
    }

    private Observable<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>> getRefreshObservable()
    {
        if (isReadContactsPermissionGranted)
        {
            return Observable
                    .zip(userService._refreshLocalAppFriends(),
                            eventService._fetchDetails(eventId),
                            phonebookService.fetchAllContacts(),
                            new Func3<List<Friend>, EventDetails, List<PhonebookContact>, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> appFriends, EventDetails eventDetails, List<PhonebookContact> phonebookContacts)
                                {
                                    List<FriendInviteWrapper> friendInviteWrappers = prepareFriendInviteWrappers(eventDetails, appFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = preparePhonebookContactInviteWrappers(phonebookContacts);
                                    return new Pair<>(friendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
        else
        {
            return Observable
                    .zip(userService._fetchFacebookFriendsNetwork(false),
                            eventService._fetchDetails(eventId),
                            new Func2<List<Friend>, EventDetails, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> facebookFriends, EventDetails eventDetails)
                                {
                                    List<FriendInviteWrapper> facebookFriendInviteWrappers = prepareFriendInviteWrappers(eventDetails, facebookFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();
                                    return new Pair<>(facebookFriendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
    }

    private List<FriendInviteWrapper> prepareFriendInviteWrappers(EventDetails eventDetails, List<Friend> friends)
    {
        List<FriendInviteWrapper> friendInviteWrappers = new ArrayList<>();

        List<EventDetails.Attendee> attendees = eventDetails.getAttendees();
        List<EventDetails.Invitee> invitees = eventDetails.getInvitee();

        for (Friend friend : friends)
        {
            FriendInviteWrapper friendInviteWrapper = new FriendInviteWrapper();
            friendInviteWrapper.setFriend(friend);

            EventDetails.Attendee friendAttendee = new EventDetails.Attendee();
            friendAttendee.setId(friend.getId());
            if (attendees.contains(friendAttendee))
            {
                friendInviteWrapper.setGoing(true);
            }

            EventDetails.Invitee friendInvitee = new EventDetails.Invitee();
            friendInvitee.setId(friend.getId());
            if (invitees.contains(friendInvitee))
            {
                friendInviteWrapper.setAlreadyInvited(true);
            }

            friendInviteWrappers.add(friendInviteWrapper);
        }

        return friendInviteWrappers;
    }

    private List<PhonebookContactInviteWrapper> preparePhonebookContactInviteWrappers(List<PhonebookContact> contacts)
    {
        List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();

        for (PhonebookContact contact : contacts)
        {
            PhonebookContactInviteWrapper wrapper = new PhonebookContactInviteWrapper();
            wrapper.setPhonebookContact(contact);

            phonebookContactInviteWrappers.add(wrapper);
        }

        return phonebookContactInviteWrappers;
    }
}
