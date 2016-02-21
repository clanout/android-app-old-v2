package reaper.android.app.ui.screens.launch.mvp.fb_login;


import reaper.android.app.service.FacebookService_;

public class FacebookLoginPresenterImpl implements FacebookLoginPresenter
{
    private FacebookLoginView view;
    private FacebookService_ facebookService;

    public FacebookLoginPresenterImpl(FacebookService_ facebookService)
    {
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
            facebookService.logout();
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
        view.displayFacebookLoginButton();
    }

    @Override
    public void onFacebookLoginError()
    {
        view.displayFacebookLoginError();
    }
}
