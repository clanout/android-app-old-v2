package reaper.android.app.ui.screens.friends.mvp;

import java.util.List;

import reaper.android.app.model.Friend;

public interface FriendsView
{
    void showLoading();

    void displayNoFriendsMessage();

    void displayError();

    void displayFriends(List<Friend> friends);

    interface FriendListItem
    {
        void render(Friend friend);
    }
}
