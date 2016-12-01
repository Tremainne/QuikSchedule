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
 * Class: DatabaseHelper
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/5/16
 *
 * Description: This class has a bunch of methods that intract with the devices database and allows
 *              us to push and pull data from the database
 *
 * @author Kris Rau
 * @author Rohan Chhabra
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events.db";

    public DatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Description:
     * @param
     * @return
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
     * Description:
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     * @return void
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     * @return void
     */
    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Description: Performs an SQL query based upon the input sql string and returns all the
     * events from the SQL database in the form of EventViews. They are returned within a
     * PriorityQueue based on the duration of the event.
     *
     * @param mContext context to initialize EventView
     * @param sql sql string for query
     * @return PriorityQueue<EventView> Priority queue that contains the events in the format of
     *                                  EventView based on the input SQL string
     */
    public static PriorityQueue<EventView> getEvents(Context mContext, String sql) {

        // prepares database for query
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // performs SQL query
        Cursor cursor = db.rawQuery(sql, null);

        // creates a PriorityQueue that gives high priority to the longer events.
        PriorityQueue<EventView> events = new PriorityQueue<>(10, new Comparator<EventView>() {
            public int compare(EventView event1, EventView event2) {


                int duration1 = event1.getTimeAsInt(EventView.ENDTIME)-
                                event1.getTimeAsInt(EventView.STARTTIME);
                int duration2 = event2.getTimeAsInt(EventView.ENDTIME)-
                                event2.getTimeAsInt(EventView.STARTTIME);

                return (duration1 > duration2) ? -1 : 1;
            }
        });

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                // iterates through the events in the database and converts to EventView
                do {
                    String json = cursor.getString(
                            cursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_DATA));
                    System.err.println(json);
                    JSONObject jsonObject;
                    try {

                        //get event as JSONObject for conversion
                        jsonObject = new JSONObject(json);

                        final EventView event = new EventView(mContext);

                        try {

                            // populates the EventView fields from the JSONObject Strings
                            event.name = jsonObject.getString("summary");
                            event.id = jsonObject.getString("id");
                            event.location = jsonObject.getString("location");

                            JSONObject time = jsonObject.getJSONObject("start");
                            event.startTime = time.getString("dateTime");
                            time = jsonObject.getJSONObject("end");
                            event.endTime = time.getString("dateTime");

                            // JSONObject description contains the materials, comments and
                            // transportation method separated by newline. The following lines
                            // splits the description by newline and stores it in EventView.
                            String[] lines = jsonObject.getString("description").split("\n");
                            event.materials = lines[0].substring(0, lines[0].length());
                            event.comments = lines[1].substring(0, lines[1].length());
                            event.transportation =
                                    Integer.parseInt(lines[2].substring(0, lines[2].length()));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //adds event to the PriorityQueue
                        events.add(event);

                    } catch (JSONException e) {
                    }
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        return events;
    }
}
