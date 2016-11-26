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
 * Date: 10/12/2016
 *
 * Description: Simple class that contains a CalendarView and its listener that allows the user to
 *              select a date. The date information is then sent to the NavigationDrawerActivity,
 *              which will handle it from there.
 *
 * @author Rohan Chhabra
 */
public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView; // User uses this to pick a date

    /**
     * Description: Gets the date from the CalendarView and sends it to the Navigation Drawer
     * Activity.
     * @param savedInstanceState saved bundle if activity is recreated
     * @return void
     */
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
