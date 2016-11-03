package group22.quikschedule.Calendar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import group22.quikschedule.R;

public class ExpandedEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_event);

        Intent i = getIntent();

        if(i.hasExtra("Name")) {
            EditText eventName = (EditText) findViewById(R.id.eventName);
            EditText date = (EditText) findViewById(R.id.date);
            EditText loc = (EditText) findViewById(R.id.location);

            eventName.setText(i.getStringExtra("Name"));
            date.setText(i.getStringExtra("Date"));
            loc.setText(i.getStringExtra("Location"));
        }
    }

    public void toCalendar(View v) {
        //Send Data to Google Calendar
        onBackPressed();
    }

}