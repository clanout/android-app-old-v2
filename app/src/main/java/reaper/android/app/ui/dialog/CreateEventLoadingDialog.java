package reaper.android.app.ui.dialog;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import reaper.android.R;
import reaper.android.app.ui.util.VisibilityAnimationUtil;

public class CreateEventLoadingDialog
{
    public interface Listener
    {
        void onInviteClicked();

        void onSkipClicked();
    }

    AlertDialog dialog;
    View llLoading;
    View llComplete;
    Button btnSkip;
    Button btnInvite;

    public CreateEventLoadingDialog(Activity activity, final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_create_progress, null);
        builder.setView(dialogView);

        llLoading = dialogView.findViewById(R.id.llLoading);
        llComplete = dialogView.findViewById(R.id.llComplete);
        btnSkip = (Button) dialogView.findViewById(R.id.btnSkip);
        btnInvite = (Button) dialogView.findViewById(R.id.btnInvite);

        dialog = builder.create();

        btnSkip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onSkipClicked();
                dialog.dismiss();
            }
        });

        btnInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onInviteClicked();
                dialog.dismiss();
            }
        });
    }

    public void showLoading()
    {
        llLoading.setVisibility(View.VISIBLE);
        llComplete.setVisibility(View.GONE);

        dialog.show();
    }

    public void showComplete()
    {
        llLoading.setVisibility(View.GONE);
        VisibilityAnimationUtil.expand(llComplete, 200);

        if (!dialog.isShowing())
        {
            dialog.show();
        }
    }

    public void dismiss()
    {
        if (dialog.isShowing())
        {
            dialog.dismiss();
        }
    }
}
