package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;
import javax.net.ssl.HttpsURLConnection;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.json.*;

import group22.quikschedule.R;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by christoph on 10/20/16.
 */

public class Directions {

    private LatLng home;
    public static List<List<HashMap<String, String>>> staticDirections;

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
    private String buildURLRequest(LatLng start, LatLng end) {
        String startStr = parseLatLong( start );
        String endStr = parseLatLong( end );
        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                startStr + "&destination=" + endStr + "&key=" +
                getApplicationContext().getResources().getString( R.string.DirectionsAPIKey ) +
                "&" + getApplicationContext().getResources().getString( R.string.TransitMode );
        return request;
    }

    private static String parseLatLong( LatLng parse ) {
        String latLng = parse.toString();
        String[] split = latLng.split( "\\(" );
        split = split[1].split( "\\)" );
        return split[0];
    }

    public void setHome(LatLng home) {
        this.home = home;
    }

    public List<List<HashMap<String, String>>> makeRequest(LatLng start, LatLng dest) {
        String request = buildURLRequest(start, dest);
        Retrieval asyncTask = (Retrieval) new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                result = "{ " + result + " }";
                staticDirections = getJson( result );
            }
        }).execute( request );
        return staticDirections;
    }

    private List<List<HashMap<String, String>>> getJson( String jsonStr ) {
        List directions = new ArrayList();
        JSONObject dirJSON = null;
        try {
            dirJSON = new JSONObject( jsonStr );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray routesArr, legsArr;
        JSONObject routes, legs, duration;
        int time = 0;
        try {
            routesArr = dirJSON.getJSONArray( "routes" );
            routes = routesArr.getJSONObject( 0 );
            legsArr = routes.getJSONArray( "legs" );
            legs = legsArr.getJSONObject( 0 );
            duration = legs.getJSONObject( "duration" );
            time = duration.getInt( "value" );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.err.println( "Time: " + time );

        List<List<HashMap<String, String>>> routesList = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        /*
        try {
            jRoutes = dirJSON.getJSONArray( "routes" );
            System.err.println( "I'm here at least" );
            // Traversing all routes
            for( int i = 0; i < jRoutes.length() ; i++ ) {
                System.err.println( "Made it here" );
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
        */

        return routesList;
    }

    public static final String codeToName(String code) {
        return converter.get(code);
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

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
