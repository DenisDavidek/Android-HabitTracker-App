package net.samclarke.android.habittracker.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.util.UIUtils;


public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";

    public GeofenceTransitionsIntentService() {
        super(TAG);
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
        Log.i(TAG, "Received Geofence transition intent");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, getGeofenceErrorString(geofencingEvent.getErrorCode()));
            return;
        }

        Log.i(TAG, String.valueOf(geofencingEvent.getGeofenceTransition()));

        switch (geofencingEvent.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
            case Geofence.GEOFENCE_TRANSITION_DWELL:
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
                int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
                String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);

                if (reminderId == -1) {
                    Log.e(TAG, "Geofence intent missing reminder ID");
                    return;
                }

                if (habitId == -1) {
                    Log.e(TAG, "Geofence intent missing habit ID");
                    return;
                }
                Log.e("habitId", String.valueOf(habitId));
                Log.e("reminderId", String.valueOf(reminderId));
                UIUtils.showReminderNotification(this, reminderId, habitId, habitName);
                break;
            default:
                Log.e(TAG, "Invalid Geofencing transition");
        }
    }
}
