package reaper.android.app.trigger.user;

public class ManagePhoneContactsTrigger
{
    private String id;
    private boolean isSelected;

    public ManagePhoneContactsTrigger(String id, boolean isSelected) {
        this.id = id;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getId() {
        return id;
    }
}
