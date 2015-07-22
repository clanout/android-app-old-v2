package reaper.android.app.ui.screens.invite;

import java.io.Serializable;

public interface InviteeListCommunicator extends Serializable
{
    public void manageFacebookFriends(String id);

    public void managePhoneContacts(String id);
}
