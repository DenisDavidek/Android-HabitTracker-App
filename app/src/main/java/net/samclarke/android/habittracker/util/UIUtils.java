package net.samclarke.android.habittracker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.notifications.ActionIntentService;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.ui.HabitActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UIUtils {
    public static String getQuoteOfDay(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.pref_quote_of_day),
                context.getString(R.string.default_quote_of_day));
    }

    public static void setQuoteOfDay(Context context, String quote) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_quote_of_day), quote);
        editor.apply();
    }

    public static String formatTarget(Context context, int target, int type, int operator) {
        String[] operators = context.getResources().getStringArray(R.array.target_operators);
        String[] types = context.getResources().getStringArray(R.array.target_types);

        if (operator == HabitEntry.TARGET_OPERATOR_NONE) {
            return context.getString(R.string.target_none);
        }

        if (type < 0 || type > types.length) {
            throw new IllegalArgumentException("Invalid type");
        }

        if (operator < 0 || operator > operators.length) {
            throw new IllegalArgumentException("Invalid operator");
        }

        return context.getString(R.string.target,
                operators[operator], String.valueOf(target), types[type]);
    }

    private static PendingIntent createActionIntent(Context context, int reminderId, int habitId,
                                             int date, String action) {

        Intent doneIntent = new Intent(context, ActionIntentService.class);
        doneIntent.setAction(action);
        doneIntent.putExtra(ActionIntentService.EXTRA_HABIT_ID, habitId);
        doneIntent.putExtra(ActionIntentService.EXTRA_REMINDER_ID, reminderId);
        doneIntent.putExtra(ActionIntentService.EXTRA_DATE, DateUtils.clearTime(date));

        return PendingIntent
                .getService(context, reminderId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void showReminderNotification(Context context, int reminderId,
                                                int habitId, String habitName) {

        int date = (int) TimeUnit.MILLISECONDS.toSeconds(new Date().getTime());

        PendingIntent doneIntent = createActionIntent(context, reminderId, habitId, date,
                ActionIntentService.ACTION_DONE);

        PendingIntent failIntent = createActionIntent(context, reminderId, habitId, date,
                ActionIntentService.ACTION_FAIL);

        PendingIntent skipIntent = createActionIntent(context, reminderId, habitId, date,
                ActionIntentService.ACTION_SKIP);

        Intent habitIntent = new Intent(context, HabitActivity.class);
        habitIntent.putExtra(HabitActivity.EXTRA_HABIT_ID, habitId);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, habitIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_event_available_black_18dp)
                .setContentTitle(habitName)
                .setContentText(context.getString(R.string.notification_content, habitName))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_done_black_18dp, "Done", doneIntent)
                .addAction(R.drawable.ic_clear_black_18dp, "Fail", failIntent)
                .addAction(R.drawable.ic_skip_next_black_18dp, "Skip", skipIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(reminderId, notification);
    }
}
