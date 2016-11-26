package group22.quikschedule.Calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import group22.quikschedule.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * Class: DayFragment
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/12/2016
 *
 * Description: Fragment that contains the schedule for each individual day when on the Schedule
 *              tab. It handles populating the agenda by sending a query to the SQL database,
 *              pulling out all the events, formatting them according to their time and duration,
 *              and displaying them on the UI. It also allows each event to be clicked on, which
 *              will then send that events information to the ExpandedEventActivity. This class
 *              also contains the ability to refresh the days events and sync them with the phone's
 *              database and Google Calendar.
 *
 * @author Rohan Chhabra
 */
public class DayFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private String[] dates; // all 7 dates of the current week
    private String[] daysOfWeek =
            {" = 'SUNDAY'", " = 'MONDAY'", " = 'TUESDAY'", " = 'WEDNESDAY'", " = 'THURSDAY'",
                    " = 'FRIDAY'", " = 'SATURDAY'"}; // Day String for SQL Query
    private int mPage; // current tab number
    private View view; // View that contains the agenda to populate

    GoogleAccountCredential mCredential; // GoogleCredentials for accessing calendar

    // constants for Google Credential use
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    /**
     * Description: Initializes a new instance of the DayFragment.
     *
     * @param page page number which also serves as dayOfWeek number
     * @param tabTitles titles for the top each tab
     * @return DayFragment returns an instance of this DayFragment once initialized
     */
    public static DayFragment newInstance(int page, String[] tabTitles) {
        Bundle args = new Bundle();
        args.putInt("Page", page);
        args.putStringArray("Dates", tabTitles);

        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Description: Gets arguments from the input bundle and sets the global variables. Also clears
     * the Google Account Credentials.
     *
     * @param savedInstanceState bundle that saves data when recreating activity
     * @return void
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt("Page");
        dates = getArguments().getStringArray("Dates");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    /**
     * Description: Creates the view for the current day by initializing the date and populating
     * the agenda. Also allows the agenda to be refreshed.
     *
     * @param inflater inflates the view
     * @param container ViewGroup container that is used to inflate the layout
     * @param savedInstanceState bundle that saves data when recreating activity
     * @return View the view that is created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_full_agenda, container, false);

        // initializes the date to the current one
        final RelativeLayout schedule = (RelativeLayout) view.findViewById(R.id.fullAgendaSchedule);
        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(dates[mPage-1]);

        populateAgenda(view); // adds events to the agenda

        // scrolls to the current time of the day
        Calendar currentTime = Calendar.getInstance();
        NestedScrollView sv = (NestedScrollView) view.findViewById(R.id.calendarScrollView);
        sv.scrollTo(0, currentTime.get(Calendar.HOUR_OF_DAY)*60+currentTime.get(Calendar.MINUTE));

        //Google Account Credential clearanse
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // Swipe Refresh listener that will sync the phone's database, remove all events from the
        // schedule and repopulates using the newly synced database
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getResultsFromApi();
                swipeRefreshLayout.setRefreshing(false);
                schedule.removeAllViews();
                populateAgenda(view);
            }
        });

        return view;
    }

    /**
     * Description: When the activity resumes, it repopulates the agenda.
     *
     * @return void
     */
    public void onResume() {
        super.onResume();
        populateAgenda(view);
    }

    /**
     * Description: Displays the events for the day on the agenda. First gets all the day's data
     * from the SQL database and then formats and displays the EventViews through a call to
     * addEvent.
     *
     * @param view View where events will be added
     * @return void
     */
    public void populateAgenda(View view)
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
        int week = c.get(Calendar.WEEK_OF_YEAR); // current week

        // Query for the events of the day
        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE "
                + DatabaseContract.DatabaseEntry.COLUMN_WEEK + " = '"+ Integer.toString(week)+ "'"
                + " AND " + DatabaseContract.DatabaseEntry.COLUMN_DAY + daysOfWeek[mPage-1];

        // gets events from the SQL database
        PriorityQueue<EventView> events = DatabaseHelper.getEvents(getContext(), sql);

        // loops through all events, adding them to agenda
        for(EventView i : events ) {
            addEvent(i, view);
        }
    }

    /**
     * Description:
     * @param event EventView item to be formatted and displayed on UI
     * @param v     The view to display the EventView on
     * @return
     */
    public void addEvent(final EventView event, View v) {

        // sets the text, gravity, textColor, and background of the EventView
        event.setGravity(Gravity.NO_GRAVITY);
        event.setText(" "+event.name+"\n"+
                " " +event.getTimeAsString(EventView.STARTTIME)+
                "-" +event.getTimeAsString(EventView.ENDTIME)+"\n"+
                " " +event.location);
        event.setBackgroundResource(R.drawable.border);
        event.setTextColor(ContextCompat.getColor(getContext(), R.color.md_black_1000));

        // Calculates the size of the EventView and its position based upon the start and end times
        int startTime = event.getTimeAsInt(EventView.STARTTIME);
        int endTime = event.getTimeAsInt(EventView.ENDTIME);
        int eventSize = (endTime - startTime) * 3;
        int eventPosition = startTime * 3; //Considering startTime is in terms of minutes

        // LayoutParams to format the layout of the EventView when on the UI
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eventSize);
        params.leftMargin = 10;
        params.rightMargin = 10;
        params.topMargin = eventPosition;

        // Relative Layout that will hold the EventView
        RelativeLayout schedule = (RelativeLayout) v.findViewById(R.id.fullAgendaSchedule);

        //adds the EventView to the View
        schedule.addView(event, params);

        // Sets a Listener that will open the ExpandedEventView whenever the user clicks on the
        // EventView. It also sends the Event's data as to the ExpandedEventView for it to display
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

    /**
     * Description: Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     *
     * @return void
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getActivity().getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
        } else {
            new SyncCalendarToSQL(mCredential, getActivity()).execute();
        }
    }

    /**
     * Description: Opens a dialog that lets the user to choose a Google account. It will choose
     * the previously saved account if it exists, otherwise it will prompt the user to choose. It
     * will also make sure that the app has the GET_ACCOUNTS permissions first.
     *
     * @return void
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getActivity(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
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

    /**
     * Description: Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     * @return void
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //mOutputText.setText(
                    //        "This app requires Google Play Services. Please install " +
                    //                "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Description: Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     * @return void
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Description: Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     * @return void
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Description: Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     * @return void
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Description: Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Description: Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getActivity());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Description: Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     *
     * @return void
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Description: Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     * @return void
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

}