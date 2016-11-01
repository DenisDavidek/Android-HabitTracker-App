package net.samclarke.android.habittracker.adapters;

import android.database.Cursor;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.GoalEntry;

import java.text.DateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoalsAdapter extends CursorAdapter<GoalsAdapter.ViewHolder> {
    public interface Listeners {
        void onGoalEditClicked(int id);
        void onGoalDeleteClicked(int id);
    }

    public static final String[] PROJECTION = new String[] {
            GoalEntry._ID,
            GoalEntry.COLUMN_NAME,
            GoalEntry.COLUMN_TARGET,
            GoalEntry.COLUMN_START_DATE,
            GoalEntry.COLUMN_PROGRESS,
    };

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_TARGET = 2;
    private static final int COLUMN_START_DATE = 3;
    private static final int COLUMN_PROGRESS = 4;

    private Listeners mListeners;


    public GoalsAdapter(Listeners listeners) {
        mListeners = listeners;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_goal, parent, false);

        return new ViewHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.bind(cursor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {

        @BindView(R.id.goal_start_date) TextView mStartDate;
        @BindView(R.id.goal_name) TextView mName;
        @BindView(R.id.goal_target) TextView mTarget;
        @BindView(R.id.goal_menu) ImageButton mMenu;
        @BindView(R.id.goal_progress) ProgressBar mProgress;

        private int mGoalId;

        private final Listeners mListeners;
        private final PopupMenu mMenuPopup;


        ViewHolder(final View itemView, final Listeners listeners) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mListeners = listeners;

            mMenuPopup = new PopupMenu(mMenu.getContext(), mMenu, Gravity.END);
            mMenuPopup.setOnMenuItemClickListener(this);
            mMenuPopup.getMenuInflater()
                    .inflate(R.menu.menu_goal_list_item, mMenuPopup.getMenu());
        }

        public void bind(Cursor cursor) {
            String target = String.valueOf(cursor.getInt(COLUMN_TARGET));

            mGoalId = cursor.getInt(COLUMN_ID);
            mName.setText(cursor.getString(COLUMN_NAME));
            mTarget.setText(mTarget.getContext().getString(R.string.goal_target_streak, target));
            mStartDate.setText(DateFormat.getDateInstance().format(cursor.getLong(COLUMN_START_DATE)));

            mProgress.setMax(cursor.getInt(COLUMN_TARGET));
            mProgress.setProgress(cursor.getInt(COLUMN_PROGRESS));
        }

        @OnClick(R.id.goal_menu)
        void showMenu() {
            mMenuPopup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    mListeners.onGoalEditClicked(mGoalId);
                    return true;
                case R.id.action_remove:
                    mListeners.onGoalDeleteClicked(mGoalId);
                    return true;
            }

            return false;
        }
    }
}
