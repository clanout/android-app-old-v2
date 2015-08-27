package reaper.android.app.api.auth.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

/**
 * Created by Aditya on 27-08-2015.
 */
public class CreateNewSessionApiRequest extends ApiRequest
{
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("gender")
    private String gender;

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    public CreateNewSessionApiRequest(String firstName, String lastName, String gender, String id, String email)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.id = id;
        this.email = email;
        this.sessionCookie = null;
    }
}
