package group22.quikschedule.Maps;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.util.Maps;

import javax.net.ssl.HttpsURLConnection;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.*;

import group22.quikschedule.R;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by christoph on 10/20/16.
 */

public class Directions {

    private static LatLng home;

    public static List<List<HashMap<String, String>>> getStaticDirections() {
        return staticDirections;
    }

    public static List<List<HashMap<String, String>>> staticDirections;

    private static final HashMap<String, String> converter = new HashMap<String, String>();
    static {
        converter.put("TM", "Marshall College");
        converter.put("APM","Applied Physics and Mathematics " +
                "San Diego, CA 92161");
        converter.put("CENTR","Center Hall Library Walk, San Diego, CA 92161");
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
     * @return  The URL request string
     */
    private static String buildURLRequest(LatLng start, LatLng end) {
        String startStr = parseLatLong( start );
        String endStr = parseLatLong( end );
        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                startStr + "&destination=" + endStr +
                "&key=AIzaSyBFaJcedR1gHACBsISOnAajioMQqyVKVyg&mode=transit" ;
        return request;
    }

    /**
     * Wrapper around LatLng version, uses geocoding API to convert string to LatLng then runs
     * request
     * @param start The starting address
     * @param end The ending address
     * @return the URL request to use.
     */
    /*public static String buildURLRequest(String start, String end){
        return buildURLRequest(Geocode.nameToLatLng(start), Geocode.nameToLatLng(end));
    }*/

    /**
     * Sets the home location that is used as the default starting location.
     * @param home The home location to set in LatLng form.
     */
    private static void setHome(LatLng home) {
        Directions.home = home;
    }

    /**
     * Sets the home location that is used as the default starting location.
     * @param home The home location to set in string form
     */
    public static void setHome(String home) {
        //setHome(Geocode.nameToLatLng(home));
    }

    private static String parseLatLong( LatLng parse ) {
        String latLng = parse.toString();
        String[] split = latLng.split( "\\(" );
        split = split[1].split( "\\)" );
        return split[0];
    }

    public static void makeRequest(final LatLng start, LatLng dest,
                                                                  final MapsActivity maps) {
        String request = buildURLRequest(start, dest);
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("directions", "callback completed");
                Directions.staticDirections = getJson( result );
                maps.plotLine(staticDirections);

            }
        });

        asyncTask.execute(request);
    }

    private static List<List<HashMap<String, String>>> getJson( String jsonStr ) {



        JSONObject dirJSON = null;
        try {
            dirJSON = new JSONObject( jsonStr );
            Log.d("Directions", jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray routesArr, legsArr;
        JSONObject routes, legs = null, duration;
        int time = 0;
        try {
            Log.d("Directions", dirJSON.toString());
            routesArr = dirJSON.getJSONArray( "routes" );
            routes = routesArr.getJSONObject( 0 );
            legsArr = routes.getJSONArray( "legs" );
            legs = legsArr.getJSONObject( 0 );
            duration = legs.getJSONObject( "duration" );
            time = duration.getInt( "value" );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Directions", "Time: " + time );

        List<List<HashMap<String, String>>> routesList = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        Log.d("Directions", legs.toString());
        try {
            jRoutes = dirJSON.getJSONArray( "routes" );
            Log.d("Directions", "I'm here at least" );
            // Traversing all routes
            for( int i = 0; i < jRoutes.length() ; i++ ) {
                Log.d("Directions", "Made it here" );
                jLegs = ( (JSONObject) jRoutes.get(i) ).getJSONArray( "legs" );
                List path = new ArrayList<>();

                // Traversing all legs
                for( int j = 0; j < jLegs.length(); j++ ) {
                    jSteps = ( (JSONObject) jLegs.get(j) ).getJSONArray( "steps" );

                    // Traversing all steps
                    for( int k = 0; k < jSteps.length(); k++){
                        String polyline = "";
                        polyline = (String) ( (JSONObject) ( (JSONObject) jSteps.get(k) )
                                .get( "polyline" ) ).get( "points" );
                        List<LatLng> list = decodePoly(polyline);

                        // Traversing all points
                        for( int l = 0; l < list.size(); l++ ) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put( "lat", Double.toString( (list.get(l)).latitude ) );
                            hm.put( "lng", Double.toString( (list.get(l)).longitude ) );
                            path.add( hm );
                        }
                    }
                    routesList.add( path );
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return routesList;
    }

    public static final String codeToName(String code) {
        return converter.get(code);
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


}
