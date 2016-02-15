package reaper.android.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import reaper.android.R;

public class NoInternetActivity extends AppCompatActivity
{
    Button btnRetry;

    public static Intent callingIntent(Context context)
    {
        Intent intent = new Intent(context, NoInternetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        btnRetry = (Button) findViewById(R.id.btnRetry);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        btnRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnRetry.setEnabled(false);

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if ((netInfo != null && netInfo.isConnected()))
                {
                    finish();
                }
                else
                {
                    btnRetry.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
    }
}
