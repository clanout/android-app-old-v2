package reaper.android.app.ui.screens.friends.mvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.model.Friend;
import reaper.android.app.model.FriendsComparator;
import reaper.android.app.service.UserService;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import timber.log.Timber;

public class FriendsPresenterImpl implements FriendsPresenter
{
    private FriendsView view;
    private UserService userService;

    private List<String> blockUpdates;
    private List<String> unblockUpdates;

    public FriendsPresenterImpl(UserService userService)
    {
        this.userService = userService;

        blockUpdates = new ArrayList<>();
        unblockUpdates = new ArrayList<>();
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
                Timber.v("Removed from unblock list : " + friend.getName());
                unblockUpdates.remove(friendId);
            }
            else if (!blockUpdates.contains(friendId))
            {
                Timber.v("Added to block list : " + friend.getName());
                blockUpdates.add(friendId);
            }
        }
        else
        {
            if (blockUpdates.contains(friendId))
            {
                Timber.v("Removed from block list : " + friend.getName());
                blockUpdates.remove(friendId);
            }
            else if (!unblockUpdates.contains(friendId))
            {
                Timber.v("Added to unblock list : " + friend.getName());
                unblockUpdates.add(friendId);
            }
        }
    }

    private void fetchAllFriends()
    {
        view.showLoading();
        userService
                .getAllFriends()
                .map(new Func1<List<Friend>, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(List<Friend> friends)
                    {
                        Collections.sort(friends, new FriendsComparator());
                        return friends;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Friend>>()
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
                    public void onNext(List<Friend> friends)
                    {
                        view.displayFriends(friends);
                    }
                });
    }
}
