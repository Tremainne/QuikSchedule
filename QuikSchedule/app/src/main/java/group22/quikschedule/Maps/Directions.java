package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.json.*;

import group22.quikschedule.R;

/**
 * Created by christoph on 10/20/16.
 */

public class Directions {

    private LatLng home;

    private static final HashMap<String, String> converter = new HashMap<String, String>();
    static {
        converter.put("TM", "Marshall College");
        converter.put("APM","Applied Physics and Mathematics");
        converter.put("CENTR","Center Hall");
        converter.put("CSB","Cognitive Science Building");
        converter.put("CICC", "Copley International Conference Center");
        converter.put("GH", "Galbraith Hall");
        converter.put("HSS", "Humanities and Social Sciences");
        converter.put("LEDDN", "Humanities and Social Sciences");
        converter.put("MANDE", "Mandeville Center");
        converter.put("MCGIL", "McGill Hall");
        converter.put("PCYNH", "Pepper Canyon Hall");
        converter.put("PETER", "Peterson Hall");
        converter.put("PRICE", "Price Center");
        converter.put("RBC", "Robinson Building");
        converter.put("SEQUO", "Sequoyah Hall");
        converter.put("SSB", "Social Sciences Building");
        converter.put("SOLIS", "Solis Hall");
        converter.put("WLH", "Warren Lecture Hall");
        converter.put("YORK", "York Hall");
    }
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
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
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
        JSONObject dirJSON;
        try {
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
            dirJSON = new JSONObject(br.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return directions;
    }

    public static final String codeToName(String code) {
        return converter.get(code);
    }
}
