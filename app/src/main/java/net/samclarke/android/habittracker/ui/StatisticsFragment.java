package net.samclarke.android.habittracker.ui;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.provider.HabitsContract;
import net.samclarke.android.habittracker.util.CheckInUtils;
import net.samclarke.android.habittracker.util.GoalsUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsFragment extends Fragment {
    @BindView(R.id.goals_stats) TextView mGoalsStats;
    @BindView(R.id.check_in_pie_chart) PieChart mCheckInPieChat;

    private final ValueFormatter integerFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value, Entry entry, int index, ViewPortHandler viewHandler) {
            return String.valueOf((int) value);
        }
    };


    private void reloadCheckInPieChart(CheckInUtils.CheckInStats stats) {
        List<Entry> entries = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (stats.done > 0) {
            xVals.add(getString(R.string.stats_check_ins_done));
            entries.add(new BarEntry(stats.done, 0));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_done));
        }

        if (stats.skipped > 0) {
            xVals.add(getString(R.string.stats_check_ins_skipped));
            entries.add(new BarEntry(stats.skipped, 1));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_skipped));
        }

        if (stats.failed > 0) {
            xVals.add(getString(R.string.stats_check_ins_failed));
            entries.add(new BarEntry(stats.failed, 2));
            colors.add(ContextCompat.getColor(getContext(), R.color.day_failed));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(integerFormatter);

        mCheckInPieChat.setTouchEnabled(false);
        mCheckInPieChat.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        mCheckInPieChat.getLegend().setTextSize(14);
        mCheckInPieChat.setDrawSliceText(false);
        mCheckInPieChat.setTransparentCircleRadius(0);
        mCheckInPieChat.setDescription(null);
        mCheckInPieChat.setData(data);
        mCheckInPieChat.invalidate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        ButterKnife.bind(this, rootView);

        GoalsUtils.GoalStats goalStats = GoalsUtils.getStats(getContext());
        if (goalStats != null) {
            mGoalsStats.setText(getString(R.string.stats_goals_body,
                    goalStats.completed, goalStats.total));
        }

        CheckInUtils.CheckInStats checkInStats = CheckInUtils.getStats(getContext());
        if (checkInStats != null) {
            reloadCheckInPieChart(checkInStats);
        }

        return rootView;
    }
}
