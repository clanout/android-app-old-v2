package reaper.android.app.trigger.user;

import java.util.List;

import reaper.android.app.model.PhoneContact;

/**
 * Created by harsh on 26/09/15.
 */
public class AllPhoneContactsForSMSFetchedTrigger {

    List<PhoneContact> phoneContactList;

    public AllPhoneContactsForSMSFetchedTrigger(List<PhoneContact> phoneContactList) {
        this.phoneContactList = phoneContactList;
    }

    public List<PhoneContact> getPhoneContactList() {
        return phoneContactList;
    }
}
