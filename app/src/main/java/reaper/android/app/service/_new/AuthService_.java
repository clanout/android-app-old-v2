package reaper.android.app.service._new;

import android.util.Pair;

import java.util.List;

import reaper.android.app.api.auth.AuthApi;
import reaper.android.app.api.auth.request.CreateNewSessionApiRequest;
import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import reaper.android.app.api.auth.response.CreateNewSessionApiResponse;
import reaper.android.app.api.core.ApiManager;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.model.User;
import reaper.android.app.service.UserService;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AuthService_
{
    private static AuthService_ instance;

    public static void init(FacebookService_ facebookService, UserService userService)
    {
        instance = new AuthService_(facebookService, userService);
    }

    public static AuthService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[AuthService Not Initialized]");
        }

        return instance;
    }

    private AuthApi authApi;
    private FacebookService_ facebookService;
    private UserService userService;

    private AuthService_(FacebookService_ facebookService, UserService userService)
    {
        this.facebookService = facebookService;
        this.userService = userService;

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

    public void logout()
    {
        facebookService.logout();
        CacheManager.clearAllCaches();
    }

    /* Helper Methods */
    private Observable<Boolean> validateSession()
    {
        final String sessionId = userService.getSessionId();
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
                .zip(facebookService.getFriends(), facebookService
                        .getCoverPicUrl(), new Func2<List<String>, String, Pair<List<String>, String>>()
                {
                    @Override
                    public Pair<List<String>, String> call(List<String> friends, String coverPicUrl)
                    {
                        return new Pair<>(friends, coverPicUrl);
                    }
                })
                .flatMap(new Func1<Pair<List<String>, String>, Observable<Pair<CreateNewSessionApiResponse, String>>>()
                {
                    @Override
                    public Observable<Pair<CreateNewSessionApiResponse, String>> call(Pair<List<String>, String> facebookData)
                    {
                        List<String> friends = facebookData.first;
                        final String coverPicUrl = facebookData.second;
                        String accessToken = facebookService.getAccessToken();

                        CreateNewSessionApiRequest request = new CreateNewSessionApiRequest(accessToken, friends);
                        return authApi
                                .createNewSession(request)
                                .map(new Func1<CreateNewSessionApiResponse, Pair<CreateNewSessionApiResponse, String>>()
                                {
                                    @Override
                                    public Pair<CreateNewSessionApiResponse, String> call(CreateNewSessionApiResponse response)
                                    {
                                        return new Pair<>(response, coverPicUrl);
                                    }
                                });
                    }
                })
                .map(new Func1<Pair<CreateNewSessionApiResponse, String>, User>()
                {
                    @Override
                    public User call(Pair<CreateNewSessionApiResponse, String> userData)
                    {
                        CreateNewSessionApiResponse response = userData.first;
                        String coverPicUrl = userData.second;

                        User user = new User();
                        user.setSessionId(response.getSessionId());
                        user.setNewUser(response.isNewUser());
                        user.setId(response.getUserId());
                        user.setFirstname(response.getFirstname());
                        user.setLastname(response.getLastname());
                        user.setEmail(response.getEmail());
                        user.setMobileNumber(response.getMobileNumber());
                        user.setGender(response.getGender());

                        user.setProfilePicUrl(facebookService
                                .getProfilePicUrl(user.getId()));
                        user.setCoverPicUrl(coverPicUrl);

                        return user;
                    }
                })
                .onErrorReturn(new Func1<Throwable, User>()
                {
                    @Override
                    public User call(Throwable e)
                    {
                        Timber.e("[Unable to create session] " + e.getMessage());
                        return null;
                    }
                })
                .map(new Func1<User, Boolean>()
                {
                    @Override
                    public Boolean call(User user)
                    {
                        if (user == null)
                        {
                            // Clear all cache (session creation failed)
                            CacheManager.clearAllCaches();
                            return false;
                        }
                        else
                        {
                            Timber.v("[New Session Created] Session ID = " + user.getSessionId());
                            userService.setSessionUser(user);
                            return true;
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}
