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

public class GoalsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_goals, container, false);

        ButterKnife.bind(this, rootView);

        ((LinearLayout)rootView.findViewById(R.id.container)).addView(createGoalView(inflater, "Habit Name", "Completed 3 days out of 30"));
        ((LinearLayout)rootView.findViewById(R.id.container)).addView(createGoalView(inflater, "Exercise", "Completed 13 hours out of 100"));

        return rootView;
    }

    View createGoalView(LayoutInflater inflater, String name, String progress) {
        View view = inflater.inflate(R.layout.list_item_goal, null, false);

        TextView habitName = (TextView)view.findViewById(R.id.habit_name);
        TextView goalProgress = (TextView)view.findViewById(R.id.goal_progress);

        goalProgress.setText(progress);
        habitName.setText(name);

        return view;
    }
}