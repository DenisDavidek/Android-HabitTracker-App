package net.samclarke.android.habittracker.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.adapters.DetailsPagerAdapter;
import net.samclarke.android.habittracker.provider.HabitsContract.HabitEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HabitActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;

    public interface FabFragment {
        void OnFabClick();
    }

    public static final String EXTRA_HABIT_ID = "habit_id";

    private static final String[] PROJECTION = new String[]{
            HabitEntry.COLUMN_NAME
    };

    private static final int COLUMN_NAME = 0;
    private static final int HABIT_LOADER_ID = 1;
    private DetailsPagerAdapter mDetailsPagerAdapter;
    private boolean mShouldShowFab = false;
    private int mCurrentPage = 0;
    private int mHabitId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mHabitId = getIntent().getIntExtra(EXTRA_HABIT_ID, -1);

        mDetailsPagerAdapter = new DetailsPagerAdapter(getSupportFragmentManager(), this, mHabitId);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int page, float positionOffset, int positionOffsetPixels) {
                float offset = positionOffset;
                if (page < mCurrentPage) {
                    offset = 1 - positionOffset;
                }

                if (!mShouldShowFab || offset > 0.05) {
                    mFab.hide();
                } else {
                    mFab.show();
                }
            }

            @Override
            public void onPageSelected(int page) {
                mCurrentPage = page;
                mShouldShowFab = (mDetailsPagerAdapter.getItem(page) instanceof FabFragment);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        mViewPager.setAdapter(mDetailsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        getSupportLoaderManager().initLoader(HABIT_LOADER_ID, null, this);
    }

    @OnClick(R.id.fab)
    void OnFabClick() {
        Fragment fragment = mDetailsPagerAdapter.getItem(mViewPager.getCurrentItem());

        if (fragment instanceof FabFragment) {
            ((FabFragment) fragment).OnFabClick();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri habitUri = ContentUris.withAppendedId(HabitEntry.CONTENT_URI, mHabitId);

        return new CursorLoader(this, habitUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            setTitle(cursor.getString(COLUMN_NAME));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
