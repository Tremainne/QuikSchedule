package group22.quikschedule.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by kris on 10/22/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events.db";

    public DatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                " (" + DatabaseContract.DatabaseEntry.COLUMN_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.DatabaseEntry.COLUMN_ID + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.COLUMN_WEEK + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.COLUMN_DAY + " TEXT NOT NULL, " +
                DatabaseContract.DatabaseEntry.COLUMN_DATA + " TEXT NOT NULL);"
        );
    }

    /**
     *
     * @param cursor
     * @return
     */
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

    /**
     *
     * @param mContext
     * @return
     */
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


    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * 
     * @param mContext
     * @param sql
     * @return
     */
    public static PriorityQueue<EventView> getEvents(Context mContext, String sql) {

        Log.d("Entered", "getEvents");
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(sql, null);

        PriorityQueue<EventView> events = new PriorityQueue<>(10, new Comparator<EventView>() {
            public int compare(EventView event1, EventView event2) {


                int duration1 = event1.getTimeAsInt(EventView.ENDTIME)-event1.getTimeAsInt(EventView.STARTTIME);
                int duration2 = event2.getTimeAsInt(EventView.ENDTIME)-event2.getTimeAsInt(EventView.STARTTIME);

                return (duration1 > duration2) ? -1 : 1;
            }
        });

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String json = cursor.getString(
                            cursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_DATA));
                    System.err.println(json);
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(json);

                        final EventView event = new EventView(mContext);

                        try {
                            event.name = jsonObject.getString("summary");
                            event.id = jsonObject.getString("id");
                            event.location = jsonObject.getString("location");
                            event.description = jsonObject.getString("description");

                            Log.d("JSON","PLS");
                            JSONObject time = jsonObject.getJSONObject("start");
                            Log.d("JSON", time.toString());
                            event.startTime = time.getString("dateTime");
                            time = jsonObject.getJSONObject("end");
                            event.endTime = time.getString("dateTime");
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        events.add(event);

                    } catch (JSONException e) {
                    }
                } while (cursor.moveToNext());
            }
        }

        cursor.close();

        return events;
    }
}
