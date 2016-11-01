package group22.quikschedule.Calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;

import group22.quikschedule.NavigationDrawerActivity;

/**
 * Created by kris on 10/30/16.
 */

public class SyncFirebaseToCalendar extends AsyncTask<Void, Void, Void> {

    private final String TAG = "SyncFirebaseToCalendar";

    private String android_id;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference db;

    public SyncFirebaseToCalendar(GoogleAccountCredential mCredential, NavigationDrawerActivity navigationDrawerActivity) {
        android_id = Settings.Secure.getString(navigationDrawerActivity.getContentResolver(), Settings.Secure.ANDROID_ID); //Device ID

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        db = FirebaseDatabase.getInstance().getReference();
    }

    public void SyncFirebaseToCalendar (GoogleAccountCredential credential, Context context) {
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //Device ID

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected Void doInBackground(Void... params) {
        db.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currClassName;
                int i = 0;

                currClassName = (String) dataSnapshot.child(android_id).child("class_" + i)
                        .child("class").getValue();

                while (currClassName != null) {
                    System.err.println(currClassName);
                    DataSnapshot snapshot = dataSnapshot.child(android_id).child("class_" + i);

                    String className = (String) snapshot.child("class").getValue();
                    String classType = (String) snapshot.child("classType").getValue();
                    String startTime = (String) snapshot.child("startTime").getValue();
                    String endTime   = (String) snapshot.child("endTime").getValue();
                    String day       = (String) snapshot.child("day").getValue();
                    String location  = (String) snapshot.child("location").getValue();
                    String section   = (String) snapshot.child("section").getValue();

                    Event event = new Event()
                            .setSummary(className + " - " + classType)
                            .setLocation(location)
                            .setDescription("Section: " + section);

                    String[] recurrence = new String[] {"RRULE:FREQ=WEEKLY;COUNT=10"};
                    event.setRecurrence(Arrays.asList(recurrence));

                    currClassName = (String) dataSnapshot.child(android_id).child("class_" + i)
                            .child("class").getValue();
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        return null;
    }

    @Override
    protected void onPostExecute (Void result) {
    }
}
