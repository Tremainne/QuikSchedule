package group22.quikschedule.Maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import org.json.*;



import group22.quikschedule.R;

/**
 * Created by christoph on 10/31/16.
 */

public class Geocode {

    public static LatLng getStaticLatLng() {
        return staticLatLng;
    }

    private static LatLng staticLatLng;

    public static LatLng nameToLatLng(String address) {
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                address + "&key=" + R.string.GeocodeAPIKey;
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
                @Override
                public void processFinish(String result) {
                    Log.d("geocode", "request completed");
                    //result = "{ " + result + " }";
                    Geocode.staticLatLng = getJson( result );
                }
            });
        asyncTask.execute(request);
        return staticLatLng;
    }

    public static LatLng getJson(String json) {
        try {
            JSONObject geoJSON = new JSONObject(json);
            JSONObject result = geoJSON.getJSONArray("result").getJSONObject(0);
            JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
            return new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
