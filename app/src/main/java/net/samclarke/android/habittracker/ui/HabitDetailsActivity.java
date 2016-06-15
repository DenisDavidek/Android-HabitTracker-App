package net.samclarke.android.habittracker.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.samclarke.android.habittracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HabitDetailsActivity extends AppCompatActivity {
    @BindView((R.id.toolbar)) Toolbar mToolbar;

    public static final String EXTRA_HABIT_ID = "habitId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
