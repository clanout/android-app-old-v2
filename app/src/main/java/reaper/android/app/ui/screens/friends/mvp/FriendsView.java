package reaper.android.app.ui.screens.friends.mvp;

import java.util.List;

import reaper.android.app.model.Friend;

public interface FriendsView
{
    void showLoading();

    void displayNoFriendsMessage();

    void displayError();

    void displayFriends(List<Friend> localFriends, List<Friend> otherFriends, String locationZone);

    interface FriendListItem
    {
        void render(Friend friend);
    }
}
