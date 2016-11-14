package group22.quikschedule.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.api.client.json.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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

    public JSONArray cursorToJson(Cursor cursor)
    {
        JSONArray resultSet     = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            Log.d("TAG_NAME", cursor.getString(i) );
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log.d("TAG_NAME", e.getMessage()  );
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    public ArrayList<JSONArray> getData(Context mContext)
    {
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String response;

        String sql = "SELECT " + DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
                DatabaseContract.DatabaseEntry.TABLE_NAME + "WHERE"; // + " WHERE " +
        //DatabaseContract.DatabaseEntry.COLUMN_DAY; // + " IS MONDAY";


        ArrayList<JSONArray> object_day = new ArrayList<JSONArray>();

        Cursor cursor = db.rawQuery(sql, null); //+ "MONDAY", null);
        object_day.add(cursorToJson(cursor));

        /*cursor = db.rawQuery(sql + "TUESDAY", null);
        object_day.add(cursorToJson(cursor));

        cursor = db.rawQuery(sql + "WEDNESDAY", null);
        object_day.add(cursorToJson(cursor));

        cursor = db.rawQuery(sql + "THURSDAY", null);
        object_day.add(cursorToJson(cursor));

        cursor = db.rawQuery(sql + "FRIDAY", null);
        object_day.add(cursorToJson(cursor));*/

        System.err.println(object_day.get(0));
        return object_day;

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
