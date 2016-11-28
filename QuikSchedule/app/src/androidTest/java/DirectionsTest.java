import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import group22.quikschedule.Maps.Directions;

/**
 * Class: DirectionsTest
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/25/16
 *
 * Description: Class used for testing functionality of Directions
 *
 * @author Tynan Dewes
 */
public class DirectionsTest extends Directions {

    final static int INT_ZERO = 0;
    final static double ZERO = 0.0;
    final static double LAT = 32.880254;
    final static double LONG = -117.237643;
    // From library walk to Geisel (deal w/ rounding)
    final static double TEST_DECODE_LAT = 32.88212;
    final static double TEST_DECODE_LONG = -117.24011;
    final static double TEST_DECODE_LAT_TWO = 32.88175;
    final static double TEST_DECODE_LONG_TWO = -117.23968;
    // Short path for decoding test
    final static double TEST_DECODE_JSON_LAT = 32.8792;
    final static double TEST_DECODE_JSON_LONG = -117.23757;
    final static int TRANSIT = 0;
    final static int DRIVING = 1;

    final String json = "{\n" +
            "   \"geocoded_waypoints\" : [\n" +
            "      {\n" +
            "         \"geocoder_status\" : \"OK\",\n" +
            "         \"place_id\" : \"ChIJGfAM-MMG3IARmKYeTtt2Tsc\",\n" +
            "         \"types\" : [ \"route\" ]\n" +
            "      },\n" +
            "      {\n" +
            "         \"geocoder_status\" : \"OK\",\n" +
            "         \"place_id\" : \"ChIJGfAM-MMG3IARmKYeTtt2Tsc\",\n" +
            "         \"types\" : [ \"route\" ]\n" +
            "      }\n" +
            "   ],\n" +
            "   \"routes\" : [\n" +
            "      {\n" +
            "         \"bounds\" : {\n" +
            "            \"northeast\" : {\n" +
            "               \"lat\" : 32.8792018,\n" +
            "               \"lng\" : -117.2375693\n" +
            "            },\n" +
            "            \"southwest\" : {\n" +
            "               \"lat\" : 32.8792018,\n" +
            "               \"lng\" : -117.2375693\n" +
            "            }\n" +
            "         },\n" +
            "         \"copyrights\" : \"Map data ©2016 Google\",\n" +
            "         \"legs\" : [\n" +
            "            {\n" +
            "               \"distance\" : {\n" +
            "                  \"text\" : \"1 ft\",\n" +
            "                  \"value\" : 0\n" +
            "               },\n" +
            "               \"duration\" : {\n" +
            "                  \"text\" : \"1 min\",\n" +
            "                  \"value\" : 0\n" +
            "               },\n" +
            "               \"end_address\" : \"Library Walk, San Diego, CA 92161, USA\",\n" +
            "               \"end_location\" : {\n" +
            "                  \"lat\" : 32.8792018,\n" +
            "                  \"lng\" : -117.2375693\n" +
            "               },\n" +
            "               \"start_address\" : \"Library Walk, San Diego, CA 92161, USA\",\n" +
            "               \"start_location\" : {\n" +
            "                  \"lat\" : 32.8792018,\n" +
            "                  \"lng\" : -117.2375693\n" +
            "               },\n" +
            "               \"steps\" : [\n" +
            "                  {\n" +
            "                     \"distance\" : {\n" +
            "                        \"text\" : \"1 ft\",\n" +
            "                        \"value\" : 0\n" +
            "                     },\n" +
            "                     \"duration\" : {\n" +
            "                        \"text\" : \"1 min\",\n" +
            "                        \"value\" : 0\n" +
            "                     },\n" +
            "                     \"end_location\" : {\n" +
            "                        \"lat\" : 32.8792018,\n" +
            "                        \"lng\" : -117.2375693\n" +
            "                     },\n" +
            "                     \"html_instructions\" : \"Head\\u003cdiv style=\\\"font" +
            "-size:0.9em\\\"\\u003eRestricted usage road\\u003c/div\\u003e\",\n" +
            "                     \"polyline\" : {\n" +
            "                        \"points\" : \"_vtgEx}pjU\"\n" +
            "                     },\n" +
            "                     \"start_location\" : {\n" +
            "                        \"lat\" : 32.8792018,\n" +
            "                        \"lng\" : -117.2375693\n" +
            "                     },\n" +
            "                     \"travel_mode\" : \"DRIVING\"\n" +
            "                  }\n" +
            "               ],\n" +
            "               \"traffic_speed_entry\" : [],\n" +
            "               \"via_waypoint\" : []\n" +
            "            }\n" +
            "         ],\n" +
            "         \"overview_polyline\" : {\n" +
            "            \"points\" : \"_vtgEx}pjU\"\n" +
            "         },\n" +
            "         \"summary\" : \"\",\n" +
            "         \"warnings\" : [],\n" +
            "         \"waypoint_order\" : []\n" +
            "      }\n" +
            "   ],\n" +
            "   \"status\" : \"OK\"\n" +
            "}";

    @Test
    public void testParseLatLong() {
        LatLng test = new LatLng( ZERO, ZERO );
        String result = ZERO + "," + ZERO;
        assertThat( Directions.parseLatLong( test ), is( result ) );
    }

    @Test
    public void testParseLatLongWithNullLatLng() {
        LatLng nil = null;
        String nilStr = null;
        assertThat( Directions.parseLatLong( nil ), is( nilStr ) );
    }

    @Test
    public void testBuildURLRequestWithModeTransit() {
        LatLng start = new LatLng( ZERO, ZERO );
        LatLng end = new LatLng( LAT, LONG );
        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                Directions.parseLatLong( start ) + "&destination=" +
                Directions.parseLatLong( end ) +
                "&key=AIzaSyBFaJcedR1gHACBsISOnAajioMQqyVKVyg&mode=" + "transit";
        assertThat( Directions.buildURLRequest( start, end, TRANSIT ), is( request ) );
    }

    @Test
    public void testBuildURLRequestWithModeDriving() {
        LatLng start = new LatLng( ZERO, ZERO );
        LatLng end = new LatLng( LAT, LONG );
        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                Directions.parseLatLong( start ) + "&destination=" +
                Directions.parseLatLong( end ) +
                "&key=AIzaSyBFaJcedR1gHACBsISOnAajioMQqyVKVyg&mode=" + "driving";
        assertThat( Directions.buildURLRequest( start, end, DRIVING ), is( request ) );
    }

    @Test
    public void testDecodePolyLine() {
        LatLng start = new LatLng( TEST_DECODE_LAT, TEST_DECODE_LONG );
        LatLng end = new LatLng( TEST_DECODE_LAT_TWO, TEST_DECODE_LONG_TWO );
        List<LatLng> ret = new ArrayList<>();
        ret.add( start );
        ret.add( end );
        assertThat( Directions.decodePoly( "ghugEtmqjUhAuA" ), is( ret ) );
    }

    @Test
    public void testDecodePolyLineWithEmptyString() {
        List<LatLng> nil = new ArrayList<>();
        assertThat( Directions.decodePoly( "" ), is( nil ) );
    }

    @Test
    public void testGetDirectionsJson() throws JSONException {
        List<List<HashMap<String, String>>> res = new ArrayList<>();
        List<HashMap<String, String>> path = new ArrayList<>();
        HashMap<String, String> hm = new HashMap<>();
        hm.put( "lat", Double.toString( TEST_DECODE_JSON_LAT ) );
        hm.put( "lng", Double.toString( TEST_DECODE_JSON_LONG ) );
        path.add( hm );
        res.add( path );
        assertThat( Directions.getDirectionsJson( json ), is( res ) );
    }

    @Test
    public void testGetTimeJson() throws JSONException {
        int res = INT_ZERO;
        assertThat( Directions.getTimeJson( json ), is( res ) );
    }
}
