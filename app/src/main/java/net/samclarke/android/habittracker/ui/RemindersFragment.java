package net.samclarke.android.habittracker.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.samclarke.android.habittracker.R;

import butterknife.ButterKnife;

public class RemindersFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminders, container, false);

        ButterKnife.bind(this, rootView);

        ((LinearLayout)rootView).addView(createReminderView(inflater, "Habit Name", "Any", "Someplace"));
        ((LinearLayout)rootView).addView(createReminderView(inflater, "Exercise", "12:00", "Any"));

        return rootView;
    }

    View createReminderView(LayoutInflater inflater, String name, String time, String location) {
        View view = inflater.inflate(R.layout.list_item_reminder, null, false);

        TextView habitName = (TextView)view.findViewById(R.id.habit_name);
        TextView reminderTime = (TextView)view.findViewById(R.id.reminder_time);
        TextView reminderLocation = (TextView)view.findViewById(R.id.reminder_location);

        reminderTime.setText(time);
        reminderLocation.setText(location);
        habitName.setText(name);

        return view;
    }
}
