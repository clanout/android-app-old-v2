package reaper.android.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import java.util.List;

import hotchemi.stringpicker.StringPicker;
import reaper.android.R;

public class DayPickerDialog
{
    public interface Listener
    {
        void onDaySelected(int position);
    }

    public static void show(Activity activity, List<String> days, int selectedDayPosition, final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_day_picker, null);
        builder.setView(dialogView);

        final StringPicker stringPicker = (StringPicker) dialogView.findViewById(R.id.dayPicker);
        stringPicker.setValues(days);
        stringPicker.setCurrent(selectedDayPosition);


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                listener.onDaySelected(stringPicker.getCurrent());
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        /* Set Width */
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int width = (int) (displayRectangle.width() * 0.80f);
        alertDialog.getWindow().setLayout(width, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
    }
}
