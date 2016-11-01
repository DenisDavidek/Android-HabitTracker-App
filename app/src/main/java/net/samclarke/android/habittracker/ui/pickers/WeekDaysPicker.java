package net.samclarke.android.habittracker.ui.pickers;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class WeekDaysPicker extends LinearLayout {
    private final static int DAYS_IN_WEEK = 7;
    private final static String STATE_SUPER = "base_state";
    private final static String STATE_SELECTION = "selection";
    private final CheckBox[] dayViews = new CheckBox[DAYS_IN_WEEK + 1];

    public WeekDaysPicker(Context context) {
        this(context, null);
    }

    public WeekDaysPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekDaysPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int dayNum = Calendar.getInstance().getFirstDayOfWeek();
        String[] dayNames = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();

        for (int i = 0; i < DAYS_IN_WEEK; i++, dayNum++) {
            if (dayNum > DAYS_IN_WEEK) {
                dayNum = 1;
            }

            dayViews[dayNum] = createDayView(dayNames[dayNum]);
            addView(dayViews[dayNum]);
        }

        dayViews[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)].setChecked(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putByte(STATE_SELECTION, getSelectedDaysBitmask());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            setSelectedDaysBitmask(bundle.getByte(STATE_SELECTION));
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    CheckBox createDayView(String day) {
        CheckBox view = new DayButton(getContext());

        view.setText(day);

        return view;
    }

    public byte getSelectedDaysBitmask() {
        byte bitmask = 0;

        for (int dayNum = 1; dayNum < dayViews.length; dayNum++) {
            if (dayViews[dayNum].isChecked()) {
                bitmask |= (1 << dayNum);
            }
        }

        return bitmask;
    }

    public void setSelectedDaysBitmask(byte bitmask) {
        for (int dayNum = 1; dayNum < dayViews.length; dayNum++) {
            int dayFlag = (1 << dayNum);

            dayViews[dayNum].setChecked((bitmask & dayFlag) == dayFlag);
        }
    }
}
