package net.samclarke.android.habittracker.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract.CheckInEntry;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;
import net.samclarke.android.habittracker.util.DateUtils;
import net.samclarke.android.habittracker.util.MaterialCalendarUtils;
import net.samclarke.android.habittracker.util.UIUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsFragment extends Fragment implements OnDateSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>, MaterialCalendarUtils.DayStatusCallback {

    @BindView(R.id.habit_target) TextView mHabitTarget;
    @BindView(R.id.calendar_view) MaterialCalendarView mCalendarView;
    @BindView(R.id.status_line_chart) LineChart mStatusLineChart;
    @BindView(R.id.status_pie_chart) PieChart mStatusPieChart;
    @BindView(R.id.streaks_chart) HorizontalBarChart mStreaksChart;

    private static final int STATUS_CHART_DAYS = 30;
    private static final String EXTRA_HABIT_ID = "habit_id";

    private Map<Integer, DayStatus> mCheckInStatus = new ArrayMap<>();
    private int mHabitId = -1;
    private int mHabitFrequency = 0;
    private int mHabitFrequencyValue = 0;

    private final ValueFormatter integerFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value, Entry entry, int index, ViewPortHandler viewHandler) {
            return String.valueOf((int) value);
        }
    };

    private final DayViewDecorator mDisabledDecorator = new DayViewDecorator() {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !DateUtils.isDateEnabled(DateUtils.getCalendarDate(day),
                    mHabitFrequency, mHabitFrequencyValue);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    };


    public static DetailsFragment newInstance(int habitId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_HABIT_ID, habitId);

        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getArguments() != null) {
            mHabitId = getArguments().getInt(EXTRA_HABIT_ID);
        }

        if (mHabitId == -1) {
            throw new UnsupportedOperationException("A habit ID must be passed");
        }

        getLoaderManager().initLoader(HabitQuery.ID, null, this);
        getLoaderManager().initLoader(CheckInQuery.ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        ButterKnife.bind(this, rootView);

        mCalendarView.addDecorator(mDisabledDecorator);
        mCalendarView.addDecorators(MaterialCalendarUtils.getStatusDecorators(getContext(), this));
        mCalendarView.setOnDateChangedListener(this);
        mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE |
                MaterialCalendarView.SHOW_DECORATED_DISABLED);

        return rootView;
    }

    private void reloadStatusLineChart() {
        List<Entry> entries = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        int currentStreak = 0;

        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_MONTH, -STATUS_CHART_DAYS);

        for (int i = 0; i < STATUS_CHART_DAYS; i++) {
            day.add(Calendar.DAY_OF_MONTH, 1);

            long date = DateUtils.clearTime(day.getTime()).getTime();
            int timestamp = (int) TimeUnit.MILLISECONDS.toSeconds(date);

            if (mCheckInStatus.containsKey(timestamp)) {
                currentStreak = mCheckInStatus.get(timestamp).getStreak();
            }

            xVals.add(String.valueOf(i + 1));
            entries.add(new Entry(currentStreak, i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progress");
        LineData data = new LineData(xVals, dataSet);

        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.primary));
        dataSet.setCircleRadius(4);
        dataSet.setLineWidth(4);
        dataSet.setDrawValues(false);

        mStatusLineChart.setTouchEnabled(false);
        mStatusLineChart.getXAxis().setDrawLabels(false);
        mStatusLineChart.getXAxis().setEnabled(false);
        mStatusLineChart.getLegend().setEnabled(false);
        mStatusLineChart.setDescription(null);
        mStatusLineChart.getAxisLeft().setGranularity(1);
        mStatusLineChart.getAxisLeft().setGranularityEnabled(true);
        mStatusLineChart.getAxisLeft().setSpaceBottom(0);
        mStatusLineChart.getAxisRight().setGranularity(1);
        mStatusLineChart.getAxisRight().setGranularityEnabled(true);
        mStatusLineChart.getAxisRight().setSpaceBottom(0);
        mStatusLineChart.setData(data);
        mStatusLineChart.invalidate();
    }

    private void reloadStatusPieChart(int[] status) {
        List<Entry> entries = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (status[CheckInEntry.STATUS_COMPLETE] > 0) {
            xVals.add("Complete");
            entries.add(new BarEntry(status[CheckInEntry.STATUS_COMPLETE], 0));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_done));
        }

        if (status[CheckInEntry.STATUS_SKIPPED] > 0) {
            xVals.add("Skipped");
            entries.add(new BarEntry(status[CheckInEntry.STATUS_SKIPPED], 1));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_skipped));
        }

        if (status[CheckInEntry.STATUS_FAILED] > 0) {
            xVals.add("Failed");
            entries.add(new BarEntry(status[CheckInEntry.STATUS_FAILED], 2));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_failed));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(integerFormatter);

        mStatusPieChart.setTouchEnabled(false);
        mStatusPieChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        mStatusPieChart.getLegend().setTextSize(14);
        mStatusPieChart.setDrawSliceText(false);
        mStatusPieChart.setTransparentCircleRadius(0);
        mStatusPieChart.setDescription(null);
        mStatusPieChart.setData(data);
        mStatusPieChart.invalidate();
    }

    private void reloadStreaksChart(PriorityQueue<Integer> longestStreaks) {
        List<BarEntry> entries = new ArrayList<>(longestStreaks.size());
        ArrayList<String> xVals = new ArrayList<>(longestStreaks.size());

        int idx = 0;
        while (longestStreaks.size() > 0) {
            entries.add(new BarEntry(longestStreaks.poll(), idx));
            xVals.add("");

            idx++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Longest Streaks");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        dataSet.setValueTextSize(12);

        BarData data = new BarData(xVals, dataSet);
        data.setValueFormatter(integerFormatter);

        mStreaksChart.setTouchEnabled(false);
        mStreaksChart.getLegend().setEnabled(false);
        mStreaksChart.setDescription(null);
        mStreaksChart.getXAxis().setEnabled(false);
        mStreaksChart.getAxisLeft().setEnabled(false);
        mStreaksChart.getAxisRight().setEnabled(false);
        mStreaksChart.setData(data);
        mStreaksChart.invalidate();
    }

    private void reloadCheckIns(Cursor cursor) {
        PriorityQueue<Integer> longestStreaks = new PriorityQueue<>(Collections.nCopies(5, 0));
        int[] statuses = new int[] { 0, 0, 0, 0 };
        int currentStreak = 0;

        mCheckInStatus.clear();

        do {
            int timestamp = DateUtils.clearTime(cursor.getInt(CheckInQuery.COLUMN_DATE));
            int status = cursor.getInt(CheckInQuery.COLUMN_STATUS);

            if (status == CheckInEntry.STATUS_COMPLETE) {
                currentStreak++;
            } else if (status == CheckInEntry.STATUS_FAILED) {
                if (longestStreaks.peek() < currentStreak) {
                    longestStreaks.poll();
                    longestStreaks.add(currentStreak);
                }

                currentStreak = 0;
            }

            mCheckInStatus.put(timestamp, new DayStatus(status, currentStreak));

            statuses[status]++;
        } while (cursor.moveToNext());

        if (longestStreaks.peek() < currentStreak) {
            longestStreaks.poll();
            longestStreaks.add(currentStreak);
        }

        reloadStatusPieChart(statuses);
        reloadStreaksChart(longestStreaks);
        reloadStatusLineChart();
        mCalendarView.invalidateDecorators();
    }

    private void reloadHabit(Cursor cursor) {
        mHabitTarget.setText(UIUtils.formatTarget(
                getContext(),
                cursor.getInt(HabitQuery.COLUMN_TARGET),
                cursor.getInt(HabitQuery.COLUMN_TARGET_TYPE),
                cursor.getInt(HabitQuery.COLUMN_TARGET_OPERATOR)
        ));

        mHabitFrequency = cursor.getInt(HabitQuery.COLUMN_FREQUENCY);
        mHabitFrequencyValue = cursor.getInt(HabitQuery.COLUMN_FREQUENCY_VALUE);

        mCalendarView.state().edit()
                .setMinimumDate(new Date(cursor.getLong(HabitQuery.COLUMN_START_DATE)))
                .setMaximumDate(CalendarDay.today())
                .commit();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                               boolean selected) {

        CheckInDialogFragment
                .newInstance(mHabitId, DateUtils.getCalendarDate(date).getTimeInMillis())
                .show(getFragmentManager(), "check in");

        mCalendarView.clearSelection();
    }

    @Override
    public int getDayStatus(CalendarDay day) {
        int date = DateUtils.getTimestamp(day);

        if (!mCheckInStatus.containsKey(date)) {
            return CheckInEntry.STATUS_NONE;
        }

        return mCheckInStatus.get(date).getStatus();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case HabitQuery.ID:
                return new CursorLoader(
                        getContext(),
                        HabitEntry.CONTENT_URI,
                        HabitQuery.PROJECTION,
                        HabitQuery.SELECTION,
                        new String[] { String.valueOf(mHabitId) },
                        null
                );

            case CheckInQuery.ID:
                return new CursorLoader(
                        getContext(),
                        CheckInEntry.CONTENT_URI,
                        CheckInQuery.PROJECTION,
                        CheckInQuery.SELECTION,
                        new String[] { String.valueOf(mHabitId) },
                        CheckInQuery.ORDER_BY_DATE
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
            case HabitQuery.ID:
                reloadHabit(cursor);
                break;

            case CheckInQuery.ID:
                reloadCheckIns(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private static final class HabitQuery {
        private HabitQuery() {}

        public static final int ID = 500;

        public static final String SELECTION = HabitEntry._ID + " = ?";

        public static final String[] PROJECTION = new String[] {
                HabitEntry.COLUMN_START_DATE,
                HabitEntry.COLUMN_FREQUENCY,
                HabitEntry.COLUMN_FREQUENCY_VALUE,
                HabitEntry.COLUMN_COLOR,
                HabitEntry.COLUMN_DESCRIPTION,
                HabitEntry.COLUMN_TARGET,
                HabitEntry.COLUMN_TARGET_TYPE,
                HabitEntry.COLUMN_TARGET_OPERATOR
        };

        public static final int COLUMN_START_DATE = 0;
        public static final int COLUMN_FREQUENCY = 1;
        public static final int COLUMN_FREQUENCY_VALUE = 2;
        public static final int COLUMN_COLOR = 3;
        public static final int COLUMN_DESCRIPTION = 4;
        public static final int COLUMN_TARGET = 5;
        public static final int COLUMN_TARGET_TYPE = 6;
        public static final int COLUMN_TARGET_OPERATOR = 7;
    }

    private static final class CheckInQuery {
        private CheckInQuery() {}

        public static final int ID = 501;

        public static final String SELECTION = CheckInEntry.COLUMN_HABIT_ID + " = ?";
        public static final String ORDER_BY_DATE = CheckInEntry.COLUMN_DATE;

        public static final String[] PROJECTION = new String[] {
                CheckInEntry.COLUMN_DATE,
                CheckInEntry.COLUMN_STATUS
        };

        public static final int COLUMN_DATE = 0;
        public static final int COLUMN_STATUS = 1;
    }

    private static class DayStatus {
        private int mStatus;
        private int mStreak;

        public DayStatus(int status, int streak) {
            mStatus = status;
            mStreak = streak;
        }

        public int getStatus() {
            return mStatus;
        }

        public int getStreak() {
            return mStreak;
        }
    }
}
