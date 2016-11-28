package group22.quikschedule.Maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class: Directions
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/20/16
 *
 * Description:
 *
 * @author Christoph Steefel
 */
public class Directions {

    public static final int ZIP_LENGTH = 5;

    /**
     * Getter for the staticDirections
     *
     * @return the directions information for display
     */
    public static List<List<HashMap<String, String>>> getStaticDirections() {
        return staticDirections;
    }

    private static final String[] transitTypes = {"transit", "driving", "cycling", "walking"};

    /**
     * Getter for travel time
     *
     * @return time of travel for a route.
     */
    public static int getStaticTime() {
        return staticTime;
    }

    private static List<List<HashMap<String, String>>> staticDirections;
    private static int staticTime;

    //converter from classroom code to Location name
    public static final HashMap<String, String> converter = new HashMap<String, String>();

    // Place locations into converter
    static {
        converter.put("TM", "Marshall College, La Jolla, CA 92161");
        converter.put("APM", "Applied Physics and Mathematics " +
                "La Jolla, CA 92161");
        converter.put("CENTR", "Center Hall Library Walk, La Jolla, CA 92161");
        converter.put("CSB", "Cognitive Science Building, La Jolla, CA 92161");
        converter.put("CICC", "Copley International Conference Center, La Jolla, CA 92161");
        converter.put("GH", "Galbraith Hall, La Jolla, CA 92161");
        converter.put("HSS", "Humanities and Social Sciences, La Jolla, CA 92161");
        converter.put("LEDDN", "Humanities and Social Sciences, La Jolla, CA 92161");
        converter.put("MANDE", "Mandeville Center, La Jolla, CA 92161");
        converter.put("MCGIL", "McGill Hall, La Jolla, CA 92161");
        converter.put("PCYNH", "Pepper Canyon Hall, La Jolla, CA 92161");
        converter.put("PETER", "Peterson Hall, La Jolla, CA 92161");
        converter.put("PRICE", "Price Center, La Jolla, CA 92161");
        converter.put("RBC", "Robinson Building, La Jolla, CA 92161");
        converter.put("SEQUO", "Sequoyah Hall, La Jolla, CA 92161");
        converter.put("SSB", "Social Sciences Building, La Jolla, CA 92161");
        converter.put("SOLIS", "Solis Hall, La Jolla, CA 92161");
        converter.put("WLH", "Warren Lecture Hall, La Jolla, CA 92161");
        converter.put("YORK", "York Hall, La Jolla, CA 92161");
    }

    /**
     * Builds a URL request for use with the DirectionsAPI, using transit for now.
     *
     * @param start Strarting point of the trip to get directions for
     * @param end   ending point of the trip to get directions for
     * @return The URL request string
     */
    protected static String buildURLRequest(LatLng start, LatLng end, int transitMode) {
        String startStr = parseLatLong(start);
        String endStr = parseLatLong(end);
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                startStr + "&destination=" + endStr +
                "&key=AIzaSyBFaJcedR1gHACBsISOnAajioMQqyVKVyg&mode=" + transitTypes[transitMode];

    }

    /**
     * Parses a LatLng value into a string that can be used with Directions
     *
     * @param parse The LatLng value to parse into a string
     * @return The resulting string
     */
    protected static String parseLatLong(LatLng parse) {
        if( parse == null ) {
            return null;
        }
        String latLng = parse.toString();
        String[] split = latLng.split("\\(");
        split = split[1].split("\\)");
        return split[0];
    }

    /**
     * Makes a request to the directions API to find the amount of travel time for a specific route
     *
     * @param start The starting location of the route
     * @param dest  The ending location of the route
     * @param maps  The listener to the result, handles using the time value.
     */
    public static void makeTimeRequest(final LatLng start, LatLng dest, int transitMode,
                                       final GeoCodeListener maps) {
        if (start == null || dest == null) {
            maps.onGeocodeListenerFail();
            return;
        }
        String request = buildURLRequest(start, dest, transitMode);
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("directions", "callback completed");
                try {
                    Directions.staticTime = getTimeJson(result);
                    maps.onGeocodeListenerComplete();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    maps.onGeocodeListenerFail();
                    return;
                }
            }
        });

        asyncTask.execute(request);
    }

    /**
     * Makes a directions API request to find out the actual routing directions, used to draw the
     * polyline
     *
     * @param start The starting location for the route
     * @param dest  The ending location for the route
     * @param maps  The listener that will use the route info.
     */
    public static void makeDirectionsRequest(final LatLng start, LatLng dest, int transitMode,
                                             final GeoCodeListener maps) {
        String request = buildURLRequest(start, dest, transitMode);
        Retrieval asyncTask = new Retrieval(new Retrieval.AsyncResponse() {
            @Override
            public void processFinish(String result) {
                Log.d("directions", "callback completed");
                try {
                    Directions.staticDirections = getDirectionsJson(result);
                    if (staticDirections != null) {
                        maps.onGeocodeListenerComplete();
                    }
                    else {
                        maps.onGeocodeListenerFail();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    maps.onGeocodeListenerFail();
                }
            }
        });

        asyncTask.execute(request);
    }

    /**
     * Parses the JSON to get the time amount
     *
     * @param jsonStr The string to parse
     * @return The time value that was parsed out
     * @throws JSONException If there was an issue with the JSON
     */
    protected static int getTimeJson(String jsonStr) throws JSONException {

        JSONObject dirJSON = new JSONObject(jsonStr);
        Log.d("Time", jsonStr);

        JSONArray routesArr, legsArr;
        JSONObject routes, legs, duration;
        int time;

        Log.d("Directions", dirJSON.toString());
        routesArr = dirJSON.getJSONArray("routes");
        routes = routesArr.getJSONObject(0);
        legsArr = routes.getJSONArray("legs");
        legs = legsArr.getJSONObject(0);
        duration = legs.getJSONObject("duration");
        time = duration.getInt("value");

        return time;
    }

    /**
     * Parses the JSON to get the directions to the destination
     *
     * Code adapted from article "Google Maps Draw Route between two points using Google Directions
     * in Google Map Android API V2" w/ Author Navneet
     * URL: http://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-
     * using-google-directions-google-map-android-api-v2/
     *
     * @param jsonStr The string to parse
     * @return The directions that were parsed out
     * @throws JSONException If there was an issue with the JSON
     */
    protected static List<List<HashMap<String, String>>> getDirectionsJson(String jsonStr)
            throws JSONException {

        JSONObject dirJSON = new JSONObject( jsonStr );
        Log.d("Directions", jsonStr);

        List<List<HashMap<String, String>>> routesList = new ArrayList<>();
        JSONArray routes;
        JSONArray legs;
        JSONArray steps;

        routes = dirJSON.getJSONArray( "routes" );
        for( int i = 0; i < routes.length(); i++ ) {
            legs = ( (JSONObject) routes.get( i ) ).getJSONArray( "legs" );
            List<HashMap<String, String>> path = new ArrayList<>();

            for( int j = 0; j < legs.length(); j++ ) {
                steps = ( (JSONObject) legs.get( j ) ).getJSONArray( "steps" );

                for( int k = 0; k < steps.length(); k++ ) {
                    String polyline;
                    polyline = (String) ( (JSONObject) ( (JSONObject) steps.get( k ) )
                            .get( "polyline" ) ).get( "points" );
                    List<LatLng> list = decodePoly( polyline );

                    for (int l = 0; l < list.size(); l++) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("lat", Double.toString( (list.get( l )).latitude) );
                        hm.put("lng", Double.toString( (list.get( l )).longitude) );
                        path.add(hm);
                    }
                }
                routesList.add( path );
            }
        }

        return routesList;
    }


    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-
     * direction-api-with-java
     *
     * @param encoded The encoding of the polyline that is then decoded and converted to
     *                a list of LatLng values.
     * @return the decoded list of LatLng values to plot.
     */
    protected static List<LatLng> decodePoly(String encoded) {

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

    /**
     * Converts an address by removing numbers other than ZIP codes and converting
     * class codes with the converter.
     * @param addr The address to convert and return
     * @return The converted address
     */
    public static String convertAddress(String addr) {
        String[] arr = addr.split("[\\s,]+");
        StringBuilder result = new StringBuilder();
        for (String str : arr) {
            Log.d("Directions", str);
            boolean isNum = str.matches("\\d+");
            if (result.length() > 0) {
                result.append(" ");
            }
            // If its a number or  its a Zip code (length 5), put it back in the address.
            if (!isNum || str.length() == Directions.ZIP_LENGTH) {

                if (Directions.converter.containsKey(str)) {
                    result.append(Directions.converter.get(str));
                }
                else {
                    result.append(str);
                }
            }
        }
        Log.d("Directions", result.toString());
        return result.toString();
    }


}
