package reaper.android.app.api.auth.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiResponse;

/**
 * Created by Aditya on 27-08-2015.
 */
public class CreateNewSessionApiResponse extends ApiResponse
{
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("is_new_user")
    private boolean isNewUser;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("first_name")
    private String firstname;

    @SerializedName("last_name")
    private String lastname;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String mobileNumber;

    @SerializedName("gender")
    private String gender;

    public String getSessionId()
    {
        return sessionId;
    }

    public boolean isNewUser()
    {
        return isNewUser;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public String getEmail()
    {
        return email;
    }

    public String getMobileNumber()
    {
        return mobileNumber;
    }

    public String getGender()
    {
        return gender;
    }
}
