package reaper.android.app.ui.screens.friends.mvp;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.Friend;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class FriendsPresenterImpl implements FriendsPresenter
{
    private FriendsView view;
    private UserService userService;
    private LocationService_ locationService;

    private List<String> blockUpdates;
    private List<String> unblockUpdates;

    private CompositeSubscription subscriptions;

    public FriendsPresenterImpl(UserService userService, LocationService_ locationService)
    {
        this.userService = userService;
        this.locationService = locationService;

        blockUpdates = new ArrayList<>();
        unblockUpdates = new ArrayList<>();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(FriendsView view)
    {
        this.view = view;
        fetchAllFriends();
    }

    @Override
    public void detachView()
    {
        if (!blockUpdates.isEmpty() || !unblockUpdates.isEmpty())
        {
            userService.sendBlockRequests(blockUpdates, unblockUpdates);
        }

        subscriptions.clear();
        view = null;
    }

    @Override
    public void onBlockToggled(Friend friend, FriendsView.FriendListItem friendListItem)
    {
        friend.setBlocked(!friend.isBlocked());
        friendListItem.render(friend);

        String friendId = friend.getId();
        if (friend.isBlocked())
        {
            if (unblockUpdates.contains(friendId))
            {
                unblockUpdates.remove(friendId);
            }
            else if (!blockUpdates.contains(friendId))
            {
                blockUpdates.add(friendId);
            }
        }
        else
        {
            if (blockUpdates.contains(friendId))
            {
                blockUpdates.remove(friendId);
            }
            else if (!unblockUpdates.contains(friendId))
            {
                unblockUpdates.add(friendId);
            }
        }
    }

    private void fetchAllFriends()
    {
        view.showLoading();

        Subscription subscription =
                userService
                        ._fetchFacebookFriendsNetwork(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Pair<List<Friend>, List<Friend>>>()
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
                            public void onNext(Pair<List<Friend>, List<Friend>> allFriends)
                            {
                                List<Friend> localFriends = allFriends.first;
                                List<Friend> otherFriends = allFriends.second;

                                view.displayFriends(localFriends, otherFriends, locationService
                                        .getCurrentLocation().getZone());
                            }
                        });

        subscriptions.add(subscription);
    }
}
