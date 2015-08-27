package reaper.android.app.trigger.facebook;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FacebookProfileFetchedTrigger
{
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;

    public FacebookProfileFetchedTrigger(String id, String firstName, String lastName, String gender, String email)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
    }

    public String getId()
    {
        return id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
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
