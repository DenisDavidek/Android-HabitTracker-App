package net.samclarke.android.habittracker.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.notifications.RescheduleIntentService;
import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;
import net.samclarke.android.habittracker.ui.pickers.WeekDaysPicker;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class ReminderDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.reminder_time_container) View mTimeContainer;
    @BindView(R.id.reminder_time) TextInputEditText mReminderTime;
    @BindView(R.id.frequency_days_of_week_label) View mFrequencyDaysOfWeekLabel;
    @BindView(R.id.frequency_days_of_week) WeekDaysPicker mFrequencyDaysOfWeek;
    @BindView(R.id.is_repeating_daily) CheckBox mIsFrequencyDaily;
    @BindView(R.id.reminder_location_container) View mLocationContainer;
    @BindView(R.id.reminder_location) TextInputEditText mLocation;
    @BindView(R.id.location_type) Spinner mLocationType;
    @BindView(R.id.reminder_type) Spinner mReminderType;

    private double mLocationLat;
    private double mLocationLong;
    private boolean mHasLocation = false;
    private int mReminderId = -1;
    private int mHabitId = -1;
    private int mLocationRadius = 25;
    private int mReminderTimeMinute = TIME_MIN;
    private boolean mIsEnabled = true;
    private boolean mIsAwaitingLocation = false;

    private static final String LOG_TAG = ReminderDialogFragment.class.getSimpleName();
    private static final String EXTRA_HABIT_ID = "habit_id";
    private static final String EXTRA_REMINDER_ID = "reminder_id";

    private static final int TIME_MIN = 0;

    private static final int REMINDER_PLACE_REQUEST = 900;
    private static final int REMINDER_LOADER_ID = 801;

    private static final String[] PROJECTION = {
            ReminderEntry.COLUMN_TIME,
            ReminderEntry.COLUMN_IS_ENABLED,
            ReminderEntry.COLUMN_GEO_LONG,
            ReminderEntry.COLUMN_GEO_LAT,
            ReminderEntry.COLUMN_GEO_LOCATION_NAME,
            ReminderEntry.COLUMN_HABIT_ID,
            ReminderEntry.COLUMN_GEO_TYPE,
            ReminderEntry.COLUMN_GEO_RADIUS,
            ReminderEntry.COLUMN_FREQUENCY,
            ReminderEntry.COLUMN_FREQUENCY_VALUE,
    };

    private static final int COLUMN_TIME = 0;
    private static final int COLUMN_IS_ENABLED = 1;
    private static final int COLUMN_GEO_LONG = 2;
    private static final int COLUMN_GEO_LAT = 3;
    private static final int COLUMN_GEO_LOCATION_NAME = 4;
    private static final int COLUMN_HABIT_ID = 5;
    private static final int COLUMN_GEO_TYPE = 6;
    private static final int COLUMN_GEO_RADIUS = 7;
    private static final int COLUMN_FREQUENCY =8;
    private static final int COLUMN_FREQUENCY_VALUE = 9;


    public static ReminderDialogFragment newInstanceWithHabitId(int habitId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_HABIT_ID, habitId);

        ReminderDialogFragment fragment = new ReminderDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static ReminderDialogFragment newInstanceWithReminderId(int reminderId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_REMINDER_ID, reminderId);

        ReminderDialogFragment fragment = new ReminderDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            mHabitId = args.getInt(EXTRA_HABIT_ID, -1);
            mReminderId = args.getInt(EXTRA_REMINDER_ID, -1);
        }

        if (mHabitId == -1 && mReminderId == -1) {
            throw new UnsupportedOperationException("A habit ID or reminder ID is required");
        }

        if (mReminderId != -1) {
            getLoaderManager().initLoader(REMINDER_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_reminder_dialog, null, false);

        ButterKnife.bind(this, view);

        Calendar cal = Calendar.getInstance();
        int nowInMinutes = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);

        setReminderTime(nowInMinutes);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reminder_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.reminder_dialog_positive, null)
                .setNegativeButton(R.string.reminder_dialog_negative, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (validateAndSave()) {
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });

        return dialog;
    }

    boolean validate() {
        if (mLocation.isShown() && !mHasLocation) {
            mLocation.setError(getString(R.string.reminder_location_missing_error));

            return false;
        }

        return true;
    }

    boolean validateAndSave() {
        if (!validate()) {
            return false;
        }

        ContentValues values = new ContentValues();

        values.put(ReminderEntry.COLUMN_HABIT_ID, mHabitId);
        values.put(ReminderEntry.COLUMN_IS_ENABLED, mIsEnabled);
        values.put(ReminderEntry.COLUMN_FREQUENCY, mIsFrequencyDaily.isChecked() ?
                ReminderEntry.FREQUENCY_DAILY : ReminderEntry.FREQUENCY_WEEKLY);
        values.put(ReminderEntry.COLUMN_FREQUENCY_VALUE, mFrequencyDaysOfWeek.getSelectedDaysBitmask());
        values.put(ReminderEntry.COLUMN_GEO_RADIUS, mLocationRadius);

        if (mLocation.isShown()) {
            values.put(ReminderEntry.COLUMN_GEO_LOCATION_NAME, mLocation.getText().toString());
            values.put(ReminderEntry.COLUMN_GEO_LAT, mLocationLat);
            values.put(ReminderEntry.COLUMN_GEO_LONG, mLocationLong);
            values.put(ReminderEntry.COLUMN_GEO_TYPE, mLocationType.getSelectedItemPosition());
            values.putNull(ReminderEntry.COLUMN_TIME);
        } else {
            values.putNull(ReminderEntry.COLUMN_GEO_LOCATION_NAME);
            values.putNull(ReminderEntry.COLUMN_GEO_LAT);
            values.putNull(ReminderEntry.COLUMN_GEO_LONG);
            values.putNull(ReminderEntry.COLUMN_GEO_TYPE);
            values.put(ReminderEntry.COLUMN_TIME, mReminderTimeMinute);
        }

        if (mReminderId != -1) {
            Uri reminderUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, mReminderId);
            getContext().getContentResolver().update(reminderUri, values, null, null);
        } else {
            Uri uri = getContext().getContentResolver().insert(ReminderEntry.CONTENT_URI, values);
            mReminderId = (int) ContentUris.parseId(uri);
        }

        Intent intent = new Intent(getContext(), RescheduleIntentService.class);
        intent.putExtra(RescheduleIntentService.EXTRA_REMINDER_ID, mReminderId);
        getContext().startService(intent);

        return true;
    }

    @OnCheckedChanged(R.id.is_repeating_daily)
    void onIsRepeatingDailyChanged(boolean isRepeatingDaily) {
        mFrequencyDaysOfWeek.setVisibility(isRepeatingDaily ? View.GONE : View.VISIBLE);
        mFrequencyDaysOfWeekLabel.setVisibility(mFrequencyDaysOfWeek.getVisibility());
    }

    @OnItemSelected(R.id.reminder_type)
    void onTypeChanged(int position) {
        boolean isTime = position == 0;

        mTimeContainer.setVisibility(isTime ? View.VISIBLE : View.GONE);
        mLocationContainer.setVisibility(isTime ? View.GONE : View.VISIBLE);
    }

    @OnClick({ R.id.reminder_location, R.id.reminder_location_label })
    void onLocationClicked() {
        try {
            // Hitting location twice quickly can cause startActivityForResult()
            // to be called twice without this guard
            if (mIsAwaitingLocation) {
                return;
            }

            mIsAwaitingLocation = true;
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            startActivityForResult(builder.build(getActivity()), REMINDER_PLACE_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException ex) {
            Log.e(LOG_TAG, "Problem creating place picker intent", ex);
        }
    }

    void setReminderTime(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;

        mReminderTimeMinute = minutes;
        mReminderTime.setText(getString(R.string.time_format, hour, minute));
    }

    @OnClick({R.id.reminder_time})
    void onTimeClicked(final View view) {
        int time = mReminderTimeMinute;

        int hour = time / 60;
        int minute = time % 60;

        TimePickerDialog picker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                int minutes = (hour * 60) + minute;

                setReminderTime(minutes);
            }
        }, hour, minute, true);

        picker.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REMINDER_PLACE_REQUEST) {
            mIsAwaitingLocation = false;

            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);

                mHasLocation = true;
                mLocation.setText(place.getName());
                mLocationLat = place.getLatLng().latitude;
                mLocationLong = place.getLatLng().longitude;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri reminderUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, mReminderId);

        return new CursorLoader(getContext(), reminderUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            Log.i(LOG_TAG, "Couldn't load reminder. Loader result was either null or empty.");
            return;
        }

        mHabitId = cursor.getInt(COLUMN_HABIT_ID);
        mIsEnabled = cursor.getInt(COLUMN_IS_ENABLED) != 0;
        mIsFrequencyDaily.setChecked(cursor.getInt(COLUMN_FREQUENCY) == ReminderEntry.FREQUENCY_DAILY);
        mFrequencyDaysOfWeek.setSelectedDaysBitmask((byte)cursor.getInt(COLUMN_FREQUENCY_VALUE));

        if (!cursor.isNull(COLUMN_GEO_LOCATION_NAME)) {
            mHasLocation = true;
            mReminderType.setSelection(1);

            mLocation.setText(cursor.getString(COLUMN_GEO_LOCATION_NAME));
            mLocationLat = cursor.getDouble(COLUMN_GEO_LAT);
            mLocationLong = cursor.getDouble(COLUMN_GEO_LONG);
            mLocationType.setSelection(cursor.getInt(COLUMN_GEO_TYPE));
            mLocationRadius = cursor.getInt(COLUMN_GEO_RADIUS);
        } else {
            mHasLocation = false;
            mReminderType.setSelection(0);
            setReminderTime(cursor.getInt(COLUMN_TIME));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
