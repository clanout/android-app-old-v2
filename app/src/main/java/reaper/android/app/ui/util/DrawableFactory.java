package reaper.android.app.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Arrays;
import java.util.Collections;
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
    private static List<Integer> colors = Arrays.asList(
            R.color.category_icon_one,
            R.color.category_icon_two,
            R.color.category_icon_three,
            R.color.category_icon_four,
            R.color.category_icon_five,
            R.color.category_icon_six,
            R.color.category_icon_seven,
            R.color.category_icon_eight,
            R.color.category_icon_nine);

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
        Collections.shuffle(colors);
        colorMapping = new HashMap<>();
        colorMapping.put(EventCategory.GENERAL, colors.get(0));
        colorMapping.put(EventCategory.EAT_OUT, colors.get(1));
        colorMapping.put(EventCategory.DRINKS, colors.get(2));
        colorMapping.put(EventCategory.CAFE, colors.get(3));
        colorMapping.put(EventCategory.MOVIES, colors.get(4));
        colorMapping.put(EventCategory.OUTDOORS, colors.get(5));
        colorMapping.put(EventCategory.INDOORS, colors.get(6));
        colorMapping.put(EventCategory.SPORTS, colors.get(7));
        colorMapping.put(EventCategory.SHOPPING, colors.get(8));
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
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint()
              .setColor(ContextCompat.getColor(Reaper.getReaperContext(), colorResource));
        return circle;

//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setCornerRadius(TypedValue
//                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, context.getResources()
//                                                                                  .getDisplayMetrics()));
//        drawable.setColor(ContextCompat.getColor(Reaper.getReaperContext(), colorResource));
//        return drawable;
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

    public static Drawable getIconBackground(EventCategory eventCategory)
    {
        int color = colorMapping.get(eventCategory);
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setCornerRadius(TypedValue
//                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, Reaper.getReaperContext()
//                                                                      .getResources()
//                                                                      .getDisplayMetrics()));
//        drawable.setColor(ContextCompat.getColor(Reaper.getReaperContext(), color));
//        return drawable;

        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint()
              .setColor(ContextCompat.getColor(Reaper.getReaperContext(), color));
        return circle;
    }
}
