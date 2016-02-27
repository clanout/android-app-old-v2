package reaper.android.app.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import reaper.android.app.config.Dimensions;
import reaper.android.app.ui.util.DrawableFactory;

public class CreateEventModel implements Model
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
        return DrawableFactory.get(category, Dimensions.EVENT_ICON_SIZE);
    }

    public Drawable getIconBackground(Context context)
    {
        return DrawableFactory.getIconBackground(category);
    }
}
