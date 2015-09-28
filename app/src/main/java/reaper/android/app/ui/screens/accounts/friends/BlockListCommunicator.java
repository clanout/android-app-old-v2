package reaper.android.app.ui.screens.accounts.friends;

import java.io.Serializable;

public interface BlockListCommunicator extends Serializable
{
    public void toggleBlock(String id, boolean isNowBlocked);
}
