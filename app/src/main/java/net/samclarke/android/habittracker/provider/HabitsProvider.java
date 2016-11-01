package net.samclarke.android.habittracker.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.samclarke.android.habittracker.provider.HabitsContract.*;

import java.util.HashMap;
import java.util.Map;

import static net.samclarke.android.habittracker.provider.HabitsContract.CONTENT_AUTHORITY;

public class HabitsProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private HabitsOpenHelper mOpenHelper;

    private static final int HABIT = 100;
    private static final int HABITS = 101;
    private static final int HABITS_WITH_STATUS = 102;
    private static final int REMINDER = 200;
    private static final int REMINDERS = 201;
    private static final int CHECK_IN = 300;
    private static final int CHECK_INS = 301;
    private static final int GOAL = 400;
    private static final int GOALS = 401;

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, HabitEntry.PATH + "/#", HABIT);
        sUriMatcher.addURI(CONTENT_AUTHORITY, HabitEntry.PATH + "/", HABITS);
        sUriMatcher.addURI(CONTENT_AUTHORITY,
                HabitEntry.PATH + "/" + HabitEntry.WITH_STATUS_PATH + "/", HABITS_WITH_STATUS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, ReminderEntry.PATH + "/#", REMINDER);
        sUriMatcher.addURI(CONTENT_AUTHORITY, ReminderEntry.PATH + "/", REMINDERS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, CheckInEntry.PATH + "/#", CHECK_IN);
        sUriMatcher.addURI(CONTENT_AUTHORITY, CheckInEntry.PATH + "/", CHECK_INS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, GoalEntry.PATH + "/#", GOAL);
        sUriMatcher.addURI(CONTENT_AUTHORITY, GoalEntry.PATH + "/", GOALS);
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = HabitsOpenHelper.getInstance(getContext());

        return true;
    }

    private String getTable(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case HABIT:
            case HABITS:
                return HabitEntry.TABLE_NAME;
            case REMINDER:
            case REMINDERS:
                return ReminderEntry.TABLE_NAME;
            case CHECK_IN:
            case CHECK_INS:
                return CheckInEntry.TABLE_NAME;
            case GOAL:
            case GOALS:
                return GoalEntry.TABLE_NAME;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private String addIdToSelection(@NonNull Uri uri, String selection) {
        switch (sUriMatcher.match(uri)) {
            case HABIT:
            case REMINDER:
            case CHECK_IN:
            case GOAL:
                String idSelection = "_ID = " + ContentUris.parseId(uri);

                if (selection != null && !selection.isEmpty()) {
                    return idSelection + " AND " + selection;
                }

                return idSelection;
        }

        return selection;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        switch (sUriMatcher.match(uri)) {
            case HABITS_WITH_STATUS:
                return queryHabitsWithStatus(projection, selection, selectionArgs, sortOrder);
            case HABIT:
            case HABITS:
            case REMINDER:
            case REMINDERS:
            case CHECK_IN:
            case CHECK_INS:
            case GOAL:
            case GOALS:
                Cursor cursor = mOpenHelper.getWritableDatabase().query(
                        getTable(uri),
                        projection,
                        addIdToSelection(uri, selection),
                        selectionArgs,
                        null, // group by
                        null, // having
                        sortOrder);

                if (getContext() != null) {
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                }

                return cursor;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case HABIT:
                return HabitEntry.CONTENT_ITEM_TYPE;
            case HABITS:
            case HABITS_WITH_STATUS:
                return HabitEntry.CONTENT_TYPE;
            case REMINDER:
                return ReminderEntry.CONTENT_ITEM_TYPE;
            case REMINDERS:
                return ReminderEntry.CONTENT_TYPE;
            case CHECK_IN:
                return CheckInEntry.CONTENT_ITEM_TYPE;
            case CHECK_INS:
                return CheckInEntry.CONTENT_TYPE;
            case GOAL:
                return GoalEntry.CONTENT_ITEM_TYPE;
            case GOALS:
                return GoalEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case CHECK_INS:
            case HABITS:
            case REMINDERS:
            case GOALS:
                long id = mOpenHelper.getWritableDatabase().insert(getTable(uri), null, values);

                if (id <= 0) {
                    throw new RuntimeException("Unable to insert rows into: " + uri);
                }

                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return ContentUris.withAppendedId(uri, id);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case CHECK_IN:
            case CHECK_INS:
            case HABIT:
            case HABITS:
            case REMINDER:
            case REMINDERS:
            case GOAL:
            case GOALS:
                int rows = mOpenHelper.getWritableDatabase()
                        .delete(getTable(uri), addIdToSelection(uri, selection), selectionArgs);

                // null selection will delete all rows
                if (selection == null || rows > 0) {
                    if (getContext() != null) {
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }

                return rows;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {
            case CHECK_IN:
            case CHECK_INS:
            case HABIT:
            case HABITS:
            case REMINDER:
            case REMINDERS:
            case GOAL:
            case GOALS:
                int rows = mOpenHelper.getWritableDatabase().update(getTable(uri), values,
                        addIdToSelection(uri, selection), selectionArgs);

                if (rows > 0 && getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rows;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private String createDayStatusColumn(String name, int day) {
        final String date = CheckInEntry.TABLE_NAME + "." + CheckInEntry.COLUMN_DATE;
        final String status = CheckInEntry.TABLE_NAME + "." + CheckInEntry.COLUMN_STATUS;
        final String subtractDays = (day == 0) ? "" : ",'-" + String.valueOf(day) + " day'";

        return "MAX(CASE WHEN " +
                "DATE(" + date + ", 'unixepoch', 'localtime') = date('now'" + subtractDays + ") " +
                "THEN " + status + " END) AS " + name;
    }

    private Cursor queryHabitsWithStatus(String[] projection, String selection,
                                         String[] selectionArgs, String sortOrder) {

        final Map<String, String> columnMap = new HashMap<>();

        columnMap.put(HabitEntry._ID, HabitEntry.TABLE_NAME + "." + HabitEntry._ID);
        columnMap.put(HabitEntry.COLUMN_NAME, HabitEntry.COLUMN_NAME);
        columnMap.put(HabitEntry.COLUMN_DESCRIPTION, HabitEntry.COLUMN_DESCRIPTION);
        columnMap.put(HabitEntry.COLUMN_START_DATE, HabitEntry.COLUMN_START_DATE);
        columnMap.put(HabitEntry.COLUMN_COLOR, HabitEntry.COLUMN_COLOR);
        columnMap.put(HabitEntry.COLUMN_FREQUENCY, HabitEntry.COLUMN_FREQUENCY);
        columnMap.put(HabitEntry.COLUMN_FREQUENCY_VALUE, HabitEntry.COLUMN_FREQUENCY_VALUE);
        columnMap.put(HabitEntry.COLUMN_TARGET, HabitEntry.COLUMN_TARGET);
        columnMap.put(HabitEntry.COLUMN_TARGET_TYPE, HabitEntry.COLUMN_TARGET_TYPE);
        columnMap.put(HabitEntry.COLUMN_TARGET_OPERATOR, HabitEntry.COLUMN_TARGET_OPERATOR);
        columnMap.put(HabitEntry.COLUMN_IS_ARCHIVED, HabitEntry.COLUMN_IS_ARCHIVED);

        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY0_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY0_STATUS, 0));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY1_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY1_STATUS, 1));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY2_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY2_STATUS, 2));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY3_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY3_STATUS, 3));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY4_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY4_STATUS, 4));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY5_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY5_STATUS, 5));
        columnMap.put(HabitEntry.VIRTUAL_COLUMN_DAY6_STATUS,
                createDayStatusColumn(HabitEntry.VIRTUAL_COLUMN_DAY6_STATUS, 6));

        final String checkInHabitId = CheckInEntry.TABLE_NAME + "." + CheckInEntry.COLUMN_HABIT_ID;
        final String checkInDate = CheckInEntry.TABLE_NAME + "." + CheckInEntry.COLUMN_DATE;
        final String habitId = HabitEntry.TABLE_NAME + "." + HabitEntry._ID;

        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setProjectionMap(columnMap);
        builder.setTables(HabitEntry.TABLE_NAME +
                " LEFT JOIN " + CheckInEntry.TABLE_NAME +
                " ON " + checkInHabitId + " = " + habitId +
                " AND " + checkInDate +
                " BETWEEN strftime('%s','now','-7 day') AND strftime('%s','now')");

        final Cursor cursor = builder.query(mOpenHelper.getWritableDatabase(), projection,
                selection, selectionArgs, habitId, null, sortOrder);

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), HabitsContract.BASE_CONTENT_URI);
        }

        return cursor;
    }
}
