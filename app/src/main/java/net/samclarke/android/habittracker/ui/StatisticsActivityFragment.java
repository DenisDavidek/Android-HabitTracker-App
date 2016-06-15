package net.samclarke.android.habittracker.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import net.samclarke.android.habittracker.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivityFragment extends Fragment {
    @BindView(R.id.streaks_chart) HorizontalBarChart mStreaksChart;
    @BindView(R.id.current_streaks_chart) HorizontalBarChart mCurrentStreaksChart;
    @BindView(R.id.goals_completed) TextView mGoalsCompleted;
    @BindView(R.id.goals_remaining) TextView mGoalsRemaining;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        ButterKnife.bind(this, rootView);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(12, 0));
        entries.add(new BarEntry(7, 1));

        List<BarEntry> current = new ArrayList<>();
        current.add(new BarEntry(8, 0));
        current.add(new BarEntry(3, 1));

        ArrayList<String> xVals = new ArrayList<>();
        xVals.add("Habit Name");
        xVals.add("Exercise more");

        mStreaksChart.setData(new BarData(xVals, new BarDataSet(entries, "Longest Streaks")));
        mCurrentStreaksChart.setData(new BarData(xVals, new BarDataSet(current, "Current Streak")));

        mGoalsCompleted.setText("Completed 3 goals");
        mGoalsRemaining.setText("Got 7 goals remainnig");

        return rootView;
    }
}
