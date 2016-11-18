package group22.quikschedule.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by kris on 10/22/16.
 */

public class SyncCalendarToSQL extends AsyncTask<Void, Void, Void> {
    private com.google.api.services.calendar.Calendar mService = null;
    private Context mContext;
    private Activity mActivity;
    private static final String SYNC_TOKEN_KEY = "syncToken";

    public SyncCalendarToSQL(GoogleAccountCredential credential, Context context) {
        mContext = context;
        mActivity = (Activity) context;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Quick Calendar")
                .build();

        Toast.makeText(mContext, "Syncing Calendar", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            run();
        } catch (UserRecoverableAuthIOException e) {
            mActivity.startActivityForResult(e.getIntent(), 69);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Override
    protected void onPostExecute (Void result) {
    }

    private void run() throws IOException {
        String calendarId = getCalendarIdFromSummary("QuickSchedule");
        if (calendarId.equals("")) {
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary("QuickSchedule");
            calendar.setTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());
            com.google.api.services.calendar.model.Calendar execute = mService.calendars().insert(calendar).execute();
            calendarId = execute.getId();
        }
        Calendar.Events.List request = mService.events().list(calendarId);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SYNC_TOKEN_KEY, Context.MODE_PRIVATE);
        String syncToken = sharedPreferences.getString(SYNC_TOKEN_KEY, null);

        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        if (isTableEmpty(db)) {
            syncToken = null;
        }

        if (syncToken == null) {
            System.out.println("Performing full sync");

            Date oneYearAgo = getRelativeDate(java.util.Calendar.YEAR, -1);
            request.setTimeMin(new DateTime(oneYearAgo, TimeZone.getTimeZone("UTC")));
        } else {
            System.out.println("Performing incremental sync");
            request.setSyncToken(syncToken);
        }

        String pageToken = null;
        Events events = null;
        do {
            request.setPageToken(pageToken);

            try {
                events = request.execute();
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 410) {
                    System.out.println("Invalid sync token, clearing event store and re-syncing.");
                    sharedPreferences.edit().putString(SYNC_TOKEN_KEY, null).apply();

                    db.execSQL("DROP DATABASE");
                    db.close();
                    run();
                } else {
                    throw e;
                }
            }

            List<Event> items = events.getItems();
            if (items.size() == 0) {
                System.out.println("No new items to sync");
            } else {
                for (Event event : items) {
                    syncEvent(event);
                    System.err.println(event.toPrettyString());
                }
            }

            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        db.close();
        String token = events.getNextSyncToken();
        sharedPreferences.edit().putString(SYNC_TOKEN_KEY, token).apply();
        System.err.println(token);
        System.out.println("Sync complete");
    }

    private void syncEvent(Event event) throws IOException {
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        if ("cancelled".equals(event.getStatus()) && isIdInTable(db, event.getId())) {
            String sql ="DELETE FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                    " WHERE " + DatabaseContract.DatabaseEntry.COLUMN_ID +
                    "=" + "'" + event.getId() + "'";

            db.execSQL(sql);
            db.close();

            System.out.println(String.format("Deleting event: ID=%s", event.getId()));
        } else {

            EventDateTime edt = event.getStart();
            DateTime dt = edt.getDateTime();
            CharSequence cs = DateFormat.format("EEEE", dt.getValue());
            final StringBuilder sb = new StringBuilder(cs.length());
            sb.append(cs);
            String day = sb.toString().toUpperCase();

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(dt.getValue());
            int weekNum = cal.get(java.util.Calendar.WEEK_OF_YEAR);

            int numOfWeeks = 0;
            try {
                JSONObject jsonObject = new JSONObject(event.toString());
                JSONArray recurrenceArray = jsonObject.getJSONArray("recurrence");
                String recurrenceString = recurrenceArray.get(0).toString();
                numOfWeeks = 0;
                if (recurrenceString.contains("WEEKLY") && recurrenceString.contains("COUNT")) {
                    numOfWeeks = Integer.parseInt(recurrenceString.substring(recurrenceString.length() - 2, recurrenceString.length()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            do {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.DatabaseEntry.COLUMN_ID, event.getId());
                values.put(DatabaseContract.DatabaseEntry.COLUMN_WEEK, weekNum + numOfWeeks);
                values.put(DatabaseContract.DatabaseEntry.COLUMN_DAY, day);
                values.put(DatabaseContract.DatabaseEntry.COLUMN_DATA, event.toPrettyString());
                db.insert(DatabaseContract.DatabaseEntry.TABLE_NAME, null, values);

                numOfWeeks--;
            } while (numOfWeeks >= 0);
            db.close();



            System.out.println(
                    String.format("Syncing event: ID=%s, Name=%s", event.getId(), event.getSummary()));
        }
    }

    private Date getRelativeDate(int field, int amount) {
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.add(field, amount);
        return cal.getTime();
    }

    private boolean isIdInTable (SQLiteDatabase db, String id) {
        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.DatabaseEntry.COLUMN_ID + "=" +
                "'" + id + "'";
        Cursor cursor = db.rawQuery(sql, null);

        boolean inTable = cursor.getCount() <= 0;
        cursor.close();
        return inTable;
    }

    private boolean isTableEmpty (SQLiteDatabase db) {
        try {
            String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME;
            Cursor cursor = db.rawQuery(sql, null);

            boolean isEmpty = cursor.getCount() <= 0;
            cursor.close();
            return isEmpty;
        } catch (SQLiteException e) {
            return true;
        }
    }

    private String getCalendarIdFromSummary (String summary) {
        try {
            String pageToken = null;
            do {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals(summary)) {
                        return calendarListEntry.getId();
                    }
                }

                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }
}
