package group22.quikschedule.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import group22.quikschedule.R;

public class QuickAgendaFragment extends Fragment {

    private String[] dates;
    private int mPage;
    private PriorityQueue<Event> events = new PriorityQueue<>(15, new Comparator<Event>() {
        @Override
        public int compare(Event a, Event b) {

            return a.startTime-b.startTime;
        }
    });

    public static QuickAgendaFragment newInstance(int page, String[] tabTitles) {
        Bundle args = new Bundle();
        args.putInt("Page", page);
        args.putStringArray("Dates", tabTitles);

        QuickAgendaFragment fragment = new QuickAgendaFragment();
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

        View view = inflater.inflate(R.layout.fragment_quick_agenda, container, false);

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

        pullEventsFromDatabase();
        LinearLayout schedule = (LinearLayout) view.findViewById(R.id.quickAgendaSchedule);

        while(!events.isEmpty()) {

            final Event event = events.poll();

            if ((event.repeating && event.days[mPage - 1]) || !event.repeating) {

                TextView newEvent = new TextView(getActivity());
                newEvent.setGravity(Gravity.NO_GRAVITY);
                newEvent.setText(event.name);
                newEvent.setBackgroundResource(R.drawable.border);

                schedule.addView(newEvent);

                newEvent.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getActivity(), ExpandedEventActivity.class);
                        i.putExtra("Date", dates[mPage - 1]);
                        i.putExtra("Name", event.name);
                        i.putExtra("Location", event.location);
                        startActivity(i);
                    }
                });
            }
        }
    }


    public void pullEventsFromDatabase() {

        DateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        Date inputDate = null;
        try {
            inputDate = (Date) format.parse(dates[mPage-1]);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("yyyy-MM-DD");

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

}
