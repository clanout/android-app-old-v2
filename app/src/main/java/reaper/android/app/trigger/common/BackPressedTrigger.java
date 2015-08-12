package reaper.android.app.trigger.common;

public class BackPressedTrigger
{
    private String activeFragment;

    public BackPressedTrigger(String activeFragment)
    {
        this.activeFragment = activeFragment;
    }

    public String getActiveFragment()
    {
        return activeFragment;
    }
}
