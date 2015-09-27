package reaper.android.app.trigger.user;

public class ManageAppFriendsTrigger
{
    private String id;
    private boolean isSelected;

    public ManageAppFriendsTrigger(String id, boolean isSelected) {
        this.id = id;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
