package net.samclarke.android.habittracker.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.samclarke.android.habittracker.provider.HabitsContract.GoalEntry;
import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoalsUtils {
    public static class GoalStats {
        public final int completed;
        public final int total;

        GoalStats(int completed, int total) {
            this.completed = completed;
            this.total = total;
        }
    }

    public static GoalStats getStats(Context context) {
        final Cursor cursor = context.getContentResolver().query(
                GoalEntry.CONTENT_URI,
                GoalStatsQuery.PROJECTION,
                "", // Selection
                null, // selectionArgs
                null // orderBy
        );

        if (cursor == null) {
            return null;
        }

        try {
            int completed = 0;

            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(GoalStatsQuery.COLUMN_PROGRESS) >=
                            cursor.getInt(GoalStatsQuery.COLUMN_TARGET)) {

                        completed += 1;
                    }
                } while (cursor.moveToNext());

            }

            return new GoalStats(completed, cursor.getCount());
        } finally {
            cursor.close();
        }
    }

    public static void updateHabitGoals(Context context, int habitId, int from) {
        long fromDate = TimeUnit.SECONDS.toMillis(DateUtils.clearTime(from));
        final Cursor cursor = context.getContentResolver().query(
                GoalEntry.CONTENT_URI,
                HabitGoalsQuery.PROJECTION,
                HabitGoalsQuery.SELECTION,
                new String[]{ String.valueOf(habitId), String.valueOf(fromDate) },
                null // orderBy
        );

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        final HashMap<Integer, Integer> streaks = getStreaks(context, habitId);

        try {
            if (!cursor.moveToFirst()) {
                return;
            }

            do {
                int target = cursor.getInt(HabitGoalsQuery.COLUMN_TARGET);
                int progress = cursor.getInt(HabitGoalsQuery.COLUMN_PROGRESS);
                int startDate = (int) TimeUnit.MILLISECONDS.toSeconds(
                        cursor.getLong(HabitGoalsQuery.COLUMN_START_DATE));

                startDate = DateUtils.clearTime(startDate);

                int newProgress = getProgress(startDate, target, streaks);
                if (progress == newProgress) {
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put(GoalEntry.COLUMN_PROGRESS, newProgress);

                context.getContentResolver().update(
                        GoalEntry.CONTENT_URI,
                        values,
                        GoalEntry._ID + " = ?",
                        new String[] {
                                String.valueOf(cursor.getInt(HabitGoalsQuery.COLUMN_ID))
                        }
                );
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    private static int getProgress(int startDate, int target, HashMap<Integer, Integer> streaks) {
        int startingStreak = 0;
        int longestStreak = 0;

        for (Map.Entry<Integer, Integer> streak : streaks.entrySet()) {
            if (streak.getKey() >= startDate) {
                if (streak.getValue() == 0) {
                    startingStreak = 0;
                }

                if (streak.getKey() >= startDate && (streak.getValue() - startingStreak) >= target) {
                    return target;
                }

                if (streak.getKey() > longestStreak) {
                    longestStreak = streak.getValue();
                }
            } else {
                startingStreak = streak.getValue();
            }
        }

        return longestStreak;
    }

    private static HashMap<Integer, Integer> getStreaks(Context context, int habitId) {
        final Cursor cursor = context.getContentResolver().query(
                CheckInEntry.CONTENT_URI,
                CheckInQuery.PROJECTION,
                CheckInQuery.SELECTION,
                new String[] { String.valueOf(habitId) },
                CheckInQuery.ORDER_BY_DATE
        );

        if (cursor == null) {
            return null;
        }

        try {
            final LinkedHashMap<Integer, Integer> streaks = new LinkedHashMap<>(cursor.getCount());

            int currentStreak = 0;
            if (cursor.moveToFirst()) {
                do {
                    int status = cursor.getInt(CheckInQuery.COLUMN_STATUS);
                    int date = DateUtils.clearTime(cursor.getInt(CheckInQuery.COLUMN_DATE));

                    if (status == CheckInEntry.STATUS_COMPLETE) {
                        currentStreak += 1;
                    } else if (status == CheckInEntry.STATUS_FAILED) {
                        currentStreak = 0;
                    }

                    streaks.put(date, currentStreak);
                } while (cursor.moveToNext());
            }

            return streaks;
        } finally {
            cursor.close();
        }
    }

    private static final class CheckInQuery {
        private CheckInQuery() {}

        public static final String SELECTION = CheckInEntry.COLUMN_HABIT_ID + " = ?";
        public static final String ORDER_BY_DATE = CheckInEntry.COLUMN_DATE;

        public static final String[] PROJECTION = new String[] {
                CheckInEntry.COLUMN_DATE,
                CheckInEntry.COLUMN_STATUS
        };

        public static final int COLUMN_DATE = 0;
        public static final int COLUMN_STATUS = 1;
    }

    private static final class HabitGoalsQuery {
        private HabitGoalsQuery() {}

        public static final String SELECTION =
                GoalEntry.COLUMN_HABIT_ID + " = ? AND " + GoalEntry.COLUMN_START_DATE + " <= ? ";

        public static final String[] PROJECTION = new String[] {
                GoalEntry.COLUMN_START_DATE,
                GoalEntry.COLUMN_TARGET,
                GoalEntry.COLUMN_PROGRESS,
                GoalEntry._ID
        };

        public static final int COLUMN_START_DATE = 0;
        public static final int COLUMN_TARGET = 1;
        public static final int COLUMN_PROGRESS = 2;
        public static final int COLUMN_ID = 3;
    }

    private static final class GoalStatsQuery {
        private GoalStatsQuery() {}

        public static final String[] PROJECTION = new String[] {
                GoalEntry.COLUMN_PROGRESS,
                GoalEntry.COLUMN_TARGET
        };

        public static final int COLUMN_PROGRESS = 0;
        public static final int COLUMN_TARGET = 1;
    }
}
