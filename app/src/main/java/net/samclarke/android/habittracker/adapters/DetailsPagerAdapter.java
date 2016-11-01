package net.samclarke.android.habittracker.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.ui.DetailsFragment;
import net.samclarke.android.habittracker.ui.GoalsFragment;
import net.samclarke.android.habittracker.ui.RemindersFragment;

import java.util.ArrayList;

public class DetailsPagerAdapter extends FragmentPagerAdapter {
    private final ArrayList<Pair<String, Fragment>> mTabs = new ArrayList<>();

    public DetailsPagerAdapter(FragmentManager fragmentManager, Context context, int habitId) {
        super(fragmentManager);

        mTabs.add(Pair.create(context.getString(R.string.tab_habit_details),
                (Fragment) DetailsFragment.newInstance(habitId)));

        mTabs.add(Pair.create(context.getString(R.string.tab_habit_goals),
                (Fragment) GoalsFragment.newInstance(habitId)));

        mTabs.add(Pair.create(context.getString(R.string.tab_habit_reminders),
                (Fragment) RemindersFragment.newInstance(habitId)));
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mTabs.get(position).second;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return mTabs.get(position).first;
    }
}
