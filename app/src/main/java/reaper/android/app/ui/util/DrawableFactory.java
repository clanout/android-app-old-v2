package reaper.android.app.ui.util;

import android.graphics.drawable.Drawable;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.HashMap;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.root.Reaper;

/**
 * Created by harsh on 24/09/15.
 */
public class DrawableFactory
{
    private static Drawable generalDrawable, eatOutDrawable, drinksDrawable, cafeDrawable, moviesDrawable, outdorsDrawable, partyDrawable, localEventsDrawable, shoppingDrawable, increaseTimeDrawable, decreaseTimeDrawable, expandDrawable;

    private static Map<EventCategory, MaterialDrawableBuilder.IconValue> iconMapping;

    static
    {
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


    public static Drawable get(EventCategory eventCategory, int size, int color)
    {
        return MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                      .setIcon(iconMapping.get(eventCategory))
                                      .setColor(Reaper.getReaperContext()
                                                      .getResources()
                                                      .getColor(color))
                                      .setSizeDp(size)
                                      .build();
    }

    public static Drawable getIncreaseTimeDrawable()
    {
        if (increaseTimeDrawable == null)
        {

            increaseTimeDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                          .setIcon(MaterialDrawableBuilder.IconValue.ARROW_RIGHT_BOLD_CIRCLE)
                                                          .setColor(Reaper.getReaperContext()
                                                                          .getResources()
                                                                          .getColor(R.color.cyan))
                                                          .setSizeDp(18)
                                                          .build();

            return increaseTimeDrawable;
        }
        else
        {
            return increaseTimeDrawable;
        }
    }

    public static Drawable getDecreaseTimeDrawable()
    {
        if (decreaseTimeDrawable == null)
        {

            decreaseTimeDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                          .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT_BOLD_CIRCLE)
                                                          .setColor(Reaper.getReaperContext()
                                                                          .getResources()
                                                                          .getColor(R.color.cyan))
                                                          .setSizeDp(18)
                                                          .build();

            return decreaseTimeDrawable;
        }
        else
        {
            return decreaseTimeDrawable;
        }
    }

    public static Drawable getExpandDrawable()
    {
        if (expandDrawable == null)
        {

            expandDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.CHEVRON_DOWN)
                                                    .setColor(Reaper.getReaperContext()
                                                                    .getResources()
                                                                    .getColor(R.color.cyan))
                                                    .setSizeDp(24)
                                                    .build();

            return expandDrawable;
        }
        else
        {
            return expandDrawable;
        }
    }
}
