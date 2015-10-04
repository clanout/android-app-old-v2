package reaper.android.app.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
            .asList(R.color.drinks, R.color.all_nighter, R.color.general, R.color.outdoors, R.color.eat_out, R.color.cafe, R.color.shopping, R.color.sports);

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
        colorMapping.put(EventCategory.GENERAL, R.color.general);
        colorMapping.put(EventCategory.EAT_OUT, R.color.eat_out);
        colorMapping.put(EventCategory.DRINKS, R.color.drinks);
        colorMapping.put(EventCategory.CAFE, R.color.cafe);
        colorMapping.put(EventCategory.MOVIES, R.color.movies);
        colorMapping.put(EventCategory.OUTDOORS, R.color.outdoors);
        colorMapping.put(EventCategory.INDOORS, R.color.all_nighter);
        colorMapping.put(EventCategory.SPORTS, R.color.sports);
        colorMapping.put(EventCategory.SHOPPING, R.color.shopping);
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
        drawable.setColor(Reaper.getReaperContext().getResources()
                                .getColor(colorResource));
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
        drawable.setColor(Reaper.getReaperContext().getResources()
                                .getColor(colors.get(random)));
        return drawable;
    }
}
