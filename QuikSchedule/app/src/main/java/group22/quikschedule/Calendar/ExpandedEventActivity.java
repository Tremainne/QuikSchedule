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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_ACCOUNT_PICKER;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_AUTHORIZATION;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static group22.quikschedule.Calendar.CalendarSyncActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

//import group22.quikschedule.Maps.MapsActivity;

/**
 * Class: ExpandedEventActivity
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/5/16
 *
 * Description: Activity that ...?
 *
 * @author Ishjot Suri
 * @author Rohan Chhabra
 */
public class ExpandedEventActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private boolean editingEvent = false;
    private boolean deleteEvent = false;
    private String date;
    private TextView dateContainer;
    private TextView startTimeContainer;
    private TextView endTimeContainer;
    private EditText location;
    private Spinner dropdown;
    private String eventID;

    GoogleAccountCredential mCredential;
    private final Calendar c = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");


    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private com.google.api.services.calendar.Calendar mService = null;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_event);

        EditText eventName = (EditText) findViewById(R.id.eventName);
        dateContainer = (TextView) findViewById(R.id.datePicker);
        startTimeContainer = (TextView) findViewById(R.id.startTimePicker);
        endTimeContainer = (TextView) findViewById(R.id.endTimePicker);
        location = (EditText) findViewById(R.id.location);
        EditText comments = (EditText) findViewById(R.id.comments);
        EditText materials = (EditText) findViewById(R.id.materials);

        dropdown = (Spinner)findViewById(R.id.transportationMode);
        String[] items = new String[]
                {"Transit", "Driving", "Cycling", "Walking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, items);
        dropdown.setAdapter(adapter);

        Intent i = getIntent();

        if(i.hasExtra("Name")) {
            editingEvent = true;

            eventID = i.getStringExtra("ID");
            eventName.setText(i.getStringExtra("Name"));
            location.setText(i.getStringExtra("Location"));

            System.err.println( i.getStringExtra("Date") );

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
            dateContainer.setText(dateFormat.format(c.getTime()));

            startTimeContainer.setText(i.getStringExtra("Start Time"));
            endTimeContainer.setText(i.getStringExtra("End Time"));

            if(i.hasExtra("Comments")) {
                comments.setText(i.getStringExtra("Comments"));
            }

            if(i.hasExtra("Materials")) {
                Log.d("Trans", "Tf");
                materials.setText(i.getStringExtra("Materials"));
            }

            if(i.hasExtra("Transportation")) {

                int j = i.getIntExtra("Transportation", 2);
                dropdown.setSelection(j);
            }
        }
    }

    public void toCalendar(View v) {
        //Send Data to Google Calendar

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        if(v.getId() == R.id.delete) {
            Log.d("Delete", "toCalendar");
            deleteEvent = true;
            deleteEvent();
        }
        else {
            addToCalendar();
        }
    }

    private boolean addToCalendar () {

        String eventName = ((EditText) findViewById(R.id.eventName)).getText().toString();
        String date = ((TextView) findViewById(R.id.datePicker)).getText().toString();
        String startTime = ((TextView) findViewById(R.id.startTimePicker)).getText().toString();
        String endTime = ((TextView) findViewById(R.id.endTimePicker)).getText().toString();
        String locationName = ((EditText) findViewById(R.id.location)).getText().toString();

        if (!eventName.matches(".*[a-zA-Z].*")) {
            Toast.makeText(this, "Enter an event name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (date.equals("") || date == null || date.length() < 10) {
            Toast.makeText(this, "Enter a date", Toast.LENGTH_LONG).show();
            return false;
        }
        if (startTime.equals("") || startTime == null || startTime.length() < 5) {
            Toast.makeText(this, "Enter a start time", Toast.LENGTH_LONG).show();
            return false;
        }
        if (endTime.equals("") || endTime == null || endTime.length() < 5) {
            Toast.makeText(this, "Enter an end time", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!locationName.matches(".*[a-zA-Z].*")) {
            Toast.makeText(this, "Enter a location", Toast.LENGTH_LONG).show();
            return false;
        }

        Date startDate = null;
        try{
            startDate = timeFormat.parse(startTimeContainer.getText().toString());
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        Date endDate = null;
        try{
            endDate = timeFormat.parse(endTimeContainer.getText().toString());
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        if(startDate.after(endDate)) {
            Toast.makeText(this, "Start time should be before end time and on the same day ",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
            return false;

        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
            return false;

        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
            return false;

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
                    .setDescription(((EditText) findViewById(R.id.materials)).getText().toString() +
                    "\n"+ ((EditText) findViewById(R.id.comments)).getText().toString() + "\n" +
                    dropdown.getSelectedItemPosition());

            Calendar cal = Calendar.getInstance();

            Calendar time = Calendar.getInstance();
            Date inputDate = null;
            try{
                inputDate = dateFormat.parse(dateContainer.getText().toString());
                cal.setTime(inputDate);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }

            time.setTime(startDate);
            cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            DateTime startDateTime = new DateTime(cal.getTimeInMillis());

            time.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
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

            Intent i = new Intent(this, NavigationDrawerActivity.class);
            i.putExtra("Day", c.get(Calendar.DAY_OF_MONTH));
            i.putExtra("Year", c.get(Calendar.YEAR));
            i.putExtra("Month", c.get(Calendar.MONTH));
            startActivity(i);

            return true;
        }
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
                dateContainer.setText(dateFormat.format(c.getTime()));
            }
        }, year, month, day);
        datePicker.show();
    }

    public void pickTime(final View v) {

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                c.set(Calendar.HOUR_OF_DAY, selectedHour);
                c.set(Calendar.MINUTE, selectedMinute);
                if(v.getId() == R.id.endTimePicker) {

                    endTimeContainer.setText(timeFormat.format(c.getTime()));
                } else {
                        startTimeContainer.setText(timeFormat.format(c.getTime()));
                }

            }
        }, hour, minute, false);
        mTimePicker.show();
    }

    public void routeToEvent(View v) {

        if(location.getText().toString().equals("")) {

            Toast.makeText(getBaseContext(), "Please enter a location for the event.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Intent i  = new Intent(this, NavigationDrawerActivity.class);
            i.putExtra("Location", location.getText().toString());
            i.putExtra("Fragment", 1);
            i.putExtra("Transportation", dropdown.getSelectedItemPosition());
            startActivity(i);
        }
    }

    public void cancel(View v) {
        onBackPressed();
    }

    public boolean deleteEvent() {

        if(editingEvent) {

            deleteEvent = true;
            Log.d("Delete", "Function Delete 1");

            if (!isGooglePlayServicesAvailable()) {
                acquireGooglePlayServices();
                return false;

            } else if (mCredential.getSelectedAccountName() == null) {
                chooseAccount();
                return false;

            } else if (!isDeviceOnline()) {
                Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
                return false;

            }

            Log.d("Delete", "Function Delete 2");
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Quick Calendar")
                    .build();

            new addToCalendarInBackground().execute();

            Calendar c = Calendar.getInstance();

            Intent i = new Intent(this, NavigationDrawerActivity.class);
            i.putExtra("Day", c.get(Calendar.DAY_OF_MONTH));
            i.putExtra("Year", c.get(Calendar.YEAR));
            i.putExtra("Month", c.get(Calendar.MONTH));
            startActivity(i);

            return true;
        }
        else {
            Toast.makeText(getBaseContext(), "Event does not exist in calendar",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            //if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
            //} else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            //}
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                } else {
                    if(deleteEvent) {
                        deleteEvent();
                    }
                    else{
                        addToCalendar();
                    }
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
                        if(deleteEvent) {
                            deleteEvent();
                        }
                        else{
                            addToCalendar();
                        }
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    if(deleteEvent) {
                        deleteEvent();
                    }
                    else{
                        addToCalendar();
                    }
                }
                break;
        }
    }

    /**
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // not needed
    }

    /**
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // not needed
    }

    /**
     *
     * @return
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @param connectionStatusCode
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ExpandedEventActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     *
     * @param summary
     * @return
     */
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

            if(deleteEvent) {
                Log.d("Delete", "Delete");
                try {
                    Log.d("Delete", "SUCCESS: " + mService.events().delete(getCalendarIdFromSummary("QuickSchedule"), eventID).execute());
                } catch (IOException e) {
                    System.err.println("Failed to delete event in calendar");
                }
            }
            else if(editingEvent) {

                Log.d("Delete", "Editing");
                try {
                    System.err.println("SUCCESS: " + mService.events().update(getCalendarIdFromSummary("QuickSchedule"), eventID, event).execute());
                } catch (IOException e) {
                    System.err.println("Failed to edit event in calendar");
                }
            }
            else {
                Log.d("Delete", "New");
                try {
                    System.err.println("SUCCESS: " + mService.events().insert(getCalendarIdFromSummary("QuickSchedule"), event).execute());
                } catch (IOException e) {
                    System.err.println("Failed to add event to calendar");
                }
            }

            return null;
        }
    }
}