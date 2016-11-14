package group22.quikschedule.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group22.quikschedule.NavigationDrawerActivity;

/**
 * Created by kris on 10/30/16.
 */

public class SyncFirebaseToCalendar extends AsyncTask<Void, Void, ArrayList<Event>> {

    public ArrayList<Event> events = new ArrayList<Event>();
    public boolean syncing = true;


    private Context mContext;
    private Activity mActivity;
    private com.google.api.services.calendar.Calendar mService = null;
    private GoogleAccountCredential mCredential;

    private final String TAG = "SyncFirebaseToCalendar";

    private String android_id;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference db;

    public SyncFirebaseToCalendar (GoogleAccountCredential credential, Context context) {
        System.err.println("SYNCING FIREBASE TO CALENDAR");


        final String[] SCOPES = {CalendarScopes.CALENDAR};
        mContext = context;
        mActivity = (Activity) context;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mCredential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Quick Calendar")
                .build();

        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //Device ID

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected ArrayList<Event> doInBackground(Void... params) {
        ArrayList<Event> events = new ArrayList<Event>();


        db.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currClassName;
                int i = 0;

                currClassName = (String) dataSnapshot.child(android_id).child("class_" + i)
                        .child("class").getValue();

                while (currClassName != null) {
                    System.err.println(currClassName);
                    DataSnapshot snapshot = dataSnapshot.child(android_id).child("class_" + i);

                    String className = (String) snapshot.child("class").getValue();
                    String classType = (String) snapshot.child("classType").getValue();
                    String startTime = (String) snapshot.child("startTime").getValue();
                    String endTime = (String) snapshot.child("endTime").getValue();
                    String day = (String) snapshot.child("day").getValue();
                    String location = (String) snapshot.child("location").getValue();
                    String section = (String) snapshot.child("section").getValue();
                    String textBooks = "";

                    int textBookNum = 0;
                    String currTextBook = (String) snapshot.child("textbook_" + textBookNum).getValue();
                    String currAuthor = (String) snapshot.child("author_" + textBookNum).getValue();
                    while (currTextBook != null) {
                        textBooks += " : " + currTextBook + " by " + currAuthor;

                        textBookNum++;
                        currTextBook = (String) snapshot.child("textbook_" + textBookNum).getValue();
                        currAuthor = (String) snapshot.child("author_" + textBookNum).getValue();
                    }


                    if (startTime.substring(1,2).equals(":")) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("0");
                        builder.append(startTime);
                        startTime = builder.toString();
                    }
                    if (endTime.substring(1,2).equals(":")) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("0");
                        builder.append(endTime);
                        endTime = builder.toString();
                    }

                    Event event = new Event()
                            .setSummary(className + " - " + classType)
                            .setLocation(location)
                            .setDescription("Section: " + section + "\n"
                                            + "Textbooks" + textBooks);

                    String[] recurrence = new String[]{"RRULE:FREQ=WEEKLY;COUNT=11"};
                    event.setRecurrence(Arrays.asList(recurrence));

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.WEEK_OF_YEAR, 39);

                    cal.set(java.util.Calendar.MINUTE, Integer.parseInt(startTime.substring(3,5)));
                    cal.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek(day));
                    if (startTime.substring(startTime.length() - 2, startTime.length()).equals("pm")) {
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 12 + Integer.parseInt(startTime.substring(0, 2)));
                    } else {
                        cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(startTime.substring(0, 2)));
                    }
                    DateTime start = new DateTime(cal.getTimeInMillis());

                    cal.set(java.util.Calendar.MINUTE, Integer.parseInt(endTime.substring(3,5)));
                    if (endTime.substring(endTime.length() - 2, endTime.length()).equals("pm")) {
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 12 + Integer.parseInt(endTime.substring(0, 2)));
                    } else {
                        cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(endTime.substring(0, 2)));
                    }
                    DateTime end = new DateTime(cal.getTimeInMillis());

                    EventDateTime eventStart = new EventDateTime();
                    eventStart.setDateTime(start);
                    eventStart.setTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());

                    EventDateTime eventEnd = new EventDateTime();
                    eventEnd.setDateTime(end);
                    eventEnd.setTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());

                    event.setStart(eventStart);
                    event.setEnd(eventEnd);

                    SyncFirebaseToCalendar.this.events.add(event);

                    i++;
                    currClassName = (String) dataSnapshot.child(android_id).child("class_" + i)
                            .child("class").getValue();
                }

                SyncFirebaseToCalendar.this.syncing = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }

            public int dayOfWeek (String day) {
                day = day.toUpperCase();
                int num = 0;
                switch (day) {
                    case "SUNDAY":
                        num = 1;
                        break;
                    case "MONDAY":
                        num = 2;
                        break;
                    case "TUESDAY":
                        num = 3;
                        break;
                    case "WEDNESDAY":
                        num = 4;
                        break;
                    case "THURSDAY":
                        num = 5;
                        break;
                    case "FRIDAY":
                        num = 6;
                        break;
                    case "SATURDAY":
                        num = 7;
                        break;
                }
                return num;
            }
        });

        while (syncing) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.err.println("POST EXECUTE");
        for (Event event : SyncFirebaseToCalendar.this.events) {
            try {
                System.err.println("SUCCESS: " + mService.events().insert(getCalendarIdFromSummary("QuickSchedule"), event).execute());
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        return events;
    }

    @Override
    protected void onPostExecute (ArrayList<Event> result) {
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