package group22.quikschedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import group22.quikschedule.Calendar.CalendarSyncActivity;
import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.WeekFragment;
import group22.quikschedule.Friends.FriendsFragment;
import group22.quikschedule.Maps.MapsFragment;
import group22.quikschedule.Maps.PollingService;
import group22.quikschedule.Settings.AlertActivity;
import group22.quikschedule.Settings.SettingsFragment;
import group22.quikschedule.Settings.WebregActivity;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean inMaps = false;


    public static HashMap<Integer, PendingIntent> alarmIntentMap;
    public static HashMap<Integer, AlarmManager> alarmManagerMap;




    public static int setAlarmtime(JSONObject jsonObj, Calendar cal) throws JSONException {
        HashMap<String, String> map = AlertActivity.getDataFromEvent(jsonObj);

        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(map.get("start").substring(0,2)));
        cal.set(Calendar.MINUTE, Integer.parseInt(map.get("start").substring(3)));

        return Integer.parseInt(map.get("start"));
    }

    public void setAlarm(View view) throws JSONException {
        System.err.println("adsfasdf");

        //Get the list of jsonObjects for the current day
        ArrayList<JSONObject> list = getData(getApplicationContext());

        //Initialise my maps that store the pendingIntent and alarmManager for each alarm
        //with the id as the time that they were set so that they can be canceled later using
        //alarmManagerMap.get(timeOfAlarmToCancel).cancel(alarmIntentMap.get(timeOfAlarmToCancel));
        alarmIntentMap = new HashMap<Integer, PendingIntent>();
        alarmManagerMap = new HashMap<Integer, AlarmManager>();

        for(int i = 0; i < list.size(); i++)
        {
            Calendar c = Calendar.getInstance();
            //Set the alarm time for event i based on the start time and get the time back
            int id = setAlarmtime(list.get(i), c);

            //Set a new alertIntent for the notification
            Intent alertIntent = new Intent(this, AlertActivity.class);

            //set a pending intent where the unique id is the time of the event
            //If you have two events with the same time then it wont notify you for second
            PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), id, alertIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Add this to my static map so it can be accessed later to cancel
            alarmIntentMap.put(id, contentIntent);

            //Create an AlarmManager for each event
            AlarmManager alarmManager  = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            ////Add this to my static map so it can be accessed later to cancel the pendingIntent
            alarmManagerMap.put(id, alarmManager);

            //Set the alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), contentIntent);
        }


        /*Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.get(Calendar.SECOND)+5);

        //System.err.println("Current time: " + c.toString());

        Intent alertIntent = new Intent(this, AlertActivity.class);
         PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alertIntent,
                 PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager  = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY
        , contentIntent);

        //alarmMangager.set(AlarmManager.RTC_WAKEUP, alertTime,
          //      PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
*/
    }

    public ArrayList<JSONObject> cursorToJson(Cursor cursor)
    {

        ArrayList<JSONObject> events = new ArrayList<>();
        Log.d("FUCK", "ME");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                System.err.println("AYYY2");
                do {
                    System.err.println("AYYY3");
                    String json = cursor.getString(
                            cursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_DATA));
                    Log.d("Pls", "help");
                    System.err.println(json);
                    JSONObject j;
                    try {
                        j = new JSONObject(json);
                        Log.d("New JSON", json);
                        events.add(j);
                    } catch (JSONException e) {
                        Log.i("FUCK", "OFF");
                    }
                } while (cursor.moveToNext());
            }
        }

        return events;

    }

    public static String getDayString()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";

            case Calendar.MONDAY:
                return "Monday";

            case Calendar.TUESDAY:
                return "Tuesday";

            case Calendar.WEDNESDAY:
                return "Wednesday";

            case Calendar.THURSDAY:
                return "Thursday";

            case Calendar.FRIDAY:
                return "Friday";

            case Calendar.SATURDAY:
                return "Saturday";
        }
        return "null";
    }

    public ArrayList<JSONObject> getData(Context mContext)
    {

        Log.d("Entered", "JSON getData");
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + DatabaseContract.DatabaseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.DatabaseEntry.COLUMN_DAY + " = '" +getDayString()+ "'";

        // COLUMN_WEEK is number
        //+ DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
        //DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE "
        //+ DatabaseContract.DatabaseEntry.COLUMN_DAY + " IS 'MONDAY'"; // + " WHERE " +
        //DatabaseContract.DatabaseEntry.COLUMN_DAY; // + " IS MONDAY";
        Cursor cursor = db.rawQuery(sql, null); //+ "MONDAY", null);
        ArrayList<JSONObject> events = cursorToJson(cursor);
        Log.d("JSON Objects", events.toString());
        Log.d("JSON Objects size", ""+events.size());
        cursor.close();
        return events;
    }

    /*public void setAlarm(View view)
    {
        Intent myIntent = new Intent(this , Alert.class);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 19);
        calendar.set(Calendar.SECOND, 00);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext()); //Allows for Facebook SDK access
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        startService( new Intent(this, PollingService.class) );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("QuikSchedule");
        setSupportActionBar(toolbar);

        Intent i = getIntent();

        int selectFrag = i.getIntExtra("Fragment", 0);

        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = WeekFragment.class;

        switch(selectFrag) {
            case 0:
                fragmentClass = WeekFragment.class;
                break;
            case 1:
                fragmentClass = MapsFragment.class;
                break;
            case 2:
                fragmentClass = FriendsFragment.class;
                break;
            case 3:
                fragmentClass = SettingsFragment.class;
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

        if(i.hasExtra("Location")) {
            Bundle mapsBundle = new Bundle();
            mapsBundle.putString("Location", i.getStringExtra("Location"));
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
        }
        else if( inMaps == true ) {
            startActivity(new Intent(NavigationDrawerActivity.this, NavigationDrawerActivity.class));
            inMaps = false;
        }
        else {
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
            fragmentClass = FriendsFragment.class;
            Log.i("Fragment Selected", "Friends");
        } else if (id == R.id.nav_settings) {
            fragmentClass = SettingsFragment.class;
            Log.i("Fragment Selected", "Settings");
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            if (id == R.id.nav_maps) {
                Bundle mapsBundle = new Bundle();
                mapsBundle.putString("Location", "CENTR");
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

    public void toWebreg(View view){
        startActivity(new Intent(this, WebregActivity.class));
    }

    public void syncCalendarToSQL (View view) { startActivity(new Intent(this, CalendarSyncActivity.class)); }

    public void toMap(View view) {
        startActivity(new Intent(this, MapsFragment.class));
    }
}