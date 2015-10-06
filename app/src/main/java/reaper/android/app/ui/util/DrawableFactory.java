package reaper.android.app.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.root.Reaper;

/**
 * Created by harsh on 24/09/15.
 */
public class DrawableFactory
{
    private static Drawable generalDrawable, eatOutDrawable, drinksDrawable, cafeDrawable, moviesDrawable, outdorsDrawable, partyDrawable, localEventsDrawable, shoppingDrawable;

    private static Map<EventCategory, MaterialDrawableBuilder.IconValue> iconMapping;
    private static Map<EventCategory, Integer> colorMapping;
    private static List<Integer> colors = Arrays
            .asList(R.color.three, R.color.nine, R.color.two, R.color.five, R.color.one, R.color.four, R.color.seven, R.color.eight);

    static
    {
        iconMapping = new HashMap<>();
        iconMapping.put(EventCategory.GENERAL, MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
        iconMapping.put(EventCategory.EAT_OUT, MaterialDrawableBuilder.IconValue.FOOD);
        iconMapping.put(EventCategory.DRINKS, MaterialDrawableBuilder.IconValue.MARTINI);
        iconMapping.put(EventCategory.CAFE, MaterialDrawableBuilder.IconValue.COFFEE);
        iconMapping.put(EventCategory.MOVIES, MaterialDrawableBuilder.IconValue.THEATER);
        iconMapping.put(EventCategory.OUTDOORS, MaterialDrawableBuilder.IconValue.BIKE);
        iconMapping.put(EventCategory.SPORTS, MaterialDrawableBuilder.IconValue.TENNIS);
        iconMapping
                .put(EventCategory.INDOORS, MaterialDrawableBuilder.IconValue.XBOX_CONTROLLER);
        iconMapping.put(EventCategory.SHOPPING, MaterialDrawableBuilder.IconValue.SHOPPING);
    }

    static
    {
        colorMapping = new HashMap<>();
        colorMapping.put(EventCategory.GENERAL, R.color.two);
        colorMapping.put(EventCategory.EAT_OUT, R.color.one);
        colorMapping.put(EventCategory.DRINKS, R.color.three);
        colorMapping.put(EventCategory.CAFE, R.color.four);
        colorMapping.put(EventCategory.MOVIES, R.color.six);
        colorMapping.put(EventCategory.OUTDOORS, R.color.five);
        colorMapping.put(EventCategory.INDOORS, R.color.nine);
        colorMapping.put(EventCategory.SPORTS, R.color.eight);
        colorMapping.put(EventCategory.SHOPPING, R.color.seven);
    }


    public static Drawable get(EventCategory eventCategory, int size)
    {
        return MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                      .setIcon(iconMapping.get(eventCategory))
                                      .setColor(Color.WHITE)
                                      .setSizeDp(size)
                                      .build();
    }


    public static Drawable getIconBackground(Context context, int colorResource, int cornerRadius)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, context.getResources()
                                                                                  .getDisplayMetrics()));
        drawable.setColor(ContextCompat.getColor(Reaper.getReaperContext(), colorResource));
        return drawable;
    }

    public static Drawable randomIconBackground()
    {
        int random = (int) (Math.random() * colors.size());

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, Reaper.getReaperContext()
                                                                      .getResources()
                                                                      .getDisplayMetrics()));
        drawable.setColor(Reaper.getReaperContext().getResources().getColor(colors.get(random)));
        return drawable;
    }
}
