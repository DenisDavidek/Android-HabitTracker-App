package net.samclarke.android.habittracker.util;

import android.content.Context;
import android.database.Cursor;

import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;

public class CheckInUtils {
    public static int getLongestStreak(Context context, int habitId, int startDate) {
        final int startTimestamp = DateUtils.clearTime(startDate);
        final Cursor cursor = context.getContentResolver().query(
                CheckInEntry.CONTENT_URI,
                CheckInQuery.PROJECTION,
                CheckInQuery.SELECTION_WITH_START,
                new String[] { String.valueOf(habitId), String.valueOf(startTimestamp) },
                CheckInQuery.ORDER_BY_DATE
        );

        if (cursor == null) {
            return -1;
        }

        try {
            int currentStreak = 0;
            int longestStreak = 0;

            if (cursor.moveToFirst()) {
                do {
                    int status = cursor.getInt(CheckInQuery.COLUMN_STATUS);

                    if (status == CheckInEntry.STATUS_COMPLETE) {
                        currentStreak += 1;
                    } else if (status == CheckInEntry.STATUS_FAILED) {
                        longestStreak = Math.max(longestStreak, currentStreak);
                        currentStreak = 0;
                    }
                } while (cursor.moveToNext());
            }

            return Math.max(longestStreak, currentStreak);
        } finally {
            cursor.close();
        }
    }

    public static int currentStreak(Context context, int habitId) {
        final Cursor cursor = context.getContentResolver().query(
                CheckInEntry.CONTENT_URI,
                CheckInQuery.PROJECTION,
                CheckInQuery.SELECTION,
                new String[] { String.valueOf(habitId) },
                CheckInQuery.ORDER_BY_DATE
        );

        if (cursor == null) {
            return -1;
        }

        try {
            int streak = 0;

            if (cursor.moveToLast()) {
                do {
                    int status = cursor.getInt(CheckInQuery.COLUMN_STATUS);

                    if (status == CheckInEntry.STATUS_COMPLETE) {
                        streak += 1;
                    } else if (status == CheckInEntry.STATUS_FAILED) {
                        return streak;
                    }
                } while (cursor.moveToPrevious());
            }

            return streak;
        } finally {
            cursor.close();
        }
    }

    private static final class CheckInQuery {
        private CheckInQuery() {}

        public static final int ID = 301;

        public static final String SELECTION = CheckInEntry.COLUMN_HABIT_ID + " = ?";
        public static final String SELECTION_WITH_START =
                CheckInEntry.COLUMN_HABIT_ID + " = ? AND " + CheckInEntry.COLUMN_DATE + " >= ?";

        public static final String ORDER_BY_DATE = CheckInEntry.COLUMN_DATE;

        public static final String[] PROJECTION = new String[] {
                CheckInEntry.COLUMN_STATUS
        };

        public static final int COLUMN_STATUS = 0;
    }
}
