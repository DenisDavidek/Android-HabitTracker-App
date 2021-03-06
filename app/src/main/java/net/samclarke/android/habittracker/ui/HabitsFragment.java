package net.samclarke.android.habittracker.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.adapters.HabitsAdapter;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.util.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class HabitsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, HabitsAdapter.Listeners {

    @BindView(R.id.habits_list) RecyclerView mHabitsList;
    @BindView(R.id.habits_list_empty) TextView mEmptyMessage;

    private boolean mIncludeArchived = false;
    private boolean mSortByTitle = false;

    private static final int HABITS_LOADER_ID = 1;

    private static final String SELECTION_EXCLUDE_ARCHIVED =
            HabitEntry.COLUMN_IS_ARCHIVED + " = 0";

    private static final String SORT_BY_TITLE = HabitEntry.COLUMN_NAME + " ASC";
    private static final String SORT_BY_START_DATE = HabitEntry.COLUMN_START_DATE + " ASC";

    private final HabitsAdapter mAdapter = new HabitsAdapter(this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_habits, container, false);

        ButterKnife.bind(this, rootView);

        mHabitsList.setAdapter(mAdapter);

        getLoaderManager().initLoader(HABITS_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_habits_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_habit:
                startActivity(new Intent(getContext(), CreateHabitActivity.class));
                return true;

            case R.id.action_include_archived:
                item.setChecked(!item.isChecked());
                mIncludeArchived = item.isChecked();
                getLoaderManager().restartLoader(HABITS_LOADER_ID, null, this);
                return true;

            case R.id.action_sort_by_title:
                mSortByTitle = true;
                getLoaderManager().restartLoader(HABITS_LOADER_ID, null, this);
                return true;

            case R.id.action_sort_by_start_date:
                mSortByTitle = false;
                getLoaderManager().restartLoader(HABITS_LOADER_ID, null, this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHabitClicked(int id) {
        Intent intent = new Intent(getContext(), HabitActivity.class);
        intent.putExtra(HabitActivity.EXTRA_HABIT_ID, id);

        getContext().startActivity(intent);
    }

    @Override
    public void onHabitDateClicked(int id, CalendarDay date) {
        CheckInDialogFragment.newInstance(id, DateUtils.getCalendarDate(date).getTimeInMillis())
                .show(getFragmentManager(), "check in");
    }

    @Override
    public void onHabitEditClicked(int id) {
        Intent intent = new Intent(getContext(), CreateHabitActivity.class);
        intent.putExtra(CreateHabitActivity.EXTRA_HABIT_ID, id);

        getContext().startActivity(intent);
    }

    @Override
    public void onHabitArchiveClicked(int id) {
        Uri habitUri = ContentUris.withAppendedId(HabitEntry.CONTENT_URI, id);

        ContentValues values = new ContentValues();
        values.put(HabitEntry.COLUMN_IS_ARCHIVED, true);

        getContext().getContentResolver().update(habitUri, values, null, null);
    }

    @Override
    public void onHabitDeleteClicked(int id) {
        final Uri habitUri = ContentUris.withAppendedId(HabitEntry.CONTENT_URI, id);

        new AlertDialog.Builder(getContext())
            .setTitle(getContext().getString(R.string.dialog_remove_habit_title))
            .setMessage(getContext().getString(R.string.dialog_remove_habit_message))
            .setPositiveButton(getContext().getString(R.string.dialog_remove_habit_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getContext().getContentResolver().delete(habitUri, null, null);
                }
            })
            .setNegativeButton(getContext().getString(R.string.dialog_remove_habit_negative), null)
            .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortBy = mSortByTitle ? SORT_BY_TITLE : SORT_BY_START_DATE;
        String selection = "";

        if (!mIncludeArchived) {
            selection = SELECTION_EXCLUDE_ARCHIVED;
        }

        return new CursorLoader(
                getContext(),
                HabitEntry.CONTENT_WITH_STATUS_URI,
                HabitsAdapter.PROJECTION,
                selection,
                null,   //selectionArgs
                sortBy
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        if (cursor.getCount() > 0) {
            mHabitsList.setVisibility(View.VISIBLE);
            mEmptyMessage.setVisibility(View.GONE);
        } else {
            mHabitsList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
