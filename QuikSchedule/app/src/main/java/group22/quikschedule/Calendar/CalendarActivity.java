package group22.quikschedule.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CalendarView;

import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;

/**
 * Class: CalendarActivity
 *
 * Bugs: None known
 * Version: 1.0
 * Date: ????
 *
 * Description:
 *
 * @author Rohan Chhabra
 */
public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                Intent i = new Intent(view.getContext(), NavigationDrawerActivity.class);
                i.putExtra("Day", dayOfMonth);
                i.putExtra("Year", year);
                i.putExtra("Month", month);
                startActivity(i);
            }
        });

    }
}
