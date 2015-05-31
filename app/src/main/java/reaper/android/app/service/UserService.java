package reaper.android.app.service;

import com.squareup.otto.Bus;

public class UserService
{
    private Bus bus;

    public UserService(Bus bus)
    {
        this.bus = bus;
    }

    public String getActiveUser()
    {
        return "9320369679";
    }
}
