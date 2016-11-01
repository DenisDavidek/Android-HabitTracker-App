package net.samclarke.android.habittracker.ui;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumPalette;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.ui.pickers.MonthDaysPicker;
import net.samclarke.android.habittracker.ui.pickers.WeekDaysPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

public class CreateHabitActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CreateHabitActivity.class.getSimpleName();
    public static final String EXTRA_HABIT_ID = "habit_id";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.habit_name) TextInputEditText mHabitName;
    @BindView(R.id.habit_description) TextInputEditText mHabitDescription;
    @BindView(R.id.habit_color) SpectrumPalette mHabitColorPalette;
    @BindView(R.id.habit_start_date) TextInputEditText mStartDate;
    @BindView(R.id.target_type) Spinner mTargetType;
    @BindView(R.id.target) TextInputEditText mTarget;
    @BindView(R.id.target_postfix) TextView mTargetPostFix;
    @BindView(R.id.target_operator) Spinner mTargetOperator;
    @BindView(R.id.habit_frequency) Spinner mHabitFrequency;
    @BindView(R.id.frequency_interval_container) LinearLayout mFrequencyIntervalContainer;
    @BindView(R.id.frequency_interval) TextInputEditText mFrequencyInterval;
    @BindView(R.id.frequency_days_of_week) WeekDaysPicker mFrequencyDaysOfWeek;
    @BindView(R.id.frequency_days_of_month) MonthDaysPicker mFrequencyDaysOfMonth;

    private int mHabitId = -1;
    private DatePickerDialog mDatePickerDialog;
    private Calendar mStartDateValue = Calendar.getInstance();
    private int mHabitColor = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        mHabitId = this.getIntent().getIntExtra(EXTRA_HABIT_ID, -1);

        mDatePickerDialog = new DatePickerDialog(this, this,
                mStartDateValue.get(Calendar.YEAR),
                mStartDateValue.get(Calendar.MONTH),
                mStartDateValue.get(Calendar.DAY_OF_MONTH));

        updateStartDateDisplay();

        mHabitColor = ContextCompat.getColor(this, R.color.default_theme);

        mHabitColorPalette.setSelectedColor(mHabitColor);
        mHabitColorPalette.setOnColorSelectedListener(new SpectrumPalette.OnColorSelectedListener() {
            @Override
            public void onColorSelected(@ColorInt int color) {
                mHabitColor = color;
            }
        });

        mStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.show();
            }
        });

        if (mHabitId != -1) {
            getSupportLoaderManager().initLoader(HabitQuery.ID, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_habit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveAndFinish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mStartDateValue.set(year, month, day);
        updateStartDateDisplay();
    }

    @OnItemSelected(R.id.target_operator)
    public void onTargetSelected(int operator) {
        if (operator == HabitEntry.TARGET_OPERATOR_NONE) {
            mTarget.setVisibility(View.GONE);
            mTargetPostFix.setVisibility(View.GONE);
        } else {
            mTarget.setVisibility(View.VISIBLE);
            mTargetPostFix.setVisibility(View.VISIBLE);
        }
    }

    void setFrequencyValue(int value) {
        switch (mHabitFrequency.getSelectedItemPosition()) {
            case HabitEntry.FREQUENCY_DAILY:
                break;
            case HabitEntry.FREQUENCY_WEEKLY:
                mFrequencyDaysOfWeek.setSelectedDaysBitmask((byte) value);
                break;
            case HabitEntry.FREQUENCY_MONTHLY:
                mFrequencyDaysOfMonth.setSelectedDaysBitmask(value);
                break;
            case HabitEntry.FREQUENCY_INTERVAL:
                mFrequencyInterval.setText(String.valueOf(value));
                break;
            default:
                throw new UnsupportedOperationException("Unknown frequency");
        }
    }

    int getFrequencyValue() {
        switch (mHabitFrequency.getSelectedItemPosition()) {
            case HabitEntry.FREQUENCY_DAILY:
                return 0;
            case HabitEntry.FREQUENCY_WEEKLY:
                return mFrequencyDaysOfWeek.getSelectedDaysBitmask();
            case HabitEntry.FREQUENCY_MONTHLY:
                return mFrequencyDaysOfMonth.getSelectedDaysBitmask();
            case HabitEntry.FREQUENCY_INTERVAL:
                return Math.min(1, Integer.parseInt(mFrequencyInterval.getText().toString()));
            default:
                throw new UnsupportedOperationException("Unknown frequency");
        }
    }

    void updateStartDateDisplay() {
        mStartDate.setText(SimpleDateFormat.getDateInstance().format(mStartDateValue.getTime()));
    }

    @OnItemSelected(R.id.habit_frequency)
    void updateFrequencyDisplay(int frequency) {
        mFrequencyIntervalContainer.setVisibility(View.GONE);
        mFrequencyDaysOfWeek.setVisibility(View.GONE);
        mFrequencyDaysOfMonth.setVisibility(View.GONE);

        switch (frequency) {
            case HabitEntry.FREQUENCY_DAILY:
                break;
            case HabitEntry.FREQUENCY_WEEKLY:
                mFrequencyDaysOfWeek.setVisibility(View.VISIBLE);
                break;
            case HabitEntry.FREQUENCY_MONTHLY:
                mFrequencyDaysOfMonth.setVisibility(View.VISIBLE);
                break;
            case HabitEntry.FREQUENCY_INTERVAL:
                mFrequencyIntervalContainer.setVisibility(View.VISIBLE);
                break;
            default:
                throw new UnsupportedOperationException("Unknown frequency");
        }
    }

    boolean isValid() {
        String habitName = mHabitName.getText().toString();

        if (!habitName.isEmpty() && mHabitId == -1) {
            Cursor cursor = getContentResolver().query(HabitEntry.CONTENT_URI, null,
                    HabitEntry.COLUMN_NAME + " = ?", new String[] { habitName }, null);

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    mHabitName.setError(getString(R.string.habit_error_name_exists, habitName));
                    return false;
                }

                cursor.close();
            }
        }

        if (habitName.isEmpty()) {
            mHabitName.setError(getString(R.string.habit_error_name_required));
            return false;
        }

        if (mFrequencyInterval.isShown() && mFrequencyInterval.getText().toString().isEmpty()) {
            mFrequencyInterval.setError(getString(R.string.habit_error_interval_required));
            return false;
        }

        if (mFrequencyDaysOfWeek.isShown() && mFrequencyDaysOfWeek.getSelectedDaysBitmask() == 0) {
            Toast.makeText(this, R.string.habit_error_day_of_week_required, Toast.LENGTH_LONG).show();
            return false;
        }

        if (mFrequencyDaysOfMonth.isShown() && mFrequencyDaysOfMonth.getSelectedDaysBitmask() == 0) {
            Toast.makeText(this, R.string.habit_error_day_of_month_required, Toast.LENGTH_LONG).show();
            return false;
        }

        if (mTarget.isShown() && mTarget.getText().toString().isEmpty()) {
            mTarget.setError(getString(R.string.habit_error_target_required));
            return false;
        }

        return true;
    }

    void saveAndFinish() {
        if (!isValid()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(HabitEntry.COLUMN_COLOR, mHabitColor);
        values.put(HabitEntry.COLUMN_DESCRIPTION, mHabitDescription.getText().toString());
        values.put(HabitEntry.COLUMN_FREQUENCY, mHabitFrequency.getSelectedItemPosition());
        values.put(HabitEntry.COLUMN_FREQUENCY_VALUE, getFrequencyValue());
        values.put(HabitEntry.COLUMN_NAME, mHabitName.getText().toString());
        values.put(HabitEntry.COLUMN_START_DATE, mStartDateValue.getTimeInMillis());
        values.put(HabitEntry.COLUMN_TARGET, mTarget.getText().toString());
        values.put(HabitEntry.COLUMN_TARGET_OPERATOR, mTargetOperator.getSelectedItemPosition());
        values.put(HabitEntry.COLUMN_TARGET_TYPE, mTargetType.getSelectedItemPosition());

        if (mHabitId != -1) {
            Uri contentUri = ContentUris.withAppendedId(HabitEntry.CONTENT_URI, mHabitId);
            getContentResolver().update(contentUri, values, null, null);
        } else{
            getContentResolver().insert(HabitEntry.CONTENT_URI, values);
        }

        finish();
    }

    void reloadHabit(Cursor cursor) {
        mHabitName.setText(cursor.getString(HabitQuery.COLUMN_NAME));
        mHabitDescription.setText(cursor.getString(HabitQuery.COLUMN_DESCRIPTION));
        mHabitColorPalette.setSelectedColor(cursor.getInt(HabitQuery.COLUMN_COLOR));
        mHabitFrequency.setSelection(cursor.getInt(HabitQuery.COLUMN_FREQUENCY));
        mTargetType.setSelection(cursor.getInt(HabitQuery.COLUMN_TARGET_TYPE));
        mTarget.setText(cursor.getString(HabitQuery.COLUMN_TARGET));
        mTargetOperator.setSelection(cursor.getInt(HabitQuery.COLUMN_TARGET_OPERATOR));

        setFrequencyValue(cursor.getInt(HabitQuery.COLUMN_FREQUENCY_VALUE));

        mStartDateValue.setTime(new Date(cursor.getLong(HabitQuery.COLUMN_START_DATE)));
        updateStartDateDisplay();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case HabitQuery.ID:
                Uri contentUri = ContentUris.withAppendedId(HabitEntry.CONTENT_URI, mHabitId);
                return new CursorLoader(this, contentUri, HabitQuery.PROJECTION, null, null, null);

            default:
                throw new UnsupportedOperationException("Unknown loader ID");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }

        switch (loader.getId()) {
            case HabitQuery.ID:
                reloadHabit(cursor);
                break;

            default:
                throw new UnsupportedOperationException("Unknown loader ID");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private static final class HabitQuery {
        private HabitQuery() {}

        public static final int ID = 101;

        private static final String[] PROJECTION = new String[] {
                HabitEntry.COLUMN_COLOR,
                HabitEntry.COLUMN_DESCRIPTION,
                HabitEntry.COLUMN_FREQUENCY,
                HabitEntry.COLUMN_FREQUENCY_VALUE,
                HabitEntry.COLUMN_NAME,
                HabitEntry.COLUMN_START_DATE,
                HabitEntry.COLUMN_TARGET,
                HabitEntry.COLUMN_TARGET_OPERATOR,
                HabitEntry.COLUMN_TARGET_TYPE
        };

        private static final int COLUMN_COLOR = 0;
        private static final int COLUMN_DESCRIPTION = 1;
        private static final int COLUMN_FREQUENCY = 2;
        private static final int COLUMN_FREQUENCY_VALUE = 3;
        private static final int COLUMN_NAME = 4;
        private static final int COLUMN_START_DATE = 5;
        private static final int COLUMN_TARGET = 6;
        private static final int COLUMN_TARGET_OPERATOR = 7;
        private static final int COLUMN_TARGET_TYPE = 8;
    }
}
