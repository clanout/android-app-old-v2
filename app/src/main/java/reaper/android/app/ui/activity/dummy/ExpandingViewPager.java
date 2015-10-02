package reaper.android.app.ui.activity.dummy;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ExpandingViewPager extends ViewPager
{
    private boolean isEditMode;

    public ExpandingViewPager(Context context)
    {
        super(context);
    }

    public ExpandingViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec
                    .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }

        int maxHeight = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources()
                        .getDisplayMetrics());
        int height = getChildAt(0).getMeasuredHeight();

        if (height > maxHeight)
        {
            height = maxHeight;
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setEditMode(boolean isEditMode)
    {
        this.isEditMode = isEditMode;
    }
}
