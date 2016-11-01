package net.samclarke.android.habittracker.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;
import net.samclarke.android.habittracker.util.GoalsUtils;

public class ActionIntentService extends IntentService {
    private static final String LOG_TAG = ActionIntentService.class.getSimpleName();

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    public static final String ACTION_SKIP = "skip";
    public static final String ACTION_DONE = "done";
    public static final String ACTION_FAIL = "fail";


    public ActionIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Received notification action");

        int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
        int date = intent.getIntExtra(EXTRA_DATE, -1);
        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);

        if (habitId == -1) {
            Log.e(LOG_TAG, "Received unknown habit ID");
            return;
        }

        if (date == -1) {
            Log.e(LOG_TAG, "Received unknown date");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(CheckInEntry.COLUMN_DATE, date);
        values.put(CheckInEntry.COLUMN_HABIT_ID, habitId);
        values.put(CheckInEntry.COLUMN_IS_AUTO_STATUS, 0);
        values.put(CheckInEntry.COLUMN_VALUE, 1);

        switch (intent.getAction()) {
            case ACTION_SKIP:
                values.put(CheckInEntry.COLUMN_STATUS, CheckInEntry.STATUS_SKIPPED);
                break;
            case ACTION_DONE:
                values.put(CheckInEntry.COLUMN_STATUS, CheckInEntry.STATUS_COMPLETE);
                break;
            case ACTION_FAIL:
                values.put(CheckInEntry.COLUMN_STATUS, CheckInEntry.STATUS_FAILED);
                break;
            default:
                Log.e(LOG_TAG, "Received unknown notification action");
                return;
        }

        getContentResolver().insert(CheckInEntry.CONTENT_URI, values);

        if (reminderId != -1) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(reminderId);
        } else {
            Log.e(LOG_TAG, "Received unknown reminder ID");
        }

        GoalsUtils.updateHabitGoals(this, habitId, date);
    }
}
