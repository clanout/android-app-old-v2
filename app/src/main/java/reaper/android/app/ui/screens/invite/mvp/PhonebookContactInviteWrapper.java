package reaper.android.app.ui.screens.invite.mvp;

import reaper.android.app.model.PhonebookContact;

public class PhonebookContactInviteWrapper
{
    private PhonebookContact phonebookContact;
    private boolean isSelected;

    public PhonebookContact getPhonebookContact()
    {
        return phonebookContact;
    }

    public void setPhonebookContact(PhonebookContact phonebookContact)
    {
        this.phonebookContact = phonebookContact;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
