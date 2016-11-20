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
import android.widget.Button;
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

public class DayFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private String[] dates;
    private String[] daysOfWeek =
            {" = 'SUNDAY'", " = 'MONDAY'", " = 'TUESDAY'", " = 'WEDNESDAY'", " = 'THURSDAY'",
                    " = 'FRIDAY'", " = 'SATURDAY'"};
    private int mPage;
    private View view;

    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

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
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_full_agenda, container, false);

        final RelativeLayout schedule = (RelativeLayout) view.findViewById(R.id.fullAgendaSchedule);
        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(dates[mPage-1]);

        Log.d("SyncWTF", "Day Fragment Adding Events");
        populateAgenda(getContext(), view);

        Calendar currentTime = Calendar.getInstance();
        NestedScrollView sv = (NestedScrollView) view.findViewById(R.id.calendarScrollView);
        sv.scrollTo(0, currentTime.get(Calendar.HOUR_OF_DAY)*60+currentTime.get(Calendar.MINUTE));
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getResultsFromApi();
                swipeRefreshLayout.setRefreshing(false);
                schedule.removeAllViews();
                populateAgenda(getContext(), view);
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        populateAgenda(getContext(), view);
    }

    public void populateAgenda(Context mContext, View view)
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
        int week = c.get(Calendar.WEEK_OF_YEAR);

        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE "
                + DatabaseContract.DatabaseEntry.COLUMN_WEEK + " = '"+ Integer.toString(week)+ "'"
                + " AND " + DatabaseContract.DatabaseEntry.COLUMN_DAY + daysOfWeek[mPage-1];

        PriorityQueue<EventView> events = DatabaseHelper.getEvents(getContext(), sql);

        Log.d("SyncWTF", " "+events.size()+" "+dates[mPage - 1]);
        int j = 0;
        for(EventView i : events ) {
            Log.d("SyncWTF", i.name + " "+(++j));
            addEvent(i, view);
        }
    }

    public void addEvent(final EventView event, View v) {

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

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
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
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
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
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
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
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
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
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
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
     * Check that Google Play services APK is installed and up to date.
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
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
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
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
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