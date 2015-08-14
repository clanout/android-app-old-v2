package reaper.android.app.trigger.common;

import reaper.android.app.config.ErrorCode;
import retrofit.RetrofitError;

public class GenericErrorTrigger
{
    private ErrorCode errorCode;
    private Exception error;

    public GenericErrorTrigger(ErrorCode errorCode, Exception error)
    {
        this.errorCode = errorCode;
        this.error = error;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public Exception getError()
    {
        return error;
    }
}
