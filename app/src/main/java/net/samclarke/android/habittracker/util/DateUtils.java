package net.samclarke.android.habittracker.util;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.samclarke.android.habittracker.provider.HabitsContract;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static int daysBetweenDates(Date dateA, Date dateB) {
        long diff = clearTime(dateB).getTime() - clearTime(dateA).getTime();

        return (int)TimeUnit.MILLISECONDS.toDays(diff);
    }

    public static Calendar getCalendarDate(CalendarDay date) {
        // CalendarDay removes the timezone causing getTimeInMillis()
        // to be wrong when the device timezone is not UTC / GMT so
        // create a new Calendar with the device timezone
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth(), date.getDay());

        clearTime(cal);

        return cal;
    }

    public static int getTimestamp(CalendarDay date) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(getCalendarDate(date).getTimeInMillis());
    }

    public static boolean isDateEnabled(Calendar date, int frequency, int frequencyValue) {
        switch (frequency) {
            case HabitsContract.HabitEntry.FREQUENCY_DAILY:
                return true;
            case HabitsContract.HabitEntry.FREQUENCY_WEEKLY:
                int dayOfWeekFlag = (1 << date.get(Calendar.DAY_OF_WEEK));

                return (frequencyValue & dayOfWeekFlag) == dayOfWeekFlag;
            case HabitsContract.HabitEntry.FREQUENCY_MONTHLY:
                int dayOfMonthFlag = (1 << date.get(Calendar.DAY_OF_MONTH));

                return (frequencyValue & dayOfMonthFlag) == dayOfMonthFlag;
            case HabitsContract.HabitEntry.FREQUENCY_INTERVAL:
                return daysBetweenDates(new Date(), date.getTime()) % frequencyValue == 1;
            default:
                throw new UnsupportedOperationException("Unknown habit frequency");
        }
    }

    public static boolean isSameDay(Calendar calA, Calendar calB) {
        return calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR) &&
                calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                calA.get(Calendar.ERA) == calB.get(Calendar.ERA);
    }

    private static void clearTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
    }

    public static Date clearTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        clearTime(cal);

        return cal.getTime();
    }

    public static int clearTime(int timestamp) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(TimeUnit.SECONDS.toMillis(timestamp));

        clearTime(date);

        return (int)TimeUnit.MILLISECONDS.toSeconds(date.getTimeInMillis());
    }
}
