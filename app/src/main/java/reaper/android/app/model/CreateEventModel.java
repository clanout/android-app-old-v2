package reaper.android.app.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import java.io.Serializable;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.root.Reaper;
import reaper.android.app.ui.util.DrawableFactory;

public class CreateEventModel implements Serializable
{
    private final EventCategory category;
    private final String title;

    public CreateEventModel(EventCategory category, String title)
    {
        this.category = category;
        this.title = title;
    }

    public EventCategory getCategory()
    {
        return category;
    }

    public String getTitle()
    {
        return title;
    }

    public Drawable getIcon()
    {
        return DrawableFactory.get(category, 48, R.color.white);
    }

    public Drawable getIconBackground(Context context)
    {
        return getIconBackground(context, R.color.accent, 4);
    }

    private Drawable getIconBackground(Context context, int colorResource, int cornerRadius)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, context.getResources()
                                                                                  .getDisplayMetrics()));
        drawable.setColor(ContextCompat.getColor(context, colorResource));
        return drawable;
    }
}
