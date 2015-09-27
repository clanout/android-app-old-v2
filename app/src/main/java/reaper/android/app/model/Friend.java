package reaper.android.app.model;

import reaper.android.app.model.core.Model;

public class Friend implements Model
{
    private String id;
    private String name;
    private boolean isFavourite;
    private boolean isBlocked;
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isFavourite()
    {
        return isFavourite;
    }

    public void setFavourite(boolean isFavourite)
    {
        this.isFavourite = isFavourite;
    }

    public boolean isBlocked()
    {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked)
    {
        this.isBlocked = isBlocked;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof Friend))
        {
            return false;
        }
        else
        {
            Friend other = (Friend) o;
            if (id.equals(other.id))
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

