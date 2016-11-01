package net.samclarke.android.habittracker.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;
import net.samclarke.android.habittracker.util.UIUtils;

import java.util.Calendar;


public class GeofenceTransitionIntentService extends IntentService {
    private static final String LOG_TAG = GeofenceTransitionIntentService.class.getSimpleName();

    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";
    public static final String EXTRA_FREQUENCY = "frequency";
    public static final String EXTRA_FREQUENCY_VALUE = "frequency_value";

    public GeofenceTransitionIntentService() {
        super(LOG_TAG);
    }

    String getGeofenceErrorString(int errorCode) {
        Resources resources = getResources();

        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return resources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return resources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return resources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return resources.getString(R.string.unknown_geofence_error);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "Received Geofence transition intent");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(LOG_TAG, getGeofenceErrorString(geofencingEvent.getErrorCode()));
            return;
        }

        Log.i(LOG_TAG, String.valueOf(geofencingEvent.getGeofenceTransition()));

        switch (geofencingEvent.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
            case Geofence.GEOFENCE_TRANSITION_DWELL:
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
                int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
                String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);
                int frequency = intent.getIntExtra(EXTRA_FREQUENCY, -1);
                int frequencyValue = intent.getIntExtra(EXTRA_FREQUENCY_VALUE, 0);

                if (reminderId == -1) {
                    Log.e(LOG_TAG, "Geofence intent missing reminder ID");
                    return;
                }

                if (habitId == -1) {
                    Log.e(LOG_TAG, "Geofence intent missing habit ID");
                    return;
                }

                if (frequency == -1) {
                    Log.e(LOG_TAG, "Geofence intent missing frequency");
                    return;
                }

                if (frequency == ReminderEntry.FREQUENCY_WEEKLY) {
                    int todayMask = 1 << Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if ((frequencyValue & todayMask) != todayMask) {
                        return;
                    }
                }

                UIUtils.showReminderNotification(this, reminderId, habitId, habitName);
                break;
            default:
                Log.e(LOG_TAG, "Invalid Geofencing transition");
        }
    }
}
