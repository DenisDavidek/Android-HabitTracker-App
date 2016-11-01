package net.samclarke.android.habittracker.ui;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.util.DateUtils;
import net.samclarke.android.habittracker.util.GoalsUtils;
import net.samclarke.android.habittracker.util.UIUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckInDialogFragment extends DialogFragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, DialogInterface.OnClickListener {

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_DATE = "date";

    @BindView(R.id.check_in_value_container) View mCheckInValueContainer;
    @BindView(R.id.check_in_value_plus) ImageButton mCheckInValuePlusButton;
    @BindView(R.id.check_in_value_minus) ImageButton mCheckInValueMinusButton;
    @BindView(R.id.check_in_value) EditText mCheckInValue;
    @BindView(R.id.check_in_value_prefix) TextView mCheckInValuePrefix;
    @BindView(R.id.check_in_value_postfix) TextView mCheckInValuePostfix;
    @BindView(R.id.check_in_target) TextView mCheckInTarget;
    @BindView(R.id.check_in_notes) EditText mCheckInNotes;
    @BindView(R.id.check_in_auto_status) CheckBox mIsAutoStatus;
    @BindView(R.id.check_in_manual_status_label) TextView mManualStatusLabel;
    @BindView(R.id.check_in_manual_status) Spinner mManualStatus;

    private int mDate;
    private int mHabitId;
    private int mCheckInId = -1;
    private int mCheckInStatus = -1;
    private int mTarget = -1;
    private int mTargetType = -1;
    private int mTargetOperator = -1;


    public static CheckInDialogFragment newInstance(int id, long date) {
        Bundle args = new Bundle();
        args.putInt(CheckInDialogFragment.EXTRA_HABIT_ID, id);
        args.putLong(CheckInDialogFragment.EXTRA_DATE, date);

        CheckInDialogFragment dialog = new CheckInDialogFragment();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();

        if (args != null) {
            long date = args.getLong(EXTRA_DATE, new Date().getTime());

            mHabitId = args.getInt(EXTRA_HABIT_ID, -1);
            mDate = DateUtils.clearTime((int) TimeUnit.MILLISECONDS.toSeconds(date));
        }

        if (mHabitId == -1) {
            throw new UnsupportedOperationException("A habit ID must be passed");
        }

        getLoaderManager().initLoader(CheckInQuery.ID, null, this);
        getLoaderManager().initLoader(TargetQuery.ID, null, this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_check_in_dialog, null, false);

        ButterKnife.bind(this, view);

        mIsAutoStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mManualStatusLabel.setVisibility(View.GONE);
                    mManualStatus.setVisibility(View.GONE);
                } else {
                    mManualStatusLabel.setVisibility(View.VISIBLE);
                    mManualStatus.setVisibility(View.VISIBLE);
                }
            }
        });

        mManualStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mCheckInStatus = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        mCheckInValuePlusButton.setOnClickListener(this);
        mCheckInValueMinusButton.setOnClickListener(this);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.check_in_title)
                .setView(view)
                .setPositiveButton(R.string.check_in_positive_button, this)
                .setNegativeButton(R.string.check_in_negative_button, null)
                .setNeutralButton(R.string.check_in_neutral_button, this)
                .create();
    }

    private double getCheckInValue() {
        try {
            return Double.parseDouble(mCheckInValue.getText().toString());
        } catch(NumberFormatException ex) {
            return 0;
        }
    }

    private int getCheckInStatus() {
        if (!mIsAutoStatus.isChecked()) {
            return mCheckInStatus;
        }

        if (hasReachedTarget()) {
            return CheckInEntry.STATUS_COMPLETE;
        }

        return CheckInEntry.STATUS_FAILED;
    }

    private boolean hasReachedTarget() {
        double value = getCheckInValue();

        if (mTargetType == HabitEntry.TARGET_TYPE_NOTES) {
            return !mCheckInNotes.getText().toString().isEmpty();
        }

        switch (mTargetOperator) {
            case HabitEntry.TARGET_OPERATOR_EQUAL:
                return value == mTarget;
            case HabitEntry.TARGET_OPERATOR_GREATER:
                return value > mTarget;
            case HabitEntry.TARGET_OPERATOR_GREATER_EQUAL:
                return value >= mTarget;
            case HabitEntry.TARGET_OPERATOR_LESS:
                return value < mTarget;
            case HabitEntry.TARGET_OPERATOR_LESS_EQUAL:
                return value <= mTarget;
            case HabitEntry.TARGET_OPERATOR_NONE:
            default:
                return true;
        }
    }

    private void save() {
        ContentValues values = new ContentValues();
        values.put(CheckInEntry.COLUMN_DATE, mDate);
        values.put(CheckInEntry.COLUMN_HABIT_ID, mHabitId);
        values.put(CheckInEntry.COLUMN_NOTE, mCheckInNotes.getText().toString());
        values.put(CheckInEntry.COLUMN_STATUS, getCheckInStatus());
        values.put(CheckInEntry.COLUMN_IS_AUTO_STATUS, mIsAutoStatus.isChecked());
        values.put(CheckInEntry.COLUMN_VALUE, getCheckInValue());

        if (mCheckInId != -1) {
            Uri checkInUri = ContentUris.withAppendedId(CheckInEntry.CONTENT_URI, mCheckInId);

            getContext().getContentResolver().update(checkInUri, values, null, null);
        } else {
            getContext().getContentResolver().insert(CheckInEntry.CONTENT_URI, values);
        }

        GoalsUtils.updateHabitGoals(getContext(), mHabitId, mDate);
    }

    private void loadCheckIn(Cursor cursor) {
        mCheckInId = cursor.getInt(CheckInQuery.COLUMN_ID);
        mCheckInStatus = cursor.getInt(CheckInQuery.COLUMN_STATUS);

        mManualStatus.setSelection(mCheckInStatus);
        mCheckInValue.setText(String.valueOf(cursor.getDouble(CheckInQuery.COLUMN_VALUE)));
        mCheckInNotes.setText(cursor.getString(CheckInQuery.COLUMN_NOTE));
        mIsAutoStatus.setChecked(cursor.getInt(CheckInQuery.COLUMN_IS_AUTO_STATUS) == 1);
    }

    private void loadTarget(Cursor cursor) {
        String[] targets = getResources().getStringArray(R.array.target_types);

        mTarget = cursor.getInt(TargetQuery.COLUMN_TARGET);
        mTargetType = cursor.getInt(TargetQuery.COLUMN_TARGET_TYPE);
        mTargetOperator = cursor.getInt(TargetQuery.COLUMN_TARGET_OPERATOR);

        mCheckInValuePrefix.setText(getString(R.string.check_in_value_prefix));
        mCheckInValuePostfix.setText(getString(R.string.check_in_value_postfix,
                targets[mTargetType]));
        mCheckInTarget.setText(
                UIUtils.formatTarget(getContext(), mTarget, mTargetType, mTargetOperator));

        if (mTargetType == HabitEntry.TARGET_TYPE_NOTES) {
            mCheckInValueContainer.setVisibility(View.GONE);
            mCheckInTarget.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_NEUTRAL) {
            mIsAutoStatus.setChecked(false);
            mCheckInStatus = CheckInEntry.STATUS_SKIPPED;
        }

        save();
    }

    @Override
    public void onClick(View view) {
        if (view == mCheckInValuePlusButton) {
            mCheckInValue.setText(String.valueOf(getCheckInValue() + 1));
        } else if (view == mCheckInValueMinusButton) {
            mCheckInValue.setText(String.valueOf(getCheckInValue() - 1));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CheckInQuery.ID:
                return new CursorLoader(
                        getContext(),
                        CheckInEntry.CONTENT_URI,
                        CheckInQuery.PROJECTION,
                        CheckInQuery.SELECTION,
                        new String[] { String.valueOf(mHabitId), String.valueOf(mDate) },
                        null
                );

            case TargetQuery.ID:
                return new CursorLoader(
                        getContext(),
                        HabitEntry.CONTENT_URI,
                        TargetQuery.PROJECTION,
                        TargetQuery.SELECTION,
                        new String[] { String.valueOf(mHabitId) },
                        null
                );

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
            case CheckInQuery.ID:
                loadCheckIn(cursor);
                break;

            case TargetQuery.ID:
                loadTarget(cursor);
                break;

            default:
                throw new UnsupportedOperationException("Unknown loader ID");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private static final class CheckInQuery {
        private CheckInQuery() {}

        public static final int ID = 400;

        public static final String SELECTION = CheckInEntry.COLUMN_HABIT_ID + " = ? " +
                "AND date(" + CheckInEntry.COLUMN_DATE + ", 'unixepoch') = date(?, 'unixepoch')";

        public static final String[] PROJECTION = new String[] {
                CheckInEntry._ID,
                CheckInEntry.COLUMN_VALUE,
                CheckInEntry.COLUMN_NOTE,
                CheckInEntry.COLUMN_STATUS,
                CheckInEntry.COLUMN_IS_AUTO_STATUS
        };

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_VALUE = 1;
        public static final int COLUMN_NOTE = 2;
        public static final int COLUMN_STATUS = 3;
        public static final int COLUMN_IS_AUTO_STATUS = 4;
    }

    private static final class TargetQuery {
        private TargetQuery() {}

        public static final int ID = 401;

        public static final String SELECTION = HabitEntry._ID + " = ?";

        public static final String[] PROJECTION = new String[] {
                HabitEntry.COLUMN_TARGET,
                HabitEntry.COLUMN_TARGET_OPERATOR,
                HabitEntry.COLUMN_TARGET_TYPE
        };

        public static final int COLUMN_TARGET = 0;
        public static final int COLUMN_TARGET_OPERATOR = 1;
        public static final int COLUMN_TARGET_TYPE = 2;
    }
}
