package reaper.android.app.ui.screens.launch.mvp.bootstrap;

public interface BootstrapPresenter
{
    void attachView(BootstrapView view);

    void detachView();
}
