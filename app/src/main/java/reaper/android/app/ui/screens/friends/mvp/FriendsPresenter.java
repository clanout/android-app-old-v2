package reaper.android.app.ui.screens.friends.mvp;

import reaper.android.app.model.Friend;

public interface FriendsPresenter
{
    void attachView(FriendsView view);

    void detachView();

    void onBlockToggled(Friend friend, FriendsView.FriendListItem friendListItem);
}
