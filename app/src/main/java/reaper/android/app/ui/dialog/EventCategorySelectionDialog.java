package reaper.android.app.ui.dialog;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.DrawableFactory;

public class EventCategorySelectionDialog
{
    public interface Listener
    {
        void onCategorySelected(EventCategory category);
    }

    public static void show(Activity activity, final Listener listener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_event_category, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final LinearLayout cafe = (LinearLayout) dialogView.findViewById(R.id.llCafe);
        final LinearLayout movies = (LinearLayout) dialogView.findViewById(R.id.llMovie);
        final LinearLayout eatOut = (LinearLayout) dialogView.findViewById(R.id.llEatOut);
        final LinearLayout sports = (LinearLayout) dialogView.findViewById(R.id.llSports);
        final LinearLayout outdoors = (LinearLayout) dialogView.findViewById(R.id.llOutdoors);
        final LinearLayout indoors = (LinearLayout) dialogView.findViewById(R.id.llIndoors);
        final LinearLayout drinks = (LinearLayout) dialogView.findViewById(R.id.llDrinks);
        final LinearLayout shopping = (LinearLayout) dialogView.findViewById(R.id.llShopping);
        final LinearLayout general = (LinearLayout) dialogView.findViewById(R.id.llGeneral);

        cafe.setBackground(DrawableFactory.getIconBackground(EventCategory.CAFE));
        movies.setBackground(DrawableFactory.getIconBackground(EventCategory.MOVIES));
        eatOut.setBackground(DrawableFactory.getIconBackground(EventCategory.EAT_OUT));
        sports.setBackground(DrawableFactory.getIconBackground(EventCategory.SPORTS));
        outdoors.setBackground(DrawableFactory.getIconBackground(EventCategory.OUTDOORS));
        indoors.setBackground(DrawableFactory.getIconBackground(EventCategory.INDOORS));
        drinks.setBackground(DrawableFactory.getIconBackground(EventCategory.DRINKS));
        shopping.setBackground(DrawableFactory.getIconBackground(EventCategory.SHOPPING));
        general.setBackground(DrawableFactory.getIconBackground(EventCategory.GENERAL));

        cafe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.CAFE);
                alertDialog.dismiss();
            }
        });

        movies.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.MOVIES);
                alertDialog.dismiss();
            }
        });


        eatOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.EAT_OUT);
                alertDialog.dismiss();
            }
        });


        sports.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.SPORTS);
                alertDialog.dismiss();
            }
        });


        outdoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.OUTDOORS);
                alertDialog.dismiss();
            }
        });


        indoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.INDOORS);
                alertDialog.dismiss();
            }
        });


        drinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.DRINKS);
                alertDialog.dismiss();
            }
        });


        shopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.SHOPPING);
                alertDialog.dismiss();
            }
        });


        general.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.GENERAL);
                alertDialog.dismiss();

            }
        });

        alertDialog.show();
    }
}
