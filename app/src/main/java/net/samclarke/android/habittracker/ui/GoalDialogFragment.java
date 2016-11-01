package net.samclarke.android.habittracker.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.DatePicker;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.GoalEntry;
import net.samclarke.android.habittracker.util.CheckInUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GoalDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.goal_start_date) TextInputEditText mStartDateInput;
    @BindView(R.id.goal_target) TextInputEditText mTargetInput;
    @BindView(R.id.goal_name) TextInputEditText mNameInput;

    private static final String EXTRA_HABIT_ID = "habit_id";
    private static final String EXTRA_GOAL_ID = "goal_id";

    private static final int GOAL_LOADER_ID = 701;

    private static final String[] PROJECTION = {
            GoalEntry.COLUMN_NAME,
            GoalEntry.COLUMN_START_DATE,
            GoalEntry.COLUMN_TARGET,
            GoalEntry.COLUMN_HABIT_ID
    };

    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_START_DATE = 1;
    private static final int COLUMN_TARGET = 2;
    private static final int COLUMN_HABIT_ID = 3;

    private int mHabitId = -1;
    private int mGoalId = -1;
    private Calendar mStartDate = Calendar.getInstance();
    private DatePickerDialog mDatePickerDialog;


    public static GoalDialogFragment newInstanceWithHabitId(int habitId) {
        return newInstance(habitId, -1);
    }

    public static GoalDialogFragment newInstanceWithGoalId(int goalId) {
        return newInstance(-1, goalId);
    }

    public static GoalDialogFragment newInstance(int habitId, int goalId) {
        Bundle args = new Bundle();
        args.putInt(GoalDialogFragment.EXTRA_HABIT_ID, habitId);
        args.putInt(GoalDialogFragment.EXTRA_GOAL_ID, goalId);

        GoalDialogFragment dialog = new GoalDialogFragment();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();

        if (args != null) {
            mHabitId = args.getInt(EXTRA_HABIT_ID, -1);
            mGoalId = args.getInt(EXTRA_GOAL_ID, -1);
        }

        if (mHabitId == -1 && mGoalId == -1) {
            throw new UnsupportedOperationException("A habit ID or goal ID is required");
        }

        if (mGoalId != -1) {
            getLoaderManager().initLoader(GOAL_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_goal_dialog, null, false);

        ButterKnife.bind(this, view);

        mDatePickerDialog = new DatePickerDialog(getContext(), this,
                mStartDate.get(Calendar.YEAR),
                mStartDate.get(Calendar.MONTH),
                mStartDate.get(Calendar.DAY_OF_MONTH));


        mStartDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.show();
            }
        });

        updateStartDateDisplay();

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.goal_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.goal_dialog_positive, null)
                .setNegativeButton(R.string.goal_dialog_negative, null)
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

    void updateStartDateDisplay() {
        mStartDateInput.setText(SimpleDateFormat.getDateInstance().format(mStartDate.getTime()));
    }

    boolean isValid() {
        String name = mNameInput.getText().toString();

        if (name.isEmpty()) {
            mNameInput.setError(getString(R.string.goal_name_required));
            return false;
        }

        return true;
    }

    int getTarget() {
        try {
            return Math.max(0, Integer.parseInt(mTargetInput.getText().toString()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    boolean validateAndSave() {
        if (!isValid()) {
            return false;
        }

        int longestStreak = CheckInUtils.getLongestStreak(getContext(), mHabitId,
                (int) TimeUnit.MILLISECONDS.toSeconds(mStartDate.getTimeInMillis()));

        ContentValues values = new ContentValues();
        values.put(GoalEntry.COLUMN_HABIT_ID, mHabitId);
        values.put(GoalEntry.COLUMN_NAME, mNameInput.getText().toString());
        values.put(GoalEntry.COLUMN_START_DATE, mStartDate.getTimeInMillis());
        values.put(GoalEntry.COLUMN_TARGET, getTarget());
        values.put(GoalEntry.COLUMN_PROGRESS, Math.min(longestStreak, getTarget()));

        if (mGoalId != -1) {
            Uri contentUri = ContentUris.withAppendedId(GoalEntry.CONTENT_URI, mGoalId);

            getContext().getContentResolver().update(contentUri, values, null, null);
        } else{
            getContext().getContentResolver().insert(GoalEntry.CONTENT_URI, values);
        }

        return true;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mStartDate.set(year, month, day);
        updateStartDateDisplay();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri goalUri = ContentUris.withAppendedId(GoalEntry.CONTENT_URI, mGoalId);

        return new CursorLoader(getContext(), goalUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            mHabitId = cursor.getInt(COLUMN_HABIT_ID);

            mNameInput.setText(cursor.getString(COLUMN_NAME));
            mTargetInput.setText(String.valueOf(cursor.getInt(COLUMN_TARGET)));
            mStartDate.setTime(new Date(cursor.getLong(COLUMN_START_DATE)));

            updateStartDateDisplay();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
