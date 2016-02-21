package reaper.android.app.service;

import android.util.Pair;

import java.util.List;

import reaper.android.app.api.auth.AuthApi;
import reaper.android.app.api.auth.request.CreateNewSessionApiRequest;
import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import reaper.android.app.api.auth.response.CreateNewSessionApiResponse;
import reaper.android.app.api.core.ApiManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.model.User;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AuthService_
{
    private AuthApi authApi;
    private FacebookService_ facebookService;
    private GenericCache genericCache;

    public AuthService_(FacebookService_ facebookService, GenericCache genericCache)
    {
        this.facebookService = facebookService;
        this.genericCache = genericCache;
        authApi = ApiManager.getInstance().getApi(AuthApi.class);
    }

    public Observable<Boolean> initSession()
    {
        return validateSession()
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isSessionValid)
                    {
                        if (isSessionValid)
                        {
                            return Observable.just(true);
                        }
                        else
                        {
                            return createSession();
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Helper Methods */
    private Observable<Boolean> validateSession()
    {
        final String sessionId = genericCache.get(CacheKeys.SESSION_ID);
        if (sessionId == null)
        {
            return Observable.just(false);
        }
        else
        {
            ValidateSessionApiRequest request = new ValidateSessionApiRequest(sessionId);
            return authApi
                    .validateSession(request)
                    .map(new Func1<Response, Boolean>()
                    {
                        @Override
                        public Boolean call(Response response)
                        {
                            Timber.v("[Session Valid] Session ID = " + sessionId);
                            return true;
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, Boolean>()
                    {
                        @Override
                        public Boolean call(Throwable e)
                        {
                            Timber.e("[Session Invalid]");
                            return false;
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    private Observable<Boolean> createSession()
    {
        return Observable
                .zip(facebookService.getUser(), facebookService
                        .getFriends(), new Func2<User, List<String>, Pair<User, List<String>>>()
                {
                    @Override
                    public Pair<User, List<String>> call(User user, List<String> friends)
                    {
                        genericCache.put(CacheKeys.USER, user);

                        return new Pair<>(user, friends);
                    }
                })
                .flatMap(new Func1<Pair<User, List<String>>, Observable<CreateNewSessionApiResponse>>()
                {
                    @Override
                    public Observable<CreateNewSessionApiResponse> call(Pair<User, List<String>> userData)
                    {
                        User user = userData.first;
                        List<String> friends = userData.second;

                        CreateNewSessionApiRequest request = new CreateNewSessionApiRequest(
                                user.getFirstname(),
                                user.getLastname(),
                                user.getGender(),
                                user.getId(),
                                user.getEmail(),
                                friends);

                        return authApi.createNewSession(request);
                    }
                })
                .map(new Func1<CreateNewSessionApiResponse, String>()
                {
                    @Override
                    public String call(CreateNewSessionApiResponse response)
                    {
                        return response.getSessionId();
                    }
                })
                .onErrorReturn(new Func1<Throwable, String>()
                {
                    @Override
                    public String call(Throwable e)
                    {
                        Timber.e("[Unable to create session] " + e.getMessage());
                        return null;
                    }
                })
                .map(new Func1<String, Boolean>()
                {
                    @Override
                    public Boolean call(String sessionId)
                    {
                        if (sessionId == null)
                        {
                            // Delete User from cache
                            genericCache.delete(CacheKeys.USER);

                            return false;
                        }
                        else
                        {
                            Timber.v("[New Session Created] Session ID = " + sessionId);
                            genericCache.put(CacheKeys.SESSION_ID, sessionId);
                            return true;
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}
