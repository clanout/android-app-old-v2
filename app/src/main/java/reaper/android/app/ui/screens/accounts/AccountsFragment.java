package reaper.android.app.ui.screens.accounts;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import reaper.android.R;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.screens.accounts.friends.ManageFriendsFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class AccountsFragment extends Fragment implements AccountsAdapter.AccountsItemClickListener
{
    private TextView userName;
    private RecyclerView recyclerView;
    private ImageView userPic;

    private FragmentManager fragmentManager;
    private UserService userService;
    private Bus bus;

    private AccountsAdapter accountsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        userName = (TextView) view.findViewById(R.id.tv_account_fragment_user_name);
        userPic = (ImageView) view.findViewById(R.id.iv_account_fragment_user_pic);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_accounts_fragment);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();
        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);

        accountsAdapter = new AccountsAdapter(getActivity(), AccountsService.getmenuList());
        accountsAdapter.setAccountItemClickListener(this);
        recyclerView.setAdapter(accountsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
    }

    @Override
    public void onAccountItemClicked(View view, int position)
    {
        if (position == 0)
        {
            FragmentUtils.changeFragment(fragmentManager, new ManageFriendsFragment(), true);
        }
        else if (position == 1)
        {
            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled)
            {
                ComponentName componentName = new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, R.string.whatsapp_message);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getActivity(), R.string.whatsapp_not_installed, Toast.LENGTH_LONG).show();
            }
        }
        else if (position == 2)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_share_feedback, null);
            builder.setView(dialogView);

            final EditText commentMessage = (EditText) dialogView.findViewById(R.id.et_alert_dialog_share_feedback_comment);
            final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.rb_alert_dialog_share_feedback_rating);

            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String rating = String.valueOf(ratingBar.getRating());
                    String comment = commentMessage.getText().toString();
                    Boolean wantToCloseDialog = false;

                    if (rating == null || rating.isEmpty() || rating.equals("0.0"))
                    {
                        Toast.makeText(getActivity(), R.string.empty_rating, Toast.LENGTH_LONG).show();
                        wantToCloseDialog = false;
                    }
                    else
                    {
                        userService.shareFeedback(rating, comment);
                        Toast.makeText(getActivity(), R.string.feedback_submitted, Toast.LENGTH_LONG).show();
                        wantToCloseDialog = true;
                    }

                    if (wantToCloseDialog)
                    {
                        alertDialog.dismiss();
                    }
                }
            });
        }
        else if (position == 3)
        {

        }
    }
}
