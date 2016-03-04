package reaper.android.app.model;

import android.graphics.drawable.Drawable;

import reaper.android.app.config.Dimensions;
import reaper.android.app.ui.util.CategoryIconFactory;

public class CreateEventSuggestion implements Model
{
    private final EventCategory category;
    private final String title;

    public CreateEventSuggestion(EventCategory category, String title)
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
        return CategoryIconFactory.get(category, Dimensions.EVENT_ICON_SIZE);
    }

    public Drawable getIconBackground()
    {
        return CategoryIconFactory.getIconBackground(category);
    }
}
