package net.samclarke.android.habittracker.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.adapters.RemindersAdapter;
import net.samclarke.android.habittracker.notifications.RescheduleIntentService;
import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemindersFragment extends Fragment implements RemindersAdapter.Listeners,
        LoaderManager.LoaderCallbacks<Cursor>, HabitActivity.FabFragment {

    private static final String EXTRA_HABIT_ID = "habit_id";
    private static final int REMINDERS_LOADER_ID = 1;

    @BindView(R.id.reminders_list) RecyclerView mRemindersList;
    @BindView(R.id.list_empty) NestedScrollView mEmptyMessage;

    private final RemindersAdapter mAdapter = new RemindersAdapter(this);
    private int mHabitId = -1;


    public static RemindersFragment newInstance(int habitId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_HABIT_ID, habitId);

        RemindersFragment fragment = new RemindersFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mHabitId = getArguments().getInt(EXTRA_HABIT_ID, -1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(REMINDERS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminders, container, false);

        ButterKnife.bind(this, rootView);

        mRemindersList.setAdapter(mAdapter);
        mRemindersList.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));

        return rootView;
    }

    @Override
    public void OnFabClick() {
        if (getFragmentManager() != null) {
             ReminderDialogFragment.newInstanceWithHabitId(mHabitId)
                     .show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onReminderEditClicked(int id) {
        ReminderDialogFragment.newInstanceWithReminderId(id).show(getFragmentManager(), "dialog");
    }

    @Override
    public void onReminderDeleteClicked(final int id) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete this reminder?")
                .setMessage("This will permanently delete the reminder.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), RescheduleIntentService.class);
                        intent.putExtra(RescheduleIntentService.EXTRA_REMINDER_ID, id);
                        intent.putExtra(RescheduleIntentService.EXTRA_REMOVE_REMINDER, true);
                        getContext().startService(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReminderDisabled(int id) {
        setReminderEnabled(id, false);
    }

    @Override
    public void onReminderEnabled(int id) {
        setReminderEnabled(id, true);
    }

    private void setReminderEnabled(int id, boolean isEnabled) {
        final Uri reminder = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, id);
        final ContentValues values = new ContentValues();

        values.put(ReminderEntry.COLUMN_IS_ENABLED, isEnabled);

        getContext().getContentResolver().update(reminder, values, null, null);

        Intent intent = new Intent(getContext(), RescheduleIntentService.class);
        intent.putExtra(RescheduleIntentService.EXTRA_REMINDER_ID, id);
        intent.putExtra(RescheduleIntentService.EXTRA_CLEAR_ONLY, !isEnabled);
        getContext().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;

        if (mHabitId != -1) {
            selection = ReminderEntry.COLUMN_HABIT_ID + " = ?";
            selectionArgs = new String[] { String.valueOf (mHabitId) };
        }

        return new CursorLoader(getContext(), ReminderEntry.CONTENT_URI,
                RemindersAdapter.PROJECTION, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        if (cursor != null && cursor.getCount() > 0) {
            mRemindersList.setVisibility(View.VISIBLE);
            mEmptyMessage.setVisibility(View.GONE);
        } else {
            mRemindersList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
