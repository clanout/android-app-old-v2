package reaper.android.app.ui.screens.accounts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import reaper.android.R;


/**
 * Created by Aditya on 07-04-2015.
 */
public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>
{
    private LayoutInflater inflater;
    List<AccountsListItem> data = Collections.emptyList();
    private AccountsItemClickListener accountsItemClickListener;

    public AccountsAdapter(Context context, List<AccountsListItem> data)
    {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.list_item_accounts, parent, false);
        AccountViewHolder accountViewHolder = new AccountViewHolder(view);
        return accountViewHolder;
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position)
    {
        AccountsListItem current = data.get(position);
        holder.title.setText(current.title);
        holder.icon.setImageResource(current.icon);
    }

    public void setAccountItemClickListener(AccountsItemClickListener accountsItemClickListener){
        this.accountsItemClickListener = accountsItemClickListener;
    }
    
    @Override
    public int getItemCount()
    {
        return data.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView icon;
        TextView title;

        public AccountViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            icon = (ImageView) itemView.findViewById(R.id.iv_list_item_accounts);
            title = (TextView) itemView.findViewById(R.id.tv_list_item_accounts);
        }

        @Override
        public void onClick(View view)
        {
            if(accountsItemClickListener != null){
                accountsItemClickListener.onAccountItemClicked(view, getAdapterPosition());
            }
        }
    }

    public interface AccountsItemClickListener
    {
        public void onAccountItemClicked(View view, int position);
    }
}
