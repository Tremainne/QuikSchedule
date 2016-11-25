package group22.quikschedule;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.Calendar.SyncCalendarToSQL;
import group22.quikschedule.Calendar.SyncFirebaseToCalendar;
import group22.quikschedule.Calendar.WeekFragment;
import group22.quikschedule.Maps.Directions;
import group22.quikschedule.Maps.MapsFragment;
import group22.quikschedule.Maps.PollingService;
import group22.quikschedule.Settings.AlertActivity;
import group22.quikschedule.Settings.SettingsFragment;
import group22.quikschedule.Settings.WebregActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static group22.quikschedule.InitialActivity.APP_PREFERENCES;

/**
 * Class: NavigationDrawerActivity
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/12/16
 *
 * Description: Activity that primarily handles the switching between different tabs and their
 *              respective fragments. This includes sending the appropriate data to each fragment,
 *              getting permissions from the user, and general setup of the app. It will do the
 *              following things in this order:
 *                  1. ask user to log into Google if he/she isn't already
 *                  2. sync Google Calendar with the phone's database
 *                  3. ask the user to add classes from WebReg
 *                  4. gets input data from the Intent and sends it to other fragments/activities
 *                  5. sets the viewing tab according to the input data from the intent
 *
 * @author Rudr Tandon
 * @author Ishjot Suri
 * @author Rohan Chhabra
 * @author David Thomson
 * @author Tynan Dewes
 */
public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        EasyPermissions.PermissionCallbacks {

    public static boolean inMaps = false;
    public static boolean loggedIn;

    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    public void setAlarm(View view) throws JSONException {
        AlertActivity.setAlarm(getApplicationContext());
    }

    public static String getDayString() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "SUNDAY";

            case Calendar.MONDAY:
                return "MONDAY";

            case Calendar.TUESDAY:
                return "TUESDAY";

            case Calendar.WEDNESDAY:
                return "WEDNESDAY";

            case Calendar.THURSDAY:
                return "THURSDAY";

            case Calendar.FRIDAY:
                return "FRIDAY";

            case Calendar.SATURDAY:
                return "SATURDAY";
        }
        return "null";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        SharedPreferences settings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        settings.getBoolean("LoggedIn", loggedIn);
        if (loggedIn) {
            startActivity(new Intent(this, NavigationDrawerActivity.class));
        }

        startService(new Intent(this, PollingService.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("QuikSchedule");
        setSupportActionBar(toolbar);

        Intent i = getIntent();

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();

        if (i.hasExtra("webreg")) {
            if (i.getStringExtra("webreg").equals("webreg")) {
                new SyncFirebaseToCalendar(mCredential, this).execute();
            }
        }

        if(i.hasExtra("Initial")) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Google Sign In Successful!");
            alertDialog.setMessage("Would you like to add your classes from WebReg?");
            alertDialog.setCancelable(false);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), WebregActivity.class));
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        int selectFrag = i.getIntExtra("Fragment", 0);

        Fragment fragment = null;
        Class fragmentClass;

        switch (selectFrag) {
            case 0:
                fragmentClass = WeekFragment.class;
                break;
            case 1:
                fragmentClass = MapsFragment.class;
                break;
            case 3:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = WeekFragment.class;
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (i.hasExtra("Day")) {
            Bundle weekBundle = new Bundle();
            weekBundle.putInt("Day", i.getIntExtra("Day", 0));
            weekBundle.putInt("Month", i.getIntExtra("Month", 0));
            weekBundle.putInt("Year", i.getIntExtra("Year", 0));
            fragment.setArguments(weekBundle);
        }

        if (i.hasExtra("Location")) {
            Bundle mapsBundle = new Bundle();
            String result = Directions.convertAddress(i.getStringExtra("Location"));
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            mapsBundle.putString("Location", result);
            mapsBundle.putInt("Transportation", i.getIntExtra("Transportation", 0));
            fragment.setArguments(mapsBundle);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (inMaps) {
            startActivity(new Intent(NavigationDrawerActivity.this, NavigationDrawerActivity.class));
            inMaps = false;
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        Log.i("Fragment Selected", "Entering");
        if (id == R.id.nav_schedule) {
            fragmentClass = WeekFragment.class;
            Log.i("Fragment Selected", "Schedule");
            // Handle the camera action
        } else if (id == R.id.nav_maps) {
            fragmentClass = MapsFragment.class;

            Log.i("Fragment Selected", "Maps");
            inMaps = true;
        } else if (id == R.id.nav_friends) {
            Log.i("Fragment Selected", "Friends");
            Intent i;
            try {
                this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href="));
            } catch (Exception e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
            }
            startActivity(i);
            return true;
        } else if (id == R.id.nav_settings) {
            fragmentClass = SettingsFragment.class;
            Log.i("Fragment Selected", "Settings");
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            if (id == R.id.nav_maps) {
                Bundle mapsBundle = new Bundle();
                // This should probably be a getEvents method.
                String sql = "SELECT " + DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
                        DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE " +
                        DatabaseContract.DatabaseEntry.COLUMN_DAY + " IS '" +
                        NavigationDrawerActivity.getDayString() + "'";
                // get location of days first event.
                PriorityQueue<EventView> pq = DatabaseHelper.getEvents(getApplicationContext(),
                        sql);
                PriorityQueue<EventView> events = new PriorityQueue<>(10, new Comparator<EventView>() {
                    public int compare(EventView event1, EventView event2) {
                        int start1 = event1.getTimeAsInt(EventView.STARTTIME);
                        int start2 = event2.getTimeAsInt(EventView.STARTTIME);
                        if (start1 == start2 ) { return 0; }
                        return (start1 < start2) ? -1 : 1;
                    }
                });
                // Go through EventView PQ and add to new PQ based on start time
                for (EventView ev : pq) {
                    events.add(ev);
                }
                Map<Integer, EventView> map = new HashMap<Integer, EventView>();
                // Go through EventView PQ and add to map/PQ
                for (EventView ev : events) {
                    int start = ev.getTimeAsInt(EventView.STARTTIME);

                    map.put(start, ev);
                }
                Integer start = events.peek().getTimeAsInt(EventView.STARTTIME);
                events.remove();

                EventView curr = map.get(start);
                Log.d("NavBar", curr.getTimeAsString(EventView.STARTTIME));
                String end = curr.location;
                String result = Directions.convertAddress(end);
                mapsBundle.putString("Location", result);
                fragment.setArguments(mapsBundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void toWebreg(View view) {
        startActivity(new Intent(this, WebregActivity.class));
    }

    public void toMap(View view) {
        startActivity(new Intent(this, MapsFragment.class));
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
            Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
        } else {
            new SyncCalendarToSQL(mCredential, this).execute();
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
                this, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
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
                    android.Manifest.permission.GET_ACCOUNTS);
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
    protected void onActivityResult(
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
                                getPreferences(Context.MODE_PRIVATE);
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
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                apiAvailability.isGooglePlayServicesAvailable(this);
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
                apiAvailability.isGooglePlayServicesAvailable(this);
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
                NavigationDrawerActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

}