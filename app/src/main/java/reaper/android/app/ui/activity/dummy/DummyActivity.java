package reaper.android.app.ui.activity.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import reaper.android.R;

public class DummyActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_create);
    }
}
