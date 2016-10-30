package group22.quikschedule.Calendar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kris on 10/22/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events.db";

    public DatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                " (" + DatabaseContract.DatabaseEntry.COLUMN_ID + " TEXT PRIMARY KEY, " +
                DatabaseContract.DatabaseEntry.COLUMN_WEEK + " INT NOT NULL, " +
                DatabaseContract.DatabaseEntry.COLUMN_DAY + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.COLUMN_DATA + " TEXT NOT NULL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
