package net.samclarke.android.habittracker.ui;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;

import net.samclarke.android.habittracker.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HabitDetailsActivityFragment extends Fragment {
    private static final String PARAM_HABIT_ID = "habitId";
    private int mHabitId;

    @BindView(R.id.habit_name) TextView mHabitName;
    @BindView(R.id.calendar_view) MaterialCalendarView mCalendarView;
    @BindView(R.id.line_chart) LineChart mLineChart;


    public static MainActivityFragment newInstance(int habitId) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mHabitId = getArguments().getInt(PARAM_HABIT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_habit_details, container, false);

        ButterKnife.bind(this, rootView);

        mHabitName.setText("Habit Name");

        // Limit calendar dates to when habit started up to this month?
        // Have date change listener to update graph too
//        mcv.state().edit()
//                .setFirstDayOfWeek(Calendar.WEDNESDAY)
//                .setMinimumDate(CalendarDay.from(2016, 4, 3))
//                .setMaximumDate(CalendarDay.from(2016, 5, 12))
//                .setCalendarDisplayMode(CalendarMode.WEEKS)
//                .commit();
//
        mCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.getDay() > 4 && day.getDay() < 8 || day.getDay() > 10 && day.getDay() < 23 || day.getDay() == 9;
            }

            @Override
            public void decorate(DayViewFacade view) {
                Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.day_done);

                view.setBackgroundDrawable(background);
            }
        });

        mCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.getDay() == 8;
            }

            @Override
            public void decorate(DayViewFacade view) {
                Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.day_failed);

                view.setBackgroundDrawable(background);
            }
        });

        mCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.getDay() == 10;
            }

            @Override
            public void decorate(DayViewFacade view) {
                Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.day_skipped);

                view.setBackgroundDrawable(background);
            }
        });

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(0, 2));
        entries.add(new Entry(0, 3));
        entries.add(new Entry(0, 4));
        entries.add(new Entry(1, 5));
        entries.add(new Entry(2, 6));
        entries.add(new Entry(3, 7));
        entries.add(new Entry(0, 8));
        entries.add(new Entry(1, 9));
        entries.add(new Entry(1, 10));
        entries.add(new Entry(2, 11));
        for (int i = 2; i < 13; i++) {
            entries.add(new Entry(i, 10 + i));
        }

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < 23; i++) {
            xVals.add(String.valueOf(i));
        }
        LineData data = new LineData(xVals, new LineDataSet(entries, "Progress"));
        mLineChart.setData(data);

        return rootView;
    }
}
