package net.samclarke.android.habittracker.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.ui.HabitActivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

class HabitsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor mCursor;
    private Context mContext;

    private final static int DAYS_IN_WEEK = 7;


    private static final String[] DAY_NAMES = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();

    private static final int[] DAY_LABEL_IDS = new int[] {
            R.id.day_label_0,
            R.id.day_label_1,
            R.id.day_label_2,
            R.id.day_label_3,
            R.id.day_label_4,
            R.id.day_label_5,
            R.id.day_label_6,
    };

    private static final int[] DAY_VALUE_IDS = new int[] {
            R.id.day_value_0,
            R.id.day_value_1,
            R.id.day_value_2,
            R.id.day_value_3,
            R.id.day_value_4,
            R.id.day_value_5,
            R.id.day_value_6,
    };

    private static final String[] COLUMNS_PROJECTION = new String[] {
            HabitEntry._ID,
            HabitEntry.COLUMN_NAME,
            HabitEntry.VIRTUAL_COLUMN_DAY0_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY1_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY2_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY3_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY4_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY5_STATUS,
            HabitEntry.VIRTUAL_COLUMN_DAY6_STATUS,
            HabitEntry.COLUMN_COLOR,
    };
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_NAME = 1;
    private static final int VIRTUAL_COLUMN_DAY0_STATUS = 2;
    private static final int VIRTUAL_COLUMN_DAY1_STATUS = 3;
    private static final int VIRTUAL_COLUMN_DAY2_STATUS = 4;
    private static final int VIRTUAL_COLUMN_DAY3_STATUS = 5;
    private static final int VIRTUAL_COLUMN_DAY4_STATUS = 6;
    private static final int VIRTUAL_COLUMN_DAY5_STATUS = 7;
    private static final int VIRTUAL_COLUMN_DAY6_STATUS = 8;
    private static final int COLUMN_COLOR = 9;

    HabitsRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(HabitEntry.CONTENT_WITH_STATUS_URI,
                COLUMNS_PROJECTION, HabitEntry.COLUMN_IS_ARCHIVED + " = 0", null, null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    private void setStatusBackgroud(RemoteViews view, int viewId, int status) {
        switch (status) {
            case CheckInEntry.STATUS_COMPLETE:
                view.setTextColor(viewId, Color.WHITE);
                view.setInt(viewId, "setBackgroundResource", R.drawable.day_done);
                break;

            case CheckInEntry.STATUS_FAILED:
                view.setTextColor(viewId, Color.WHITE);
                view.setInt(viewId, "setBackgroundResource", R.drawable.day_failed);
                break;

            case CheckInEntry.STATUS_SKIPPED:
                view.setTextColor(viewId, Color.WHITE);
                view.setInt(viewId, "setBackgroundResource", R.drawable.day_skipped);
                break;

            default:
                view.setTextColor(viewId, Color.BLACK);
                break;
        }
    }

    @Override
    public RemoteViews getViewAt(int pos) {
        mCursor.moveToPosition(pos);

        final int[] STATUS_VALUES = new int[] {
                mCursor.getInt(VIRTUAL_COLUMN_DAY6_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY5_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY4_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY3_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY2_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY1_STATUS),
                mCursor.getInt(VIRTUAL_COLUMN_DAY0_STATUS),
        };

        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_habit);

        view.setTextViewText(R.id.habit_name, mCursor.getString(COLUMN_NAME));

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            view.setTextViewText(DAY_LABEL_IDS[i], DAY_NAMES[calendar.get(Calendar.DAY_OF_WEEK)]);
            view.setTextViewText(DAY_VALUE_IDS[i], String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            setStatusBackgroud(view, DAY_VALUE_IDS[i], STATUS_VALUES[i]);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent();
        intent.putExtra(HabitActivity.EXTRA_HABIT_ID, mCursor.getInt(COLUMN_ID));
        view.setOnClickFillInIntent(R.id.list_item, intent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        mCursor.moveToPosition(i);

        return mCursor.getLong(COLUMN_ID);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
