package reaper.android.app.ui.screens.invite.core;

import java.io.Serializable;
import java.util.List;

import reaper.android.app.model.EventDetails;

public interface InviteeListCommunicator extends Serializable
{
    public void manageFacebookFriends(String id);

    public void managePhoneContacts(String id);

}
