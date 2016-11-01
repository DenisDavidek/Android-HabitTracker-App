package net.samclarke.android.habittracker.ui.pickers;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.GridLayout;

import java.util.Calendar;

public class MonthDaysPicker extends GridLayout {
    private final static int DAYS_IN_MONTH = 31;
    private final static String STATE_SUPER = "base_state";
    private final static String STATE_SELECTION = "selection";
    private final CheckBox[] dayViews = new CheckBox[DAYS_IN_MONTH + 1];

    public MonthDaysPicker(Context context) {
        this(context, null);
    }

    public MonthDaysPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthDaysPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setColumnCount(7);
        setRowCount(5);

        for (int i = 0; i < DAYS_IN_MONTH; i++) {
            dayViews[i + 1] = createDayView(String.valueOf(i + 1));
            addView(dayViews[i + 1], new GridLayout.LayoutParams(GridLayout.spec(i / 7), GridLayout.spec(i % 7)));
        }

        dayViews[Calendar.getInstance().get(Calendar.DAY_OF_MONTH)].setChecked(true);
    }



    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putInt(STATE_SELECTION, getSelectedDaysBitmask());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            setSelectedDaysBitmask(bundle.getInt(STATE_SELECTION));
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    CheckBox createDayView(String day) {
        CheckBox view = new DayButton(getContext());

        view.setText(day);

//        view.setTag(TAG_WEEK_DAY, weekDay);
//        view.setTag(TAG_MONTH_DAY, monthDay);

        return view;
    }

    public int getSelectedDaysBitmask() {
        int bitmask = 0;

//        for (int i = 0; i < getChildCount(); i++){
//            View view = getChildAt(i);
//
//            if (view.getTag(TAG_MONTH_DAY) != null) {
//                if (((CheckBox)view).isChecked()) {
//                    int dayOfMonth = (int) view.getTag(TAG_MONTH_DAY);
//
//                    add to array list?
//                }
//            }
//        }

        for (int i = 1; i < dayViews.length; i++) {
            if (dayViews[i].isChecked()) {
                bitmask |= (1 << i);
            }
        }

        return bitmask;
    }

    public void setSelectedDaysBitmask(int bitmask) {
        for (int i = 1; i < dayViews.length; i++) {
            int dayFlag = (1 << i);

            dayViews[i].setChecked((bitmask & dayFlag) == dayFlag);
        }
    }
}
