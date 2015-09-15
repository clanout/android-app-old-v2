package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.app.api.auth.AuthApi;
import reaper.android.app.api.auth.request.CreateNewSessionApiRequest;
import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import reaper.android.app.api.auth.response.CreateNewSessionApiResponse;
import reaper.android.app.api.core.ApiManager;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.NewSessionCreatedTrigger;
import reaper.android.app.trigger.user.SessionValidatedTrigger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    public void createNewSession(String firstName, String lastName, String gender, String email, String id, List<String> friendList)
    {
        if(firstName == null)
        {
            firstName = "";
        }

        if(lastName == null)
        {
            lastName = "";
        }

        if(gender == null)
        {
            gender = "unspecified";
        }

        if(email == null)
        {
            email = id;
        }

        CreateNewSessionApiRequest request = new CreateNewSessionApiRequest(firstName, lastName, gender, id, email, friendList);
        authApi.createNewSession(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CreateNewSessionApiResponse>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.NEW_SESSION_CREATION_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(CreateNewSessionApiResponse createNewSessionApiResponse)
                    {
                        bus.post(new NewSessionCreatedTrigger(createNewSessionApiResponse.getSessionId()));
                    }
                });
    }
}
