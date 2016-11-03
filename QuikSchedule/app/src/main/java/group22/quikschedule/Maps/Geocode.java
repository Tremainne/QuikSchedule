package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import group22.quikschedule.R;

/**
 * Created by christoph on 10/31/16.
 */

public class Geocode {

    public static LatLng nameToLatLng(String address) {
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                address + "&key=" + R.string.GeocodeAPIKey;
        JSONObject geoJSON;
        try {
            URL url = new URL(request);

            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext()) {
                str += scan.nextLine();
            }
            scan.close();
            System.err.println(str);
            geoJSON = new JSONObject(str);
            System.err.println(geoJSON);
            JSONObject result = geoJSON.getJSONArray("result").getJSONObject(0);
            JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
            return new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
