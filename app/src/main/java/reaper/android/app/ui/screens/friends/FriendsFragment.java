package reaper.android.app.ui.screens.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.model.Friend;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui._core.BaseFragment;
import reaper.android.app.ui.screens.friends.mvp.FriendsPresenter;
import reaper.android.app.ui.screens.friends.mvp.FriendsPresenterImpl;
import reaper.android.app.ui.screens.friends.mvp.FriendsView;

public class FriendsFragment extends BaseFragment implements
        FriendsView,
        FriendAdapter.BlockListener
{
    public static FriendsFragment newInstance()
    {
        return new FriendsFragment();
    }

    FriendsScreen screen;

    FriendsPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvFriends)
    RecyclerView rvFriends;

    @Bind(R.id.tvMessage)
    TextView tvMessage;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.etSearch)
    EditText etSearch;

    TextWatcher search;

    List<Friend> localFriends;
    List<Friend> otherFriends;
    String locationZone;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* User Service */
        UserService userService = UserService.getInstance();

        /* Location Service */
        LocationService_ locationService = LocationService_.getInstance();

        /* Presenter */
        presenter = new FriendsPresenterImpl(userService, locationService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        screen = (FriendsScreen) getActivity();
        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        etSearch.removeTextChangedListener(search);
        search = null;

        presenter.detachView();
    }

    /* Listeners */
    @Override
    public void onBlockToggled(Friend friend, FriendListItem friendListItem)
    {
        presenter.onBlockToggled(friend, friendListItem);
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        rvFriends.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public void displayNoFriendsMessage()
    {
        tvMessage.setText(R.string.no_facebook_friends);

        tvMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
    }

    @Override
    public void displayError()
    {
        tvMessage.setText(R.string.error_facebook_friends);

        tvMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
    }

    @Override
    public void displayFriends(List<Friend> localFriends, List<Friend> otherFriends, String locationZone)
    {
        this.localFriends = localFriends;
        this.otherFriends = otherFriends;
        this.locationZone = locationZone;

        initSearch();
        etSearch.addTextChangedListener(search);

        refreshRecyclerView(localFriends, otherFriends, locationZone);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshRecyclerView(new ArrayList<Friend>(), new ArrayList<Friend>(), null);
    }

    private void refreshRecyclerView(List<Friend> visibleLocalFriends, List<Friend> visibleOtherFriends, String locationZone)
    {
        rvFriends
                .setAdapter(new FriendAdapter(getActivity(), visibleLocalFriends, visibleOtherFriends, locationZone, this));

        rvFriends.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    private void initSearch()
    {
        search = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                List<Friend> visibleLocalFriends = new ArrayList<>();
                List<Friend> visibleOtherFriends = new ArrayList<>();

                if (s.length() >= 1)
                {
                    visibleLocalFriends = new ArrayList<>();
                    for (Friend friend : localFriends)
                    {
                        if (friend.getName().toLowerCase().contains(s.toString().toLowerCase()))
                        {
                            visibleLocalFriends.add(friend);
                        }
                    }

                    visibleOtherFriends = new ArrayList<>();
                    for (Friend friend : otherFriends)
                    {
                        if (friend.getName().toLowerCase().contains(s.toString().toLowerCase()))
                        {
                            visibleOtherFriends.add(friend);
                        }
                    }

                    if (visibleLocalFriends.isEmpty() && visibleOtherFriends.isEmpty())
                    {
                        displayNoFriendsMessage();
                    }
                    else
                    {
                        refreshRecyclerView(visibleLocalFriends, visibleOtherFriends, locationZone);
                    }
                }
                else if (s.length() == 0)
                {
                    refreshRecyclerView(localFriends, otherFriends, locationZone);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        };
    }
}
