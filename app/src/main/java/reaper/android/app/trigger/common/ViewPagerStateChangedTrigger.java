package reaper.android.app.trigger.common;

/**
 * Created by Aditya on 19-08-2015.
 */
public class ViewPagerStateChangedTrigger {

    private int state;

    public ViewPagerStateChangedTrigger(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
