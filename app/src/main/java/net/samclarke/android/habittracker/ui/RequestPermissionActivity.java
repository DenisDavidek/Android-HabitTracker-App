package net.samclarke.android.habittracker.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.notifications.RescheduleIntentService;

public class RequestPermissionActivity extends Activity {
    public static final String EXTRA_PERMISSION = "permission";
    public static final String EXTRA_GRANTED_ACTION = "grated_action";
    public static final String EXTRA_ACTION_HABIT_ID = "habit_id";
    public static final String EXTRA_ACTION_REMINDER_ID = "reminder_id";
    public static final String ACTION_RESCHEDULE_NOTIFICATIONS = "reschedule_notifications";
    private static final int REQUEST_ID = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permisson);

        final String permission = getIntent().getStringExtra(EXTRA_PERMISSION);

        ActivityCompat.requestPermissions(this, new String[] { permission }, REQUEST_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] results) {

        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (!getIntent().hasExtra(EXTRA_GRANTED_ACTION)) {
            finish();
        }

        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            switch (getIntent().getStringExtra(EXTRA_GRANTED_ACTION)) {
                case ACTION_RESCHEDULE_NOTIFICATIONS: {
                    Intent intent = new Intent(this, RescheduleIntentService.class);

                    if (getIntent().hasExtra(EXTRA_ACTION_HABIT_ID)) {
                        intent.putExtra(RescheduleIntentService.EXTRA_HABIT_ID,
                                getIntent().getIntExtra(EXTRA_ACTION_HABIT_ID, -1));
                    }

                    if (getIntent().hasExtra(EXTRA_ACTION_REMINDER_ID)) {
                        intent.putExtra(RescheduleIntentService.EXTRA_REMINDER_ID,
                                getIntent().getIntExtra(EXTRA_ACTION_REMINDER_ID, -1));
                    }

                    startService(intent);
                    break;
                }
                default:
                    throw new UnsupportedOperationException();
            }
        }

        finish();
    }
}
