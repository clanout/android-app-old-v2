package reaper.android.app.model;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by harsh on 21/09/15.
 */
public class EventSuggestion implements Serializable{

    private String title;
    private String category;
    private DateTime suggestedDateTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public DateTime getSuggestedDateTime() {
        return suggestedDateTime;
    }

    public void setSuggestedDateTime(DateTime suggestedDateTime) {
        this.suggestedDateTime = suggestedDateTime;
    }
}
