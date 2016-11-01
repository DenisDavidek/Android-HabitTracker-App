package net.samclarke.android.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;
import net.samclarke.android.habittracker.util.UIUtils;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";
    public static final String EXTRA_FREQUENCY = "frequency";
    public static final String EXTRA_FREQUENCY_VALUE = "frequency_value";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Received alarm intent");

        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
        int frequency = intent.getIntExtra(EXTRA_FREQUENCY, -1);
        int frequencyValue = intent.getIntExtra(EXTRA_FREQUENCY_VALUE, 0);
        String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);

        if (reminderId == -1) {
            Log.e(LOG_TAG, "Alarm intent missing reminder ID");
            return;
        }

        if (habitId == -1) {
            Log.e(LOG_TAG, "Alarm intent missing habit ID");
            return;
        }

        if (frequency == -1) {
            Log.e(LOG_TAG, "Alarm intent missing frequency");
            return;
        }

        if (frequency == ReminderEntry.FREQUENCY_WEEKLY) {
            int todayMask = 1 << Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if ((frequencyValue & todayMask) != todayMask) {
                return;
            }
        }

        UIUtils.showReminderNotification(context, reminderId, habitId, habitName);
    }
}