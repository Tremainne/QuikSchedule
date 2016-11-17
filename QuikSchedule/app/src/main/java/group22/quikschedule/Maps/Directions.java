package group22.quikschedule.Maps;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.json.*;

/**
 * Created by christoph on 10/20/16.
 */

public class Directions {

    private static LatLng home;

    public static List<List<HashMap<String, String>>> getStaticDirections() {
        return staticDirections;
    }

    public static List<List<HashMap<String, String>>> staticDirections;
    public static Integer staticTime;

    public static final HashMap<String, String> converter = new HashMap<String, String>();
    static {
        converter.put("TM", "Marshall College, La Jolla");
        converter.put("APM","Applied Physics and Mathematics " +
                "La Jolla, CA 92161");
        converter.put("CENTR","Center Hall Library Walk, La Jolla, CA 92161");
        converter.put("CSB","Cognitive Science Building, La Jolla");
        converter.put("CICC", "Copley International Conference Center, La Jolla");
        converter.put("GH", "Galbraith Hall, La Jolla");
        converter.put("HSS", "Humanities and Social Sciences, La Jolla");
        converter.put("LEDDN", "Humanities and Social Sciences, La Jolla");
        converter.put("MANDE", "Mandeville Center, La Jolla");
        converter.put("MCGIL", "McGill Hall, La Jolla");
        converter.put("PCYNH", "Pepper Canyon Hall, La Jolla");
        converter.put("PETER", "Peterson Hall, La Jolla");
        converter.put("PRICE", "Price Center, La Jolla");
        converter.put("RBC", "Robinson Building, La Jolla");
        converter.put("SEQUO", "Sequoyah Hall, La Jolla");
        converter.put("SSB", "Social Sciences Building, La Jolla");
        converter.put("SOLIS", "Solis Hall, La Jolla");
        converter.put("WLH", "Warren Lecture Hall, La Jolla");
        converter.put("YORK", "York Hall, La Jolla");
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



    /*
    /**
     * Sets the home location that is used as the default starting location.
     * @param home The home location to set in LatLng form.

    private static void setHome(LatLng home) {
        Directions.home = home;
    }

    /**
     * Sets the home location that is used as the default starting location.
     * @param home The home location to set in string form

    public static void setHome(String home) {
        //setHome(Geocode.nameToLatLng(home));
    }*/

    private static String parseLatLong( LatLng parse ) {
        String latLng = parse.toString();
        String[] split = latLng.split( "\\(" );
        split = split[1].split( "\\)" );
        return split[0];
    }

    public static void makeRequest(final LatLng start, LatLng dest,
                                                                  final MapsFragment maps) {
        String request = buildURLRequest(start, dest);
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("directions", "callback completed");
                Pair<Integer, List<List<HashMap<String, String>>>> ret = getJson( result );
                Directions.staticTime = ret.first;
                Directions.staticDirections = ret.second;
                maps.plotLine(staticTime, staticDirections);
            }
        });

        asyncTask.execute(request);
    }

    private static Pair<Integer, List<List<HashMap<String, String>>>> getJson(String jsonStr ) {

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
            // Traversing all routes
            for( int i = 0; i < jRoutes.length() ; i++ ) {
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


        return new Pair<>( time, routesList );
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
