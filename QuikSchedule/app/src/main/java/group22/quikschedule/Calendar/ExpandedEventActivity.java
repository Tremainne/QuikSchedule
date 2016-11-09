package group22.quikschedule.Calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.data.Application;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import group22.quikschedule.Maps.MapsActivity;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_ACCOUNT_PICKER;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_AUTHORIZATION;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

public class ExpandedEventActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private boolean editingEvent = false;
    private String date;
    private TextView dateContainer;
    private int startTime;
    private TextView startTimeContainer;
    private int endTime;
    private TextView endTimeContainer;
    private EditText location;

    GoogleAccountCredential mCredential;
    private final Calendar c = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");


    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private com.google.api.services.calendar.Calendar mService = null;

    private Event event;

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

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        if (addToCalendar()) {
            Intent i = new Intent(this, NavigationDrawerActivity.class);
            i.putExtra("Day", c.get(Calendar.DAY_OF_MONTH));
            i.putExtra("Year", c.get(Calendar.YEAR));
            i.putExtra("Month", c.get(Calendar.MONTH));
            startActivity(i);
        } else {
            // stay on this view
        }
    }

    private boolean addToCalendar () {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
            addToCalendar();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
        } else {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Quick Calendar")
                    .build();

            event = new Event()
                    .setSummary(((EditText) findViewById(R.id.eventName)).getText().toString())
                    .setLocation(location.getText().toString())
                    .setDescription("Textbooks: " + ((EditText) findViewById(R.id.materials)).getText().toString() +
                    "\n" + "Comments: " + ((EditText) findViewById(R.id.comments)).getText().toString());

            String date = ((TextView) findViewById(R.id.datePicker)).getText().toString();
            String startTime = ((TextView) findViewById(R.id.startTimePicker)).getText().toString();
            String endTime = ((TextView) findViewById(R.id.endTimePicker)).getText().toString();

            if (date == "" || date == null || date.length() < 10) {
                Toast.makeText(this, "Enter a date", Toast.LENGTH_LONG).show();
                return false;
            }
            if (startTime == "" || startTime == null || startTime.length() < 5) {
                Toast.makeText(this, "Enter a start time", Toast.LENGTH_LONG).show();
                return false;
            }
            if (endTime == "" || endTime == null || endTime.length() < 5) {
                Toast.makeText(this, "Enter an end time", Toast.LENGTH_LONG).show();
                return false;
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Integer.parseInt(date.substring(0,2)) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(3,5)));
            cal.set(Calendar.YEAR, Integer.parseInt(date.substring(6,10)));

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime.substring(0,2)));
            cal.set(Calendar.MINUTE, Integer.parseInt(startTime.substring(3,5)));
            DateTime startDateTime = new DateTime(cal.getTimeInMillis());

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime.substring(0,2)));
            cal.set(Calendar.MINUTE, Integer.parseInt(endTime.substring(3,5)));
            DateTime endDateTime = new DateTime(cal.getTimeInMillis());

            EventDateTime eventStart = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());

            EventDateTime eventEnd = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());

            event.setStart(eventStart);
            event.setEnd(eventEnd);

            new addToCalendarInBackground().execute();
            return true;
        }

        return false;
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
            //Intent i  = new Intent(this, MapsActivity.class);
            //i.putExtra("Location", location.getText());
            //startActivity(i);
        }
    }



    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                } else {
                    addToCalendar();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        addToCalendar();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    addToCalendar();
                }
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // not needed
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // not needed
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ExpandedEventActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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

    private class addToCalendarInBackground extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                System.err.println("SUCCESS: " + mService.events().insert(getCalendarIdFromSummary("QuickSchedule"), event).execute());
            } catch (IOException e) {
                System.err.println("Failed to add event to calendar");
            }

            return null;
        }
    }
}