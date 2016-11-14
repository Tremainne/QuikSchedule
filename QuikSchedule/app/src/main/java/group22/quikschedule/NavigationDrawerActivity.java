package group22.quikschedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.FacebookSdk;

import group22.quikschedule.Calendar.CalendarSyncActivity;
import group22.quikschedule.Calendar.WeekFragment;
import group22.quikschedule.Friends.FriendsFragment;
import group22.quikschedule.Maps.MapsFragment;
import group22.quikschedule.Settings.SettingsFragment;
import group22.quikschedule.Settings.WebregActivity;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext()); //Allows for Facebook SDK access
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

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
        else {
            startActivity(new Intent(NavigationDrawerActivity.this, NavigationDrawerActivity.class));
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