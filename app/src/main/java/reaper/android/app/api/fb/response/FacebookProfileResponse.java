package reaper.android.app.api.fb.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FacebookProfileResponse
{
    @SerializedName("id")
    private String id;

    @SerializedName("first_name")
    private String firstname;

    @SerializedName("last_name")
    private String lastname;

    @SerializedName("gender")
    private String gender;

    @SerializedName("email")
    private String email;

    public String getId()
    {
        return id;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public String getGender()
    {
        return gender;
    }

    public String getEmail()
    {
        return email;
    }
}
