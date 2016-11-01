package net.samclarke.android.habittracker.util;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract;

public class MaterialCalendarUtils {
    public interface DayStatusCallback {
        int getDayStatus(CalendarDay day);
    }

    public static DayViewDecorator[] getStatusDecorators(Context context,
                                                         DayStatusCallback dayStatusCallback) {

        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.WHITE);

        return new DayViewDecorator[] {
                createStatusDecorator(HabitsContract.CheckInEntry.STATUS_COMPLETE, dayStatusCallback,
                        ContextCompat.getDrawable(context, R.drawable.day_done), colorSpan),

                createStatusDecorator(HabitsContract.CheckInEntry.STATUS_SKIPPED, dayStatusCallback,
                        ContextCompat.getDrawable(context, R.drawable.day_skipped), colorSpan),

                createStatusDecorator(HabitsContract.CheckInEntry.STATUS_FAILED, dayStatusCallback,
                        ContextCompat.getDrawable(context, R.drawable.day_failed), colorSpan)
        };
    }

    private static DayViewDecorator createStatusDecorator(final int status,
                                                          final DayStatusCallback dayStatusCallback,
                                                          final Drawable background,
                                                          final ForegroundColorSpan colorSpan) {
        return new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return dayStatusCallback.getDayStatus(day) == status;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(background);
                view.addSpan(colorSpan);
            }
        };
    }
}
