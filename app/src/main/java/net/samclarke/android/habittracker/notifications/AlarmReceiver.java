package net.samclarke.android.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.samclarke.android.habittracker.util.UIUtils;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Received alarm intent");

        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
        String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);

        if (reminderId == -1) {
            Log.e(LOG_TAG, "Alarm intent missing reminder ID");
            return;
        }

        if (habitId == -1) {
            Log.e(LOG_TAG, "Alarm intent missing habit ID");
            return;
        }

        UIUtils.showReminderNotification(context, reminderId, habitId, habitName);
    }
}