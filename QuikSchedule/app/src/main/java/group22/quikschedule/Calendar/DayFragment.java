package group22.quikschedule.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;

import group22.quikschedule.R;

public class DayFragment extends Fragment {

    private String[] dates;
    private String[] daysOfWeek =
            {" = 'SUNDAY'", " = 'MONDAY'", " = 'TUESDAY'", " = 'WEDNESDAY'", " = 'THURSDAY'",
                    " = 'FRIDAY'", " = 'SATURDAY'"};
    private int mPage;
    private View view;

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

        view = inflater.inflate(R.layout.fragment_full_agenda, container, false);

        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(dates[mPage-1]);

        populateAgenda(getContext(), view);

        Calendar currentTime = Calendar.getInstance();
        NestedScrollView sv = (NestedScrollView) view.findViewById(R.id.calendarScrollView);
        sv.scrollTo(0, currentTime.get(Calendar.HOUR_OF_DAY)*60+currentTime.get(Calendar.MINUTE));
        return view;
    }

    public void onResume() {
        super.onResume();
        populateAgenda(getContext(), view);
    }

    public void populateAgenda(Context mContext, View view)
    {
        Log.d("Entered", "populateAgenda");

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

        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE "
                + DatabaseContract.DatabaseEntry.COLUMN_WEEK + " = '"+ Integer.toString(week)+ "'"
                + " AND " + DatabaseContract.DatabaseEntry.COLUMN_DAY + daysOfWeek[mPage-1];

        PriorityQueue<EventView> events = DatabaseHelper.getEvents(getContext(), sql);

        for(EventView i : events ) {
            Log.d("event", i.name);
            addEvent(i, view);
        }
    }

    public void addEvent(final EventView event, View v) {

        Log.d("Event Name", event.name+" "+event.startTime);
        event.setGravity(Gravity.NO_GRAVITY);
        event.setText(" "+event.name+"\n"+
                " " +event.getTimeAsString(EventView.STARTTIME)+
                "-" +event.getTimeAsString(EventView.ENDTIME)+"\n"+
                " " +event.location);
        event.setBackgroundResource(R.drawable.border);
        event.setTextColor(ContextCompat.getColor(getContext(), R.color.md_black_1000));

        int startTime = event.getTimeAsInt(EventView.STARTTIME);
        int endTime = event.getTimeAsInt(EventView.ENDTIME);
        int eventSize = (endTime - startTime) * 3;
        int eventPosition = startTime * 3; //Considering startTime is in terms of minutes

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eventSize);
        params.leftMargin = 10;
        params.rightMargin = 10;
        params.topMargin = eventPosition;

        RelativeLayout schedule = (RelativeLayout) v.findViewById(R.id.fullAgendaSchedule);
        schedule.addView(event, params);

        event.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), ExpandedEventActivity.class);
                i.putExtra("Date", dates[mPage - 1]);
                i.putExtra("Name", event.name);
                i.putExtra("Location", event.location);
                i.putExtra("Start Time", event.getTimeAsString(EventView.STARTTIME));
                i.putExtra("End Time", event.getTimeAsString(EventView.ENDTIME));
                i.putExtra("ID", event.id);
                i.putExtra("Transportation", event.transportation);
                i.putExtra("Comments", event.comments);
                i.putExtra("Materials", event.materials);

                startActivity(i);
            }
        });

    }
}