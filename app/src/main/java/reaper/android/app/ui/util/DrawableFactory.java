package reaper.android.app.ui.util;

import android.graphics.drawable.Drawable;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import reaper.android.R;
import reaper.android.app.root.Reaper;

/**
 * Created by harsh on 24/09/15.
 */
public class DrawableFactory
{

    private static Drawable generalDrawable, eatOutDrawable, drinksDrawable, cafeDrawable, moviesDrawable, outdorsDrawable, partyDrawable, localEventsDrawable, shoppingDrawable, increaseTimeDrawable, decreaseTimeDrawable, expandDrawable;

    public static Drawable getGeneralDrawable()
    {

        if (generalDrawable == null)
        {

            generalDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                     .setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD)
                                                     .setColor(Reaper.getReaperContext()
                                                                     .getResources()
                                                                     .getColor(R.color.accent))
                                                     .setSizeDp(48)
                                                     .build();

            return generalDrawable;
        }
        else
        {
            return generalDrawable;
        }
    }

    public static Drawable getEatOutDrawable()
    {
        if (eatOutDrawable == null)
        {

            eatOutDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.FOOD)
                                                    .setColor(Reaper.getReaperContext()
                                                                    .getResources()
                                                                    .getColor(R.color.accent))
                                                    .setSizeDp(48)
                                                    .build();

            return eatOutDrawable;
        }
        else
        {
            return eatOutDrawable;
        }
    }

    public static Drawable getDrinksDrawable()
    {
        if (drinksDrawable == null)
        {

            drinksDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.MARTINI)
                                                    .setColor(Reaper.getReaperContext()
                                                                    .getResources()
                                                                    .getColor(R.color.accent))
                                                    .setSizeDp(48)
                                                    .build();

            return drinksDrawable;
        }
        else
        {
            return drinksDrawable;
        }
    }

    public static Drawable getCafeDrawable()
    {
        if (cafeDrawable == null)
        {

            cafeDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                  .setIcon(MaterialDrawableBuilder.IconValue.COFFEE)
                                                  .setColor(Reaper.getReaperContext().getResources()
                                                                  .getColor(R.color.accent))
                                                  .setSizeDp(48)
                                                  .build();

            return cafeDrawable;
        }
        else
        {
            return cafeDrawable;
        }
    }

    public static Drawable getMoviesDrawable()
    {
        if (moviesDrawable == null)
        {

            moviesDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                    .setIcon(MaterialDrawableBuilder.IconValue.MOVIE)
                                                    .setColor(Reaper.getReaperContext()
                                                                    .getResources()
                                                                    .getColor(R.color.accent))
                                                    .setSizeDp(48)
                                                    .build();

            return moviesDrawable;
        }
        else
        {
            return moviesDrawable;
        }
    }

    public static Drawable getOutdorsDrawable()
    {
        if (outdorsDrawable == null)
        {

            outdorsDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                     .setIcon(MaterialDrawableBuilder.IconValue.TENNIS)
                                                     .setColor(Reaper.getReaperContext()
                                                                     .getResources()
                                                                     .getColor(R.color.accent))
                                                     .setSizeDp(48)
                                                     .build();

            return outdorsDrawable;
        }
        else
        {
            return outdorsDrawable;
        }
    }

    public static Drawable getPartyDrawable()
    {
        if (partyDrawable == null)
        {

            partyDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                   .setIcon(MaterialDrawableBuilder.IconValue.GIFT)
                                                   .setColor(Reaper.getReaperContext()
                                                                   .getResources()
                                                                   .getColor(R.color.accent))
                                                   .setSizeDp(48)
                                                   .build();

            return partyDrawable;
        }
        else
        {
            return partyDrawable;
        }
    }

    public static Drawable getLocalEventsDrawable()
    {
        if (localEventsDrawable == null)
        {

            localEventsDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                         .setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD)
                                                         .setColor(Reaper.getReaperContext()
                                                                         .getResources()
                                                                         .getColor(R.color.accent))
                                                         .setSizeDp(48)
                                                         .build();

            return localEventsDrawable;
        }
        else
        {
            return localEventsDrawable;
        }
    }

    public static Drawable getShoppingDrawable()
    {
        if (shoppingDrawable == null)
        {

            shoppingDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                                                      .setIcon(MaterialDrawableBuilder.IconValue.SHOPPING)
                                                      .setColor(Reaper.getReaperContext()
                                                                      .getResources()
                                                                      .getColor(R.color.accent))
                                                      .setSizeDp(48)
                                                      .build();

            return shoppingDrawable;
        }
        else
        {
            return shoppingDrawable;
        }
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
