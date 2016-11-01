package net.samclarke.android.habittracker.ui;

import android.content.ContentUris;
import android.content.DialogInterface;
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
import net.samclarke.android.habittracker.adapters.GoalsAdapter;
import net.samclarke.android.habittracker.provider.HabitsContract.GoalEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GoalsFragment extends Fragment implements GoalsAdapter.Listeners,
        LoaderManager.LoaderCallbacks<Cursor>, HabitActivity.FabFragment {

    private static final String EXTRA_HABIT_ID = "habit_id";
    private static final int GOALS_LOADER_ID = 1;

    @BindView(R.id.goals_list) RecyclerView mGoalsList;
    @BindView(R.id.list_empty) NestedScrollView mEmptyMessage;

    private final GoalsAdapter mAdapter = new GoalsAdapter(this);
    private int mHabitId = -1;


    public static GoalsFragment newInstance(int habitId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_HABIT_ID, habitId);

        GoalsFragment fragment = new GoalsFragment();
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

        getLoaderManager().initLoader(GOALS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_goals, container, false);

        ButterKnife.bind(this, rootView);

        mGoalsList.setAdapter(mAdapter);
        mGoalsList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));

        return rootView;
    }

    @Override
    public void OnFabClick() {
        if (getFragmentManager() != null) {
            GoalDialogFragment.newInstanceWithHabitId(mHabitId).show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onGoalEditClicked(int id) {
        GoalDialogFragment.newInstanceWithGoalId(id).show(getFragmentManager(), "dialog");
    }

    @Override
    public void onGoalDeleteClicked(int id) {
        final Uri goalUri = ContentUris.withAppendedId(GoalEntry.CONTENT_URI, id);

        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.dialog_remove_goal_title))
                .setMessage(getContext().getString(R.string.dialog_remove_goal_message))
                .setPositiveButton(getContext().getString(R.string.dialog_remove_goal_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().getContentResolver().delete(goalUri, null, null);
                    }
                })
                .setNegativeButton(getContext().getString(R.string.dialog_remove_goal_negative), null)
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;

        if (mHabitId != -1) {
            selection = GoalEntry.COLUMN_HABIT_ID + " = ?";
            selectionArgs = new String[] { String.valueOf (mHabitId) };
        }

        return new CursorLoader(getContext(), GoalEntry.CONTENT_URI, GoalsAdapter.PROJECTION,
                selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        if (cursor != null && cursor.getCount() > 0) {
            mGoalsList.setVisibility(View.VISIBLE);
            mEmptyMessage.setVisibility(View.GONE);
        } else {
            mGoalsList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}