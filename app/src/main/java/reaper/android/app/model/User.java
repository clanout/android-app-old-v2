package reaper.android.app.model;

import reaper.android.app.api._core.GsonProvider;

public class User implements Model
{
    private String sessionId;
    private boolean isNewUser;

    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String mobileNumber;
    private String gender;
    private String coverPicUrl;

    public boolean isNewUser()
    {
        return isNewUser;
    }

    public void setNewUser(boolean newUser)
    {
        isNewUser = newUser;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getMobileNumber()
    {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber)
    {
        this.mobileNumber = mobileNumber;
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
        return String.format("%s %s", firstname, lastname);
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getCoverPicUrl()
    {
        return coverPicUrl;
    }

    public void setCoverPicUrl(String coverPicUrl)
    {
        this.coverPicUrl = coverPicUrl;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }
}
