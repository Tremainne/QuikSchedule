package group22.quikschedule.Calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import group22.quikschedule.Maps.MapsActivity;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;

public class ExpandedEventActivity extends AppCompatActivity {

    private boolean editingEvent = false;
    private String date;
    private TextView dateContainer;
    private int startTime;
    private TextView startTimeContainer;
    private int endTime;
    private TextView endTimeContainer;
    private EditText location;

    private final Calendar c = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_event);

        dateContainer = (TextView) findViewById(R.id.datePicker);
        startTimeContainer = (TextView) findViewById(R.id.startTimePicker);
        endTimeContainer = (TextView) findViewById(R.id.endTimePicker);
        location = (EditText) findViewById(R.id.location);

        Intent i = getIntent();

        if(i.hasExtra("Name")) {
            editingEvent = true;
            EditText eventName = (EditText) findViewById(R.id.eventName);

            eventName.setText(i.getStringExtra("Name"));
            location.setText(i.getStringExtra("Location"));

            date = i.getStringExtra("Date");
            DateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            Date inputDate = null;
            try {
                inputDate = format.parse(date);
            }
            catch(ParseException e) {
                e.printStackTrace();
            }

            c.setTime(inputDate);
            dateContainer.setText(formatter.format(c.getTime()));
        }
    }

    public void toCalendar(View v) {
        //Send Data to Google Calendar


        //

        Intent i = new Intent(this, NavigationDrawerActivity.class);
        i.putExtra("Day", c.get(Calendar.DAY_OF_MONTH));
        i.putExtra("Year", c.get(Calendar.YEAR));
        i.putExtra("Month", c.get(Calendar.MONTH));
        startActivity(i);
    }

    public void pickDate(View v) {

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateContainer.setText(formatter.format(c.getTime()));
            }
        }, year, month, day);
        datePicker.show();
    }

    public void timePicker() {

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                c.set(Calendar.HOUR_OF_DAY, selectedHour);
                c.set(Calendar.MINUTE, selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.show();
    }

    public void pickStartTime(View v) {

        timePicker();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        startTimeContainer.setText(timeFormat.format(c.getTime()));
    }

    public void pickEndTime(View v) {

        timePicker();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        endTimeContainer.setText(timeFormat.format(c.getTime()));
    }

    public void routeToEvent(View v) {

        if(location.getText().toString().equals("")) {

            Toast.makeText(getBaseContext(), "Please enter a location for the event.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Intent i  = new Intent(this, MapsActivity.class);
            i.putExtra("Location", location.getText());
            startActivity(i);
        }
    }

}