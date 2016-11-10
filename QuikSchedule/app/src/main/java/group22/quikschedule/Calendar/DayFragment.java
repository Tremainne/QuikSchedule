package group22.quikschedule.Calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;

import group22.quikschedule.R;

public class DayFragment extends Fragment {

    private String[] dates;
    private String[] daysOfWeek =
            {" = 'Sunday'", " = 'Monday'", " = 'Tuesday'", " = 'Wednesday'", " = 'Thursday'",
                    " = 'Friday'", " = 'Saturday'"};
    private int mPage;
   /* private PriorityQueue<Event> events = new PriorityQueue<>(15, new Comparator<Event>() {
        @Override
        public int compare(Event a, Event b) {

            return a.startTime-b.startTime;
        }
    });*/

    public static DayFragment newInstance(int page, String[] tabTitles) {
        Bundle args = new Bundle();
        args.putInt("Page", page);
        args.putStringArray("Dates", tabTitles);

        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt("Page");
        dates = getArguments().getStringArray("Dates");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_full_agenda, container, false);

        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(dates[mPage-1]);

        addEvents(view);

        return view;
    }

    public void onResume() {
        super.onResume();

        //pullEventsFromDatabase
        //addEventsToDay();
    }

    public void addEvents(View view) {

        ArrayList<JSONObject> j = getData(getContext());

        RelativeLayout schedule = (RelativeLayout) view.findViewById(R.id.fullAgendaSchedule);

        for (JSONObject i : j) {

            final Event event = convertJSONToEvent(i);

            TextView newEvent = new TextView(getActivity());
            newEvent.setGravity(Gravity.NO_GRAVITY);
            newEvent.setText(" "+event.name+"\n"+
                            " " +event.startTime+"-"+event.endTime+"\n"+
                            " " +event.location);
            newEvent.setBackgroundResource(R.drawable.border);

            int eventSize = (event.endTime - event.startTime) * 3;
            int eventPosition = event.startTime * 3; //Considering startTime is in terms of minutes

            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eventSize);
            params.leftMargin = 10;
            params.rightMargin = 10;
            params.topMargin = eventPosition;

            schedule.addView(newEvent, params);

            newEvent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getActivity(), ExpandedEventActivity.class);
                    i.putExtra("Date", dates[mPage - 1]);
                    i.putExtra("Name", event.name);
                    i.putExtra("Location", event.location);
                    i.putExtra("Start Time", event.startTime);
                    i.putExtra("End Time", event.endTime);

                    startActivity(i);
                }
            });
        }
    }

    public ArrayList<JSONObject> getData(Context mContext)
    {

        DateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        Calendar c = Calendar.getInstance();

        try {
            Date inputDate = formatter.parse(dates[mPage - 1]);
            c.setTime(inputDate);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        int week = c.get(Calendar.WEEK_OF_YEAR);
        int day = c.get(Calendar.DAY_OF_WEEK);


        Log.d("Entered", "JSON getData");
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.DatabaseEntry.COLUMN_WEEK + " = '"+week+"'" + " AND " +
                DatabaseContract.DatabaseEntry.COLUMN_DAY + daysOfWeek[day-1];

        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<JSONObject> events = new ArrayList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String json = cursor.getString(
                            cursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_DATA));
                    System.err.println(json);
                    JSONObject j;
                    try {
                        j = new JSONObject(json);
                        events.add(j);
                    } catch (JSONException e) {
                    }
                } while (cursor.moveToNext());
            }
        }

        cursor.close();

        return events;
    }

    public Event convertJSONToEvent(JSONObject jsonObject) {

        String id;
        try {
            id = jsonObject.getString("id");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        Event e = new Event("Tester", 1200, 1250, "random_id");
        return e;
    }

    /*
    public void pullEventsFromDatabase() {

        //pull and parse the events by the input day into stack
        Event test = new Event("ETHN1 Lecture", 780, 830);
        test.days[1] = true;
        test.days[3] = true;
        test.days[5] = true;
        test.repeating = true;
        test.location = "PETER108";
        events.add(test); //test event

        test = new Event("CSE101 Lecture", 1020, 1100);
        test.days[1] = true;
        test.days[3] = true;
        test.repeating = true;
        test.location = "CENTR105";
        events.add(test);

        test = new Event("CSE101 Discussion", 1140, 1190);
        test.days[1] = true;
        test.repeating = true;
        events.add(test);

        test = new Event("CSE110 Lecture", 1110, 1190);
        test.days[1] = true;
        test.days[3] = true;
        test.location = "CENTR119";
        test.repeating = true;
        events.add(test);

        test = new Event("CSE110 Discussion", 960, 1010);
        test.days[3] = true;
        test.repeating = true;
        events.add(test);

        test = new Event("COGS187a Lecture", 840, 920);
        test.days[2] = true;
        test.days[4] = true;
        test.location = "CSB002";
        test.repeating = true;
        events.add(test);

        test = new Event("ETHN1 Discussion", 840, 890);
        test.days[3] = true;
        test.location = "MANDEB-104";
        test.repeating = true;
        events.add(test);
    }
    */

}
