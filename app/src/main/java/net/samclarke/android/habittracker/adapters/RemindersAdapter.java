package net.samclarke.android.habittracker.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.ReminderEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RemindersAdapter extends CursorAdapter<RemindersAdapter.ViewHolder> {
    public interface Listeners {
        void onReminderEditClicked(int id);
        void onReminderDeleteClicked(int id);
        void onReminderDisabled(int id);
        void onReminderEnabled(int id);
    }

    public static final String[] PROJECTION = new String[] {
            ReminderEntry._ID,
            ReminderEntry.COLUMN_TIME,
            ReminderEntry.COLUMN_IS_ENABLED,
            ReminderEntry.COLUMN_GEO_LOCATION_NAME,
    };

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TIME = 1;
    private static final int COLUMN_IS_ENABLED = 2;
    private static final int COLUMN_GEO_LOCATION_NAME = 3;

    private Listeners mListeners;


    public RemindersAdapter(Listeners listeners) {
        mListeners = listeners;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_reminder, parent, false);

        return new ViewHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.bind(cursor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener, CompoundButton.OnCheckedChangeListener {

        @BindView(R.id.reminder_time) TextView mTime;
        @BindView(R.id.reminder_menu) ImageButton mMenu;
        @BindView(R.id.reminder_switch) Switch mIsEnabled;
        @BindView(R.id.reminder_location) TextView mLocation;

        private int mReminderId;
        private boolean mIsBinding;

        private final Listeners mListeners;
        private final PopupMenu mMenuPopup;
        private final Context mContext;


        ViewHolder(final View itemView, final Listeners listeners) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mListeners = listeners;

            mContext = itemView.getContext();

            mMenuPopup = new PopupMenu(mMenu.getContext(), mMenu, Gravity.END);
            mMenuPopup.setOnMenuItemClickListener(this);
            mMenuPopup.getMenuInflater()
                    .inflate(R.menu.menu_goal_list_item, mMenuPopup.getMenu());

            mIsEnabled.setOnCheckedChangeListener(this);
        }

        public void bind(Cursor cursor) {
            mIsBinding = true;
            mReminderId = cursor.getInt(COLUMN_ID);
            mIsEnabled.setChecked(cursor.getInt(COLUMN_IS_ENABLED) != 0);

            if (cursor.isNull(COLUMN_TIME)) {
                mTime.setText(R.string.reminder_any_time);
                mLocation.setText(cursor.getString(COLUMN_GEO_LOCATION_NAME));
            } else {
                int hour = cursor.getInt(COLUMN_TIME) / 60;
                int minute = cursor.getInt(COLUMN_TIME) % 60;

                mTime.setText(mContext.getString(R.string.time_format, hour, minute));
                mLocation.setText(R.string.reminder_any_location);
            }

            mIsBinding = false;
        }

        @OnClick(R.id.reminder_menu)
        void showMenu() {
            mMenuPopup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    mListeners.onReminderEditClicked(mReminderId);
                    return true;
                case R.id.action_remove:
                    mListeners.onReminderDeleteClicked(mReminderId);
                    return true;
            }

            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (mIsBinding) {
                return;
            }

            if (isChecked) {
                mListeners.onReminderEnabled(mReminderId);
            } else {
                mListeners.onReminderDisabled(mReminderId);
            }
        }
    }
}
