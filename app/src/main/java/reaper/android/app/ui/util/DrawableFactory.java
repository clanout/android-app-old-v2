package reaper.android.app.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.HashMap;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.root.Reaper;

/**
 * Created by harsh on 24/09/15.
 */
public class DrawableFactory {
    private static Drawable generalDrawable, eatOutDrawable, drinksDrawable, cafeDrawable, moviesDrawable, outdorsDrawable, partyDrawable, localEventsDrawable, shoppingDrawable;

    private static Map<EventCategory, MaterialDrawableBuilder.IconValue> iconMapping;
    private static Map<EventCategory, Integer> colorMapping;

    static {
        iconMapping = new HashMap<>();
        iconMapping.put(EventCategory.GENERAL, MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
        iconMapping.put(EventCategory.EAT_OUT, MaterialDrawableBuilder.IconValue.FOOD);
        iconMapping.put(EventCategory.DRINKS, MaterialDrawableBuilder.IconValue.MARTINI);
        iconMapping.put(EventCategory.CAFE, MaterialDrawableBuilder.IconValue.COFFEE);
        iconMapping.put(EventCategory.MOVIES, MaterialDrawableBuilder.IconValue.MOVIE);
        iconMapping.put(EventCategory.OUTDOORS, MaterialDrawableBuilder.IconValue.TENNIS);
        iconMapping.put(EventCategory.PARTY, MaterialDrawableBuilder.IconValue.GIFT);
        iconMapping
                .put(EventCategory.LOCAL_EVENTS, MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
        iconMapping.put(EventCategory.SHOPPING, MaterialDrawableBuilder.IconValue.SHOPPING);
    }

    static {
        colorMapping = new HashMap<>();
        colorMapping.put(EventCategory.GENERAL, R.color.general);
        colorMapping.put(EventCategory.EAT_OUT, R.color.eat_out);
        colorMapping.put(EventCategory.DRINKS, R.color.drinks);
        colorMapping.put(EventCategory.CAFE, R.color.cafe);
        colorMapping.put(EventCategory.MOVIES, R.color.movies);
        colorMapping.put(EventCategory.OUTDOORS, R.color.outdoors);
        colorMapping.put(EventCategory.PARTY, R.color.party);
        colorMapping.put(EventCategory.LOCAL_EVENTS, R.color.local_events);
        colorMapping.put(EventCategory.SHOPPING, R.color.shopping);
    }


    public static Drawable get(EventCategory eventCategory, int size, int color) {
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
}