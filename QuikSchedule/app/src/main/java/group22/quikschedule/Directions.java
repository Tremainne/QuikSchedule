package group22.quikschedule;

import com.google.android.gms.maps.model.LatLng;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by christoph on 10/20/16.
 */

public class Directions {

    private LatLng home;

    /**
     * Builds a URL request for use with the DirectionsAPI, using transit for now.
     * @param start Strarting point of the trip to get directions for
     * @param end ending point of the trip to get directions for
     * @param arrivalTime The desired arrival time (should be 5 minutes before class probs)
     * @return  The URL request string
     */
    private String buildURLRequest(LatLng start, LatLng end, int arrivalTime) {
        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                start.toString() + "&destination=" + end.toString() + "&key=" +
                R.string.DirectionsAPIKey + "&" + R.string.TransitMode + "&arrivalTime=" +
                arrivalTime;
        return request;
    }

    public void setHome(LatLng home) {
        this.home = home;
    }

    public void makeRequest(LatLng dest, int arrivalTime) {
        String request = buildURLRequest(this.home, dest, arrivalTime);
        try {
            URL url = new URL(request);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection()
            getJson(con);

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List getJson(HttpsURLConnection con) {
        List directions = new ArrayList();
        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(con.getInputStream()));


    }
}
