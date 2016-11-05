package group22.quikschedule.Maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import org.json.*;


import java.util.List;

import group22.quikschedule.R;

/**
 * Created by christoph on 10/31/16.
 */

public class Geocode {

    public static LatLng getStaticLatLng() {
        return staticLatLng;
    }

    private static LatLng staticLatLng;

    public static void nameToLatLng(String address, final MapsActivity map, final boolean isStart) {
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                address + "&key=AIzaSyBmLBTq2_NcacunMNnPlEPL5fIQj38bIRs";
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
                @Override
                public void processFinish(String result) {
                    Log.d("geocode", "request completed");
                    if (isStart) {
                        map.setStart(getJson(result));
                    } else {
                        map.setEnd(getJson(result));
                    }
                    map.onGeocodeComplete();
                }
            });
        asyncTask.execute(request);

    }

    public static LatLng getJson(String json) {
        try {
            JSONObject geoJSON = new JSONObject(json);
            JSONObject result = geoJSON.getJSONArray("results").getJSONObject(0);
            JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
            return new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
