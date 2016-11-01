package net.samclarke.android.habittracker.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class HabitsContract {
    public static final String CONTENT_AUTHORITY = "net.samclarke.android.habittracker.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static class HabitEntry implements BaseColumns {
        public static final String PATH = "habits";
        public static final String WITH_STATUS_PATH = "with_days";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final Uri CONTENT_WITH_STATUS_URI =
                CONTENT_URI.buildUpon().appendPath(WITH_STATUS_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI  + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH;

        public static final int FREQUENCY_DAILY = 0;
        public static final int FREQUENCY_WEEKLY = 1;
        public static final int FREQUENCY_MONTHLY = 2;
        public static final int FREQUENCY_INTERVAL = 3;

        public static final int TARGET_TYPE_REPETITIONS = 0;
        public static final int TARGET_TYPE_MINUTES = 1;
        public static final int TARGET_TYPE_METRES = 2;
        public static final int TARGET_TYPE_KILOGRAMS = 3;
        public static final int TARGET_TYPE_LITRES = 4;
        public static final int TARGET_TYPE_NOTES = 5;

        public static final int TARGET_OPERATOR_NONE = 0;
        public static final int TARGET_OPERATOR_GREATER = 1;
        public static final int TARGET_OPERATOR_GREATER_EQUAL = 2;
        public static final int TARGET_OPERATOR_EQUAL = 3;
        public static final int TARGET_OPERATOR_LESS_EQUAL = 4;
        public static final int TARGET_OPERATOR_LESS = 5;


        public static final String TABLE_NAME = "habits";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_IS_ARCHIVED = "is_archived";

        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";

        public static final String COLUMN_TARGET = "target";
        public static final String COLUMN_TARGET_TYPE = "target_type";
        public static final String COLUMN_TARGET_OPERATOR = "target_operator";

        public static final String VIRTUAL_COLUMN_DAY0_STATUS = "status_day0";
        public static final String VIRTUAL_COLUMN_DAY1_STATUS = "status_day1";
        public static final String VIRTUAL_COLUMN_DAY2_STATUS = "status_day2";
        public static final String VIRTUAL_COLUMN_DAY3_STATUS = "status_day3";
        public static final String VIRTUAL_COLUMN_DAY4_STATUS = "status_day4";
        public static final String VIRTUAL_COLUMN_DAY5_STATUS = "status_day5";
        public static final String VIRTUAL_COLUMN_DAY6_STATUS = "status_day6";
    }

    public static class GoalEntry implements BaseColumns {
        public static final String PATH = "goals";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI  + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH;

        public static final String TABLE_NAME = "goals";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_PROGRESS = "progress";
        public static final String COLUMN_TARGET = "target";
        public static final String COLUMN_HABIT_ID = "habit_id";
    }

    public static class CheckInEntry implements BaseColumns {
        public static final String PATH = "check_ins";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI  + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH;

        public static final int STATUS_NONE = 0;
        public static final int STATUS_COMPLETE = 1;
        public static final int STATUS_SKIPPED = 2;
        public static final int STATUS_FAILED = 3;


        public static final String TABLE_NAME = "check_ins";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_IS_AUTO_STATUS = "is_auto_status";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_HABIT_ID = "habit_id";
    }

    public static class ReminderEntry implements BaseColumns {
        public static final String PATH = "reminders";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI  + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH;

        public static final int FREQUENCY_DAILY = 0;
        public static final int FREQUENCY_WEEKLY = 1;

        public static final int GEOFENCE_TRANSITION_ENTER = 0;
        public static final int GEOFENCE_TRANSITION_DWELL = 1;
        public static final int GEOFENCE_TRANSITION_LEAVE = 2;


        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_IS_ENABLED = "is_enabled";
        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_GEO_RADIUS = "geo_fence_radius";
        public static final String COLUMN_GEO_LOCATION_NAME = "geo_fence_location_name";
        public static final String COLUMN_GEO_LAT = "geo_fence_lat";
        public static final String COLUMN_GEO_LONG = "geo_fence_long";
        public static final String COLUMN_GEO_TYPE = "geo_fence_type";
        public static final String COLUMN_HABIT_ID = "habit_id";
    }
}
