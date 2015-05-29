package reaper.android.app.trigger;

import reaper.android.app.config.ErrorCode;
import retrofit.RetrofitError;

public class GenericErrorTrigger
{
    private ErrorCode errorCode;
    private RetrofitError error;

    public GenericErrorTrigger(ErrorCode errorCode, RetrofitError error)
    {
        this.errorCode = errorCode;
        this.error = error;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public RetrofitError getError()
    {
        return error;
    }
}
