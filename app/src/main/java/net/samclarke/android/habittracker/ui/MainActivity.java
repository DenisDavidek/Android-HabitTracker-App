package net.samclarke.android.habittracker.ui;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.sync.SyncAdapter;
import net.samclarke.android.habittracker.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyncAdapter.initializeSyncAdapter(this);

        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HabitsFragment())
                .commit();

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar,
                R.string.drawer_open,  R.string.drawer_close);

        mDrawer.addDrawerListener(mDrawerToggle);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });

        mNavigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5,
                                       int i6, int i7) {

                mNavigationView.removeOnLayoutChangeListener( this );

                TextView header = (TextView) mNavigationView.findViewById(R.id.quote_of_the_day);
                if (header != null) {
                    header.setText(UIUtils.getQuoteOfDay(MainActivity.this));
                }
            }
        });
    }

    public void selectDrawerItem(MenuItem item) {
        Fragment fragment;

        switch (item.getItemId()) {
            case R.id.nav_goals_page:
                fragment = new GoalsFragment();
                break;
            case R.id.nav_reminders_page:
                fragment = new RemindersFragment();
                break;
            case R.id.nav_statistics_page:
                fragment = new StatisticsFragment();
                break;
            case R.id.nav_habits_page:
            default:
                fragment = new HabitsFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        item.setChecked(true);

        setTitle(item.getTitle());

        mDrawer.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
