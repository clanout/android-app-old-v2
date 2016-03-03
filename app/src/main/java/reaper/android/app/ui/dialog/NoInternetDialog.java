package reaper.android.app.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import reaper.android.R;
import reaper.android.app.root.Reaper;

public class NoInternetDialog
{
    public static void show(Activity activity)
    {
        final BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setCanceledOnTouchOutside(true);

        View view = LayoutInflater.from(activity)
                                  .inflate(R.layout.dialog_no_internet, (ViewGroup) activity
                                          .findViewById(android.R.id.content), false);
        dialog.setContentView(view);

        Button btnRetry = (Button) view.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ConnectivityManager cm = (ConnectivityManager) Reaper.getReaperContext()
                                                                     .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo != null && netInfo.isConnected())
                {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }
}
