package group22.quikschedule.Maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class: Geocode
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/31/16
 *
 * Description: Class to handle conversion of JSON objects
 *
 * @author Christoph Steefel
 */
public class Geocode {
    /**
     * Performs a Google Geocode API request to find the latitude and longitude of the address
     *
     * @param address Address to look up
     * @param map     The listener to tell the result of the address lookup.
     * @param isStart Which value to set in the listener
     */
    public static void nameToLatLng(String address, final GeoCodeListener map,
                                    final boolean isStart) {
        address = address.replaceAll("\\s", "%20");
        final String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                address + "&key=AIzaSyBmLBTq2_NcacunMNnPlEPL5fIQj38bIRs";
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("geocode", "request completed");
                try {
                    LatLng requestResult = getJson(result);
                    if (requestResult == null) {
                        map.onGeocodeListenerFail();
                        return;
                    }
                    // Used to decide whether the start or end should get set in the map.
                    if (isStart) {
                        map.setStart(requestResult);

                    } else {
                        map.setEnd(requestResult);
                    }
                } catch (JSONException e) {
                    map.onGeocodeListenerFail();
                    e.printStackTrace();
                    return;
                }
                map.onLatLngComplete();
            }
        });
        asyncTask.execute(request);
    }

    /**
     * Parses the JSON to find the latitude and longitude.
     *
     * @param json The string to parse address info from
     * @return The parsed latitude and longitude values.
     * @throws JSONException if the address isn't found or something goes wrong with the request
     */
    protected static LatLng getJson(String json) throws JSONException {

        if (json != null && json.length() != 0 ) {
            JSONObject geoJSON = new JSONObject(json);
            JSONObject result = geoJSON.getJSONArray("results").getJSONObject(0);
            JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
            return new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));
        }
        return null;

    }
}
