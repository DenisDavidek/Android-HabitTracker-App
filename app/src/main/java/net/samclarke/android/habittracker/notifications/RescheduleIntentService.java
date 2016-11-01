package net.samclarke.android.habittracker.notifications;


import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.ui.RequestPermissionActivity;

import java.util.Calendar;

public class RescheduleIntentService extends IntentService {
    private static final String LOG_TAG = RescheduleIntentService.class.getSimpleName();
    private static final long NEVER_EXPIRE = -1L;
    private static final int LOITER_DELAY_5_MINUTES = 300000;
    private static final float GEO_FENCE_RADIUS_IN_METRES = 25;

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_REMOVE_REMINDER = "extra_delete_reminder";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_CLEAR_ONLY = "extra_clear_only";

    private int mHabitId = -1;
    private int mReminderId = -1;
    private boolean mIsRequestingGeoPermission = false;
    private GoogleApiClient mGoogleApiClient;


    public RescheduleIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mHabitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
        mReminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        mIsRequestingGeoPermission = false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        ConnectionResult result = mGoogleApiClient.blockingConnect();

        if (!result.isSuccess()) {
            Log.e(LOG_TAG, "GoogleApiClient connection failed. Code: " + result.getErrorCode() +
                    "Message: " + result.getErrorMessage());
            return;
        }

        boolean removeReminder = intent.getBooleanExtra(EXTRA_REMOVE_REMINDER, false);
        boolean clearOnly = removeReminder || intent.getBooleanExtra(EXTRA_CLEAR_ONLY, false);
        String selection = null;
        String[] selectionArgs = null;

        if (intent.hasExtra(EXTRA_REMINDER_ID)) {
            selection = RemindersQuery.SELECT_BY_ID;
            selectionArgs = new String[] { String.valueOf(mReminderId) };
        } else if (intent.hasExtra(EXTRA_HABIT_ID)) {
            selection = RemindersQuery.SELECT_BY_HABIT;
            selectionArgs = new String[] { String.valueOf(mHabitId) };
        }

        Cursor cursor = getContentResolver().query(ReminderEntry.CONTENT_URI,
                RemindersQuery.PROJECTION, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            reschedule(cursor, clearOnly);
        }

        if (mReminderId != -1 && removeReminder) {
            Uri reminderUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, mReminderId);

            getContentResolver().delete(reminderUri, null, null);
        }
    }

    private String getHabitName(int habitId) {
        final int COLUMN_NAME = 0;

        Cursor cursor = getContentResolver().query(
                HabitEntry.CONTENT_URI,
                new String[] { HabitEntry.COLUMN_NAME },
                HabitEntry._ID + " = ?",
                new String[] { String.valueOf(habitId)},
                null
        );

        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        try {
            return cursor.getString(COLUMN_NAME);
        } finally {
            cursor.close();
        }
    }

    private void reschedule(Cursor cursor, boolean clearOnly) {
        SparseArray<String> nameCache = new SparseArray<>();

        do {
            int habitId = cursor.getInt(RemindersQuery.COLUMN_HABIT_ID);
            String habitName = nameCache.get(habitId);

            if (habitName == null) {
                habitName = getHabitName(habitId);
                nameCache.put(habitId, habitName);
            }

            if (!cursor.isNull(RemindersQuery.COLUMN_GEO_TYPE)) {
                rescheduleGeofence(clearOnly, cursor, habitName);
            } else {
                rescheduleAlarm(clearOnly, cursor, habitName);
            }
        } while (cursor.moveToNext());
    }

    private void rescheduleAlarm(boolean clearOnly, Cursor cursor, String habitName) {
        int reminderId = cursor.getInt(RemindersQuery.COLUMN_ID);
        int habitId = cursor.getInt(RemindersQuery.COLUMN_HABIT_ID);
        int frequency = cursor.getInt(RemindersQuery.COLUMN_FREQUENCY);
        int frequencyValue = cursor.getInt(RemindersQuery.COLUMN_FREQUENCY_VALUE);
        int timeInMinutes = cursor.getInt(RemindersQuery.COLUMN_TIME);
        int hour = timeInMinutes / 60;
        int minute = timeInMinutes % 60;

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminderId);
        intent.putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId);
        intent.putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName);
        intent.putExtra(AlarmReceiver.EXTRA_FREQUENCY, frequency);
        intent.putExtra(AlarmReceiver.EXTRA_FREQUENCY_VALUE, frequencyValue);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.cancel(pendingIntent);
        if (!clearOnly) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private int getTransitionType(int type) {
        switch (type) {
            case ReminderEntry.GEOFENCE_TRANSITION_DWELL:
                return Geofence.GEOFENCE_TRANSITION_DWELL;
            case ReminderEntry.GEOFENCE_TRANSITION_ENTER:
                return Geofence.GEOFENCE_TRANSITION_ENTER;
            case ReminderEntry.GEOFENCE_TRANSITION_LEAVE:
                return Geofence.GEOFENCE_TRANSITION_EXIT;
            default:
                return 0;
        }
    }


    private void rescheduleGeofence(boolean clearOnly, Cursor cursor, String habitName) {
        int reminderId = cursor.getInt(RemindersQuery.COLUMN_ID);
        int habitId = cursor.getInt(RemindersQuery.COLUMN_HABIT_ID);
        int frequency = cursor.getInt(RemindersQuery.COLUMN_FREQUENCY);
        int frequencyValue = cursor.getInt(RemindersQuery.COLUMN_FREQUENCY_VALUE);
        double latitude = cursor.getDouble(RemindersQuery.COLUMN_GEO_LAT);
        double longitude = cursor.getDouble(RemindersQuery.COLUMN_GEO_LONG);
        int type = cursor.getInt(RemindersQuery.COLUMN_GEO_TYPE);

        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        intent.putExtra(GeofenceTransitionIntentService.EXTRA_REMINDER_ID, reminderId);
        intent.putExtra(GeofenceTransitionIntentService.EXTRA_HABIT_ID, habitId);
        intent.putExtra(GeofenceTransitionIntentService.EXTRA_HABIT_NAME, habitName);
        intent.putExtra(GeofenceTransitionIntentService.EXTRA_FREQUENCY, frequency);
        intent.putExtra(GeofenceTransitionIntentService.EXTRA_FREQUENCY_VALUE, frequencyValue);

        PendingIntent pendingIntent = PendingIntent.getService(this, reminderId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, pendingIntent);

        if (clearOnly || mIsRequestingGeoPermission) {
            return;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Intent requestIntent = new Intent(this, RequestPermissionActivity.class);
            requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (mReminderId != -1) {
                requestIntent.putExtra(RequestPermissionActivity.EXTRA_ACTION_REMINDER_ID,
                        mReminderId);
            }

            if (mHabitId != -1) {
                requestIntent.putExtra(RequestPermissionActivity.EXTRA_ACTION_HABIT_ID, mHabitId);
            }

            requestIntent.putExtra(RequestPermissionActivity.EXTRA_GRANTED_ACTION,
                    RequestPermissionActivity.ACTION_RESCHEDULE_NOTIFICATIONS);
            requestIntent.putExtra(RequestPermissionActivity.EXTRA_PERMISSION,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            startActivity(requestIntent);
            mIsRequestingGeoPermission = true;
        } else {
            Geofence fence = new Geofence.Builder()
                    .setRequestId(String.valueOf(reminderId))
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setCircularRegion(latitude, longitude, GEO_FENCE_RADIUS_IN_METRES)
                    .setLoiteringDelay(LOITER_DELAY_5_MINUTES)
                    .setTransitionTypes(getTransitionType(type))
                    .build();

            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
            builder.addGeofence(fence);

            GeofencingRequest request = builder.build();

            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request, pendingIntent);
        }
    }

    private static final class RemindersQuery {
        private RemindersQuery() {}

        static final String SELECT_BY_ID =
                ReminderEntry._ID + " = ?";

        static final String SELECT_BY_HABIT =
                ReminderEntry.COLUMN_HABIT_ID + " = ?";

        static final String[] PROJECTION = new String[] {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_TIME,
                ReminderEntry.COLUMN_GEO_LAT,
                ReminderEntry.COLUMN_GEO_LONG,
                ReminderEntry.COLUMN_GEO_TYPE,
                ReminderEntry.COLUMN_HABIT_ID,
                ReminderEntry.COLUMN_FREQUENCY,
                ReminderEntry.COLUMN_FREQUENCY_VALUE,
        };

        static final int COLUMN_ID = 0;
        static final int COLUMN_TIME = 1;
        static final int COLUMN_GEO_LAT = 2;
        static final int COLUMN_GEO_LONG = 3;
        static final int COLUMN_GEO_TYPE = 4;
        static final int COLUMN_HABIT_ID = 5;
        static final int COLUMN_FREQUENCY = 6;
        static final int COLUMN_FREQUENCY_VALUE = 7;
    }
}
