package net.samclarke.android.habittracker.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.samclarke.android.habittracker.provider.HabitsContract.*;

public class HabitsOpenHelper extends SQLiteOpenHelper {
    public static final String NAME = "HabitsDB";
    public static final int VERSION = 1;
    private static HabitsOpenHelper sInstance;


    private HabitsOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    public static synchronized HabitsOpenHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HabitsOpenHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + HabitEntry.TABLE_NAME + " (" +
                HabitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                HabitEntry.COLUMN_NAME + " TEXT NOT NULL UNIQUE," +
                HabitEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                HabitEntry.COLUMN_START_DATE + " INTEGER NOT NULL," +
                HabitEntry.COLUMN_IS_ARCHIVED + " INTEGER NOT NULL DEFAULT 0," +
                HabitEntry.COLUMN_COLOR + " INTEGER NOT NULL," +
                HabitEntry.COLUMN_FREQUENCY + " INTEGER NOT NULL," +
                HabitEntry.COLUMN_FREQUENCY_VALUE + " INTEGER NOT NULL," +
                HabitEntry.COLUMN_TARGET + " REAL NOT NULL," +
                HabitEntry.COLUMN_TARGET_TYPE + " INTEGER NOT NULL," +
                HabitEntry.COLUMN_TARGET_OPERATOR + " INTEGER NOT NULL," +
                "UNIQUE(" + HabitEntry.COLUMN_NAME + ") " +
            ")");

        db.execSQL("CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReminderEntry.COLUMN_IS_ENABLED + " INTEGER NOT NULL DEFAULT 0," +
                ReminderEntry.COLUMN_FREQUENCY + " INTEGER NOT NULL," +
                ReminderEntry.COLUMN_FREQUENCY_VALUE + " INTEGER NOT NULL," +
                ReminderEntry.COLUMN_TIME + " INTEGER," +
                ReminderEntry.COLUMN_GEO_LOCATION_NAME + " TEXT," +
                ReminderEntry.COLUMN_GEO_RADIUS + " INTEGER," +
                ReminderEntry.COLUMN_GEO_LAT + " REAL," +
                ReminderEntry.COLUMN_GEO_LONG + " REAL," +
                ReminderEntry.COLUMN_GEO_TYPE + " INTEGER," +
                ReminderEntry.COLUMN_HABIT_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + ReminderEntry.COLUMN_HABIT_ID + ") " +
                    "REFERENCES " + HabitEntry.TABLE_NAME + " (" + HabitEntry._ID + ") " +
                    "ON DELETE CASCADE" +
                ")");

        db.execSQL("CREATE TABLE " + GoalEntry.TABLE_NAME + " (" +
                GoalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GoalEntry.COLUMN_NAME + " TEXT NOT NULL," +
                GoalEntry.COLUMN_TARGET + " INTEGER NOT NULL," +
                GoalEntry.COLUMN_START_DATE + " INTEGER NOT NULL," +
                GoalEntry.COLUMN_PROGRESS + " INTEGER NOT NULL," +
                GoalEntry.COLUMN_HABIT_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + GoalEntry.COLUMN_HABIT_ID + ") " +
                    "REFERENCES " + HabitEntry.TABLE_NAME + " (" + HabitEntry._ID + ") " +
                    "ON DELETE CASCADE" +
            ")");

        db.execSQL("CREATE TABLE " + CheckInEntry.TABLE_NAME + " (" +
                CheckInEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CheckInEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                CheckInEntry.COLUMN_STATUS + " INTEGER NOT NULL," +
                CheckInEntry.COLUMN_IS_AUTO_STATUS + " INTEGER NOT NULL DEFAULT 1," +
                CheckInEntry.COLUMN_VALUE + " REAL NOT NULL," +
                CheckInEntry.COLUMN_HABIT_ID + " INTEGER NOT NULL," +
                CheckInEntry.COLUMN_NOTE + " TEXT," +
                "UNIQUE(" + CheckInEntry.COLUMN_DATE + ", " + CheckInEntry.COLUMN_HABIT_ID + ") " +
                    "ON CONFLICT REPLACE," +
                "FOREIGN KEY (" + CheckInEntry.COLUMN_HABIT_ID + ") " +
                    "REFERENCES " + HabitEntry.TABLE_NAME + " (" + HabitEntry._ID + ") " +
                    "ON DELETE CASCADE" +
            ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: implement when updating DB
    }
}
