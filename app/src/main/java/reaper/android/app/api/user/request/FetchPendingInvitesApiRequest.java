package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by harsh on 25/09/15.
 */
public class FetchPendingInvitesApiRequest extends ApiRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("zone")
    private String zone;

    public FetchPendingInvitesApiRequest(String phone, String zone) {
        this.phone = phone;
        this.zone = zone;
    }
}
