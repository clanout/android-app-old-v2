package reaper.android.app.trigger.user;

/**
 * Created by harsh on 26/09/15.
 */
public class ManageSMSInviteeTrigger {

    private String phone;
    private boolean isSelected;

    public ManageSMSInviteeTrigger(String phone, boolean isSelected) {
        this.phone = phone;
        this.isSelected = isSelected;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
