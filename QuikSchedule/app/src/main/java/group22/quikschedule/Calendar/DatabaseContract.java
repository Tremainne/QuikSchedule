package group22.quikschedule.Calendar;

import android.provider.BaseColumns;

/**
 * Created by kris on 10/22/16.
 */

public final class DatabaseContract {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private DatabaseContract() {}

    public static class DatabaseEntry implements BaseColumns {
        public static final String TABLE_NAME = "Events";
        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_WEEK = "Week";
        public static final String COLUMN_DAY = "Day";
        public static final String COLUMN_DATA = "Json";
    }
}