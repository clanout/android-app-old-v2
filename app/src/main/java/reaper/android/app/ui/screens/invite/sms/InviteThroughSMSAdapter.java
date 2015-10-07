package reaper.android.app.ui.screens.invite.sms;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.PhoneContact;
import reaper.android.app.trigger.user.ManageSMSInviteeTrigger;

/**
 * Created by harsh on 26/09/15.
 */
public class InviteThroughSMSAdapter extends RecyclerView.Adapter<InviteThroughSMSAdapter.InviteThroughSMSViewHolder> {

    private Context context;
    private List<PhoneContact> phoneContactList;
    private Bus bus;

    public InviteThroughSMSAdapter(Context context, List<PhoneContact> phoneContactList, Bus bus) {
        this.context = context;
        this.phoneContactList = phoneContactList;
        this.bus = bus;
    }

    @Override
    public InviteThroughSMSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_invite_through_sms, parent, false);
        InviteThroughSMSViewHolder holder = new InviteThroughSMSViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InviteThroughSMSViewHolder holder, final int position) {

        final PhoneContact current = phoneContactList.get(position);

        holder.name.setText(current.getName());
        holder.phone.setText(current.getPhone());

        holder.checkBox.setChecked(current.isSelected());
    }

    @Override
    public int getItemCount() {
        return phoneContactList.size();
    }

    class InviteThroughSMSViewHolder extends RecyclerView.ViewHolder {

        private TextView name, phone;
        private CheckBox checkBox;

        public InviteThroughSMSViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tv_list_item_invite_through_sms_user_name);
            phone = (TextView) itemView.findViewById(R.id.tv_list_item_invite_through_sms_phone_number);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_list_item_invite_through_sms);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    phoneContactList.get(getAdapterPosition()).setIsSelected(isChecked);

                    bus.post(new ManageSMSInviteeTrigger(phoneContactList.get(getAdapterPosition()).getPhone(), isChecked));
                }

            });
        }
    }
}
