package reaper.android.app.ui.screens.pending_invites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.pending_invites.mvp.PendingInvitesPresenter;
import reaper.android.app.ui.screens.pending_invites.mvp.PendingInvitesPresenterImpl;
import reaper.android.app.ui.screens.pending_invites.mvp.PendingInvitesView;
import reaper.android.app.ui.util.VisibilityAnimationUtil;

public class PendingInvitesFragment extends BaseFragment implements
        PendingInvitesView,
        PendingInviteAdapter.PendingInviteClickListener
{
    public static PendingInvitesFragment newInstance()
    {
        return new PendingInvitesFragment();
    }

    PendingInvitesScreen screen;

    PendingInvitesPresenter presenter;

    /* UI Elements */
    @Bind(R.id.etMobileNumber)
    EditText etMobileNumber;

    @Bind(R.id.tvInvalidPhoneError)
    TextView tvInvalidPhoneError;

    @Bind(R.id.llLoading)
    View llLoading;

    @Bind(R.id.llNoPendingInvites)
    View llNoPendingInvites;

    @Bind(R.id.rvInvites)
    RecyclerView rvInvites;

    @Bind(R.id.llExpiredInvites)
    View llExpiredInvites;

    @Bind(R.id.tvExpiredInvites)
    TextView tvExpiredInvites;

    @Bind(R.id.llBottomBar)
    View llBottomBar;

    @Bind(R.id.btnFetch)
    Button btnFetch;

    @Bind(R.id.btnSkip)
    Button btnSkip;

    @Bind(R.id.btnHome)
    Button btnHome;

    TextWatcher mobileErrorMessage;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        UserService userService = UserService.getInstance();
        EventService eventService = EventService.getInstance();
        presenter = new PendingInvitesPresenterImpl(userService, eventService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pending_invites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (PendingInvitesScreen) getActivity();
        initRecyclerView();

        mobileErrorMessage = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    tvInvalidPhoneError.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();
        etMobileNumber.addTextChangedListener(mobileErrorMessage);
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        etMobileNumber.addTextChangedListener(null);
        presenter.detachView();
    }

    /* Listeners */
    @OnClick(R.id.btnHome)
    public void onHomeClicked()
    {
        if (presenter != null)
        {
            presenter.gotoHome();
        }
    }

    @OnClick(R.id.btnSkip)
    public void onSkipClicked()
    {
        if (presenter != null)
        {
            presenter.skip();
        }
    }

    @OnClick(R.id.btnFetch)
    public void onFetchClicked()
    {
        if (presenter != null)
        {
            presenter.fetchPendingInvites(etMobileNumber.getText().toString());
        }
    }

    @Override
    public void onPendingInviteClicked(Event event)
    {
        if(presenter != null)
        {
            presenter.selectInvite(event);
        }
    }

    /* View Methods */
    @Override
    public void displayInvalidMobileNumberMessage()
    {
        tvInvalidPhoneError.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading()
    {
        etMobileNumber.setEnabled(false);
        llLoading.setVisibility(View.VISIBLE);
        VisibilityAnimationUtil.collapse(llBottomBar, 200);
    }

    @Override
    public void hideLoading()
    {
        llLoading.setVisibility(View.GONE);

        btnFetch.setVisibility(View.GONE);
        btnSkip.setVisibility(View.GONE);
        btnHome.setVisibility(View.VISIBLE);
        VisibilityAnimationUtil.expand(llBottomBar, 200);
    }

    @Override
    public void displayNoActivePendingInvitationsMessage()
    {
        rvInvites.setVisibility(View.GONE);
        llNoPendingInvites.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayActivePendingInvitation(List<Event> events)
    {
        rvInvites.setAdapter(new PendingInviteAdapter(getActivity(), events, this));

        llNoPendingInvites.setVisibility(View.GONE);
        rvInvites.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayExpiredEventsMessage(int expiredEventsCount)
    {
        llExpiredInvites.setVisibility(View.VISIBLE);
        tvExpiredInvites
                .setText("You have " + expiredEventsCount + " invitations for expired plans");
    }

    @Override
    public void navigateToHome()
    {
        screen.navigateToHomeScreen();
    }

    @Override
    public void navigateToDetails(String eventId)
    {
        screen.navigateToDetailsScreen(eventId);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvInvites.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvInvites.setAdapter(new PendingInviteAdapter(getActivity(), new ArrayList<Event>(), this));
    }
}
