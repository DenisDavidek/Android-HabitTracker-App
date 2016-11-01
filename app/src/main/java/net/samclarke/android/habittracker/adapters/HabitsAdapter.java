package net.samclarke.android.habittracker.adapters;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract;
import net.samclarke.android.habittracker.util.MaterialCalendarUtils;
import net.samclarke.android.habittracker.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class HabitsAdapter extends CursorAdapter<HabitsAdapter.ViewHolder> {
    public interface Listeners {
        void onHabitClicked(int id);
        void onHabitEditClicked(int id);
        void onHabitArchiveClicked(int id);
        void onHabitDeleteClicked(int id);
        void onHabitDateClicked(int id, CalendarDay date);
    }

    private Listeners mListeners;

    public static final String[] PROJECTION = new String[] {
            HabitsContract.HabitEntry._ID,
            HabitsContract.HabitEntry.COLUMN_NAME,
            HabitsContract.HabitEntry.COLUMN_COLOR,
            HabitsContract.HabitEntry.COLUMN_IS_ARCHIVED,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY0_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY1_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY2_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY3_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY4_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY5_STATUS,
            HabitsContract.HabitEntry.VIRTUAL_COLUMN_DAY6_STATUS,
            HabitsContract.HabitEntry.COLUMN_START_DATE,
            HabitsContract.HabitEntry.COLUMN_FREQUENCY,
            HabitsContract.HabitEntry.COLUMN_FREQUENCY_VALUE,
    };

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_COLOR = 2;
    private static final int COLUMN_IS_ARCHIVED = 3;
    private static final int COLUMN_DAY0_STATUS = 4;
    private static final int COLUMN_DAY1_STATUS = 5;
    private static final int COLUMN_DAY2_STATUS = 6;
    private static final int COLUMN_DAY3_STATUS = 7;
    private static final int COLUMN_DAY4_STATUS = 8;
    private static final int COLUMN_DAY5_STATUS = 9;
    private static final int COLUMN_DAY6_STATUS = 10;
    private static final int COLUMN_START_DATE = 11;
    private static final int COLUMN_FREQUENCY = 12;
    private static final int COLUMN_FREQUENCY_VALUE = 13;


    public HabitsAdapter(Listeners listeners) {
        mListeners = listeners;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_habit, parent, false);

        return new ViewHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.bind(cursor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            PopupMenu.OnMenuItemClickListener, OnDateSelectedListener, MaterialCalendarUtils.DayStatusCallback {

        @BindView(R.id.calendar_view) MaterialCalendarView mCalendarView;
        @BindView(R.id.habit_name) TextView mName;
        @BindView(R.id.habit_color) View mColor;
        @BindView(R.id.habit_menu) ImageButton mMenu;

        private static final int DAYS_IN_WEEK = 7;

        private int mHabitId = -1;
        private int[] mDayStatus = new int[DAYS_IN_WEEK];
        private int mFrequency = -1;
        private int mFrequencyValue = -1;
        private Date mStartDate;
        private Date mTodaysDate = new Date();

        private final PopupMenu mMenuPopup;
        private final Listeners mListeners;
        private final DayViewDecorator mDisabledDecorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar date = DateUtils.getCalendarDate(day);

                return date.getTime().before(mStartDate) ||
                        !DateUtils.isDateEnabled(date, mFrequency, mFrequencyValue);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setDaysDisabled(true);
            }
        };


        ViewHolder(final View itemView, Listeners listeners) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mListeners = listeners;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -6);

            mCalendarView.setTopbarVisible(false);
            mCalendarView.setOnDateChangedListener(this);
            mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE |
                    MaterialCalendarView.SHOW_DECORATED_DISABLED);
            mCalendarView.addDecorators(mDisabledDecorator);
            mCalendarView.addDecorators(
                    MaterialCalendarUtils.getStatusDecorators(itemView.getContext(), this));
            mCalendarView.state().edit()
                    .setMaximumDate(mTodaysDate)
                    .setMinimumDate(calendar.getTime())
                    .setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
                    .setCalendarDisplayMode(CalendarMode.WEEKS)
                    .commit();

            itemView.setOnClickListener(this);

            mMenu.setOnClickListener(this);
            mMenuPopup = new PopupMenu(mMenu.getContext(), mMenu, Gravity.END);
            mMenuPopup.setOnMenuItemClickListener(this);
            mMenuPopup.getMenuInflater()
                    .inflate(R.menu.menu_habit_list_item, mMenuPopup.getMenu());
        }

        public void bind(Cursor cursor) {
            mStartDate = DateUtils.clearTime(new Date(cursor.getLong(COLUMN_START_DATE)));
            mFrequency = cursor.getInt(COLUMN_FREQUENCY);
            mFrequencyValue = cursor.getInt(COLUMN_FREQUENCY_VALUE);
            mHabitId = cursor.getInt(COLUMN_ID);
            mName.setText(cursor.getString(COLUMN_NAME));
            mColor.setBackgroundColor(cursor.getInt(COLUMN_COLOR));

            mDayStatus[0] = cursor.getInt(COLUMN_DAY0_STATUS);
            mDayStatus[1] = cursor.getInt(COLUMN_DAY1_STATUS);
            mDayStatus[2] = cursor.getInt(COLUMN_DAY2_STATUS);
            mDayStatus[3] = cursor.getInt(COLUMN_DAY3_STATUS);
            mDayStatus[4] = cursor.getInt(COLUMN_DAY4_STATUS);
            mDayStatus[5] = cursor.getInt(COLUMN_DAY5_STATUS);
            mDayStatus[6] = cursor.getInt(COLUMN_DAY6_STATUS);
            mCalendarView.invalidateDecorators();
        }

        @Override
        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                                   boolean selected) {

            mListeners.onHabitDateClicked(mHabitId, date);
            mCalendarView.clearSelection();
        }

        @Override
        public void onClick(View view) {
            if (view == mMenu) {
                mMenuPopup.show();
            } else {
                mListeners.onHabitClicked(mHabitId);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    mListeners.onHabitEditClicked(mHabitId);
                    return true;
                case R.id.action_archive:
                    mListeners.onHabitArchiveClicked(mHabitId);
                    return true;
                case R.id.action_remove:
                    mListeners.onHabitDeleteClicked(mHabitId);
                    return true;
            }

            return false;
        }

        @Override
        public int getDayStatus(CalendarDay calendarDay) {
            int day = DateUtils.daysBetweenDates(calendarDay.getDate(), mTodaysDate);
            if (day < 0 || day >= mDayStatus.length) {
                return HabitsContract.CheckInEntry.STATUS_NONE;
            }

            return mDayStatus[day];
        }
    }
}
