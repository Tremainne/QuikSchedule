package group22.quikschedule.Calendar;

/**
 * Created by RohanChhabra on 10/30/16.
 */

public class Event {

    String name;
    int startTime;//in minutes
    int endTime;//in minutes
    String location;
    String dateTime;
    int transportation; //0=walking,1=bike, 2=car, 3=bus
    String id;

    public Event(String n, int s, int e, String id) {

        name = n;
        startTime = s;
        endTime = e;
        this.id = id;
    }

}
