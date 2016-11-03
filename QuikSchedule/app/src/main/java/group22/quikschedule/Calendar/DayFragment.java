package group22.quikschedule.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import group22.quikschedule.R;

public class DayFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_DATES = "ARG_DATES";

    private String[] dates;
    private Stack<Event> events = new Stack<>();
    private int mPage;
    private RelativeLayout schedule;

    public static DayFragment newInstance(int page, String[] tabTitles) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putStringArray(ARG_DATES, tabTitles);

        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        dates = getArguments().getStringArray(ARG_DATES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_day, container, false);

        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(dates[mPage-1]);

        Log.i("State", "Creating Day View");

        DateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        Date inputDate = null;
        try {
            inputDate = (Date) format.parse(dates[mPage-1]);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("yyyy-MM-DD");

        pullEventsFromDatabase(format.format(inputDate));

        schedule = (RelativeLayout) view.findViewById(R.id.schedule);

        addEventsToDay();

        return view;
    }

    public void onResume() {
        super.onResume();

        //pullEventsFromDatabase
        //addEventsToDay();
    }

    public void pullEventsFromDatabase(String day) {

        //pull and parse the events by the input day into stack
        Event test = new Event("ETHN1 Lecture", 780, 830);
        test.days[1] = true;
        test.days[3] = true;
        test.days[5] = true;
        test.repeating = true;
        test.location = "PETER108";
        events.push(test); //test event

        test = new Event("CSE101 Lecture", 1020, 1100);
        test.days[1] = true;
        test.days[3] = true;
        test.repeating = true;
        test.location = "CENTR105";
        events.push(test);

        test = new Event("CSE101 Discussion", 1140, 1190);
        test.days[1] = true;
        test.repeating = true;
        events.push(test);

        test = new Event("CSE110 Lecture", 1110, 1190);
        test.days[1] = true;
        test.days[3] = true;
        test.location = "CENTR119";
        test.repeating = true;
        events.push(test);

        test = new Event("CSE110 Discussion", 960, 1010);
        test.days[3] = true;
        test.repeating = true;
        events.push(test);

        test = new Event("COGS187a Lecture", 840, 920);
        test.days[2] = true;
        test.days[4] = true;
        test.location = "CSB002";
        test.repeating = true;
        events.push(test);

        test = new Event("ETHN1 Discussion", 840, 890);
        test.days[3] = true;
        test.location = "MANDEB-104";
        test.repeating = true;
        events.push(test);
    }

    public void addEventsToDay() {

        while(!events.isEmpty()) {

            final Event e = events.pop();

            if((e.repeating && e.days[mPage-1]) || !e.repeating) {

                int eventSize = (e.endTime - e.startTime) * 3;
                int eventPosition = e.startTime*3; //Considering startTime is in terms of minutes

                RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eventSize);
                params.leftMargin = 10;
                params.rightMargin = 10;
                params.topMargin = eventPosition;

                TextView newEvent = new TextView(getActivity());
                newEvent.setGravity(Gravity.NO_GRAVITY);
                newEvent.setText(e.name);
                newEvent.setBackgroundResource(R.drawable.border);
                schedule.addView(newEvent, params);

                //add clicklistener to send data to edit
                newEvent.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getActivity(), ExpandedEventActivity.class);
                        i.putExtra("Date", dates[mPage-1]);
                        i.putExtra("Name", e.name);
                        i.putExtra("Location", e.location);
                        startActivity(i);
                    }
                });
            }
        }

    }

}
