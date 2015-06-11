package reaper.android.app.service;

import com.squareup.otto.Bus;

import reaper.android.app.api.auth.AuthApi;
import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import reaper.android.app.api.core.ApiManager;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.GenericErrorTrigger;
import reaper.android.app.trigger.SessionValidatedTrigger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthService
{
    private Bus bus;
    private AuthApi authApi;

    public AuthService(Bus bus)
    {
        this.bus = bus;
        authApi = ApiManager.getInstance().getApi(AuthApi.class);
    }

    public void validateSession(final String sessionCookie)
    {
        ValidateSessionApiRequest request = new ValidateSessionApiRequest(sessionCookie);
        authApi.validateSession(request, new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {
                bus.post(new SessionValidatedTrigger(sessionCookie));
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.INVALID_SESSION, error));
            }
        });
    }
}
