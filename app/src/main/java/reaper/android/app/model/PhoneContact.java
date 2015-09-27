package reaper.android.app.model;

import android.util.Log;

/**
 * Created by harsh on 25/09/15.
 */
public class PhoneContact {

    private String name;
    private String phone;
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int hashCode()
    {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof PhoneContact))
        {
            return false;
        }
        else
        {
            PhoneContact other = (PhoneContact) o;
            if (phone.equals(other.phone))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
