package reaper.android.app.api.fb.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiResponse;

/**
 * Created by harsh on 02/10/15.
 */
public class FacebookCoverPicResponse extends ApiResponse {

    @SerializedName("cover")
    private Cover cover;

    public Cover getCover() {
        return cover;
    }

    public class Cover {

        @SerializedName("source")
        private String source;

        public String getSource() {
            return source;
        }
    }
}
