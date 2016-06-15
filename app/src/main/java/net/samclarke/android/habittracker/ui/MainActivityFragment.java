package net.samclarke.android.habittracker.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.WeekView;

import net.samclarke.android.habittracker.R;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, rootView);

//        ((LinearLayout)rootView).addView(createHabitView(inflater, "Habit Name"));
//        ((LinearLayout)rootView).addView(createHabitView(inflater, "Exercise more"));

        return rootView;
    }

    View createHabitView(LayoutInflater inflater, String name) {
        View view = inflater.inflate(R.layout.list_item_habit, null, false);

        TextView habitName = (TextView)view.findViewById(R.id.habit_name);

        habitName.setText(name);

        MaterialCalendarView calendarView =
                (MaterialCalendarView)view.findViewById(R.id.calendar_view);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);

        calendarView.state().edit()
                .setMaximumDate(CalendarDay.today())
                .setMinimumDate(calendar)
                .setFirstDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();

        calendarView.setTopbarVisible(false);

        return view;
    }
}
