package reaper.android.app.ui.screens.accounts;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import reaper.android.R;
import reaper.android.app.service.AccountsService;

public class AccountsFragment extends Fragment implements AccountsAdapter.AccountsItemClickListener
{
    private TextView userName;
    private RecyclerView recyclerView;
    private ImageView userPic;

    private FragmentManager fragmentManager;

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
    }

    @Override
    public void onAccountItemClicked(View view, int position)
    {
        if (position == 0)
        {

        }
        else if (position == 1)
        {
            boolean isWhatsappInstalled = AccountsService.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager());
            if (isWhatsappInstalled) {
                ComponentName componentName = new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "This is a test message.");
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Whatsapp is not installed on this device.", Toast.LENGTH_LONG).show();
            }
        }
        else if (position == 2)
        {

        }
    }
}
