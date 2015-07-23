package reaper.android.app.trigger.user;

import java.util.List;

import reaper.android.app.model.Friend;

public class PhoneContactsFetchedTrigger
{
    private List<Friend> phoneContacts;

    public PhoneContactsFetchedTrigger(List<Friend> phoneContacts)
    {
        this.phoneContacts = phoneContacts;
    }

    public List<Friend> getPhoneContacts()
    {
        return phoneContacts;
    }
}
