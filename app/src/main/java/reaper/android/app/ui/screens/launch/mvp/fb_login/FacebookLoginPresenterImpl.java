package reaper.android.app.ui.screens.launch.mvp.fb_login;


import reaper.android.app.service._new.AuthService_;
import reaper.android.app.service._new.FacebookService_;

public class FacebookLoginPresenterImpl implements FacebookLoginPresenter
{
    private FacebookLoginView view;
    private AuthService_ authService;
    private FacebookService_ facebookService;

    public FacebookLoginPresenterImpl(AuthService_ authService, FacebookService_ facebookService)
    {
        this.authService = authService;
        this.facebookService = facebookService;
    }

    @Override
    public void attachView(FacebookLoginView view)
    {
        this.view = view;

        if (facebookService.isAccessTokenValid())
        {
            this.view.proceedToSessionValidation();
        }
        else
        {
            authService.logout();
            this.view.displayFacebookLoginButton();
        }
    }

    @Override
    public void detachView()
    {
        view = null;
    }

    @Override
    public void onFacebookLoginSuccess()
    {
        if (view == null)
        {
            return;
        }

        if (facebookService.getAccessToken() == null)
        {
            view.displayFacebookLoginError();
        }
        else
        {
            if (facebookService.getDeclinedPermissions().size() > 0)
            {
                view.displayFacebookPermissionsMessage();
            }
            else
            {
                view.proceedToSessionValidation();
            }
        }
    }

    @Override
    public void onFacebookLoginCancel()
    {
        if (view == null)
        {
            return;
        }

        view.displayFacebookLoginButton();
    }

    @Override
    public void onFacebookLoginError()
    {
        if (view == null)
        {
            return;
        }

        view.displayFacebookLoginError();
    }
}
