package group22.quikschedule.Maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.*;

/**
 * Created by christoph on 10/31/16.
 */

public class Geocode {

    static LatLng ret;

    public static void nameToLatLng(String address, final GeoCodeListener map, final boolean isStart) {
        address = address.replaceAll("\\s", "%20");
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                address + "&key=AIzaSyBmLBTq2_NcacunMNnPlEPL5fIQj38bIRs";
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("geocode", "request completed");
                try {
                    if (isStart) {
                        map.setStart(getJson(result));

                    } else {
                        map.setEnd(getJson(result));
                    }
                } catch (JSONException e) {
                    map.onFail();
                }
                map.onLatLngComplete();
            }
        });
        asyncTask.execute(request);
    }

    public static LatLng getJson(String json) throws JSONException {

        JSONObject geoJSON = new JSONObject(json);
        JSONObject result = geoJSON.getJSONArray("results").getJSONObject(0);
        JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
        return new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));


    }
}
