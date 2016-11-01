package net.samclarke.android.habittracker.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

public class ExpandingCalendarView extends MaterialCalendarView {
    private static final int DEFAULT_DAYS_IN_WEEK = 7;


    public ExpandingCalendarView(Context context) {
        super(context);
    }

    public ExpandingCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int desiredWidth = specWidthSize - getPaddingLeft() - getPaddingRight();
        final int desiredTileWidth = desiredWidth / DEFAULT_DAYS_IN_WEEK;

        setTileWidth(desiredTileWidth);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
