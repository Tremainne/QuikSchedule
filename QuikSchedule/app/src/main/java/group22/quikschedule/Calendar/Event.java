package group22.quikschedule.Calendar;

import android.os.Parcelable;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by RohanChhabra on 10/30/16.
 */

public class Event{

    String name;
    int startTime;//in minutes
    int endTime;//in minutes
    String location;
    boolean repeating = false;
    int transportation; //0=walking,1=bike, 2=car, 3=bus
    boolean[] days = {false, false, false, false, false, false, false};
    boolean weekly = false;

    public Event(String n, int s, int e) {

        name = n;
        startTime = s;
        endTime = e;
    }

}
