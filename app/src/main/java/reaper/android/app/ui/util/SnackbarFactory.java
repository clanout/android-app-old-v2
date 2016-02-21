package reaper.android.app.ui.util;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class SnackbarFactory
{
    public static Snackbar create(View view, String text)
    {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);

        View snackBarView = snackbar.getView();
        TextView tv = (TextView) snackBarView
                .findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        snackbar.show();
        return snackbar;
    }
}
