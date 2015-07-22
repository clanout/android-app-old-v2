package reaper.android.app.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import reaper.android.R;
import reaper.android.app.ui.screens.edit.EditEventFragment;
import reaper.android.app.ui.util.FragmentUtils;

public class DummyActivity extends AppCompatActivity
{
    private TextView networkState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        networkState = (TextView) findViewById(R.id.activity_main_natwork_state);

        StringBuilder stringBuilder = new StringBuilder();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo networkInfo:networkInfos)
        {
            String type = networkInfo.getTypeName();
            if(type.equalsIgnoreCase("WIFI") || type.equalsIgnoreCase("MOBILE"))
            {
                stringBuilder.append(type + " : " + networkInfo.getSubtypeName() + " ; ");
            }
        }

        networkState.setText(stringBuilder.toString());

    }
}
