package group22.quikschedule;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import group22.quikschedule.Maps.Directions;
import group22.quikschedule.Maps.Geocode;

/**
 * Class: group22.quikschedule.GeocodeTest
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/25/16
 *
 * Description: Class used for testing functionality of Directions
 *
 * @author Tynan Dewes
 */
public class GeocodeTest extends Geocode {

    final static double ZERO = 0.0;
    final static double LAT = 32.8787741;
    final static double LONG = -117.2375612;

    final static String SIMPLE_JSON = "{\n" +
            "   \"results\" : [\n" +
            "      {\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : 0.0,\n" +
            "               \"lng\" : 0.0\n" +
            "            }\n" +
            "         },\n" +
            "         \"partial_match\" : true,\n" +
            "         \"place_id\" : \"ChIJ2bZbJcQG3IAR2I5SygUESJ0\",\n" +
            "         \"types\" : [ \"route\" ]\n" +
            "      }\n" +
            "   ],\n" +
            "}";

    final static String JSON = "{\n" +
            "   \"results\" : [\n" +
            "      {\n" +
            "         \"address_components\" : [\n" +
            "            {\n" +
            "               \"long_name\" : \"Library Walk\",\n" +
            "               \"short_name\" : \"Library Walk\",\n" +
            "               \"types\" : [ \"route\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"La Jolla\",\n" +
            "               \"short_name\" : \"La Jolla\",\n" +
            "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"San Diego\",\n" +
            "               \"short_name\" : \"San Diego\",\n" +
            "               \"types\" : [ \"locality\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"San Diego County\",\n" +
            "               \"short_name\" : \"San Diego County\",\n" +
            "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"California\",\n" +
            "               \"short_name\" : \"CA\",\n" +
            "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"United States\",\n" +
            "               \"short_name\" : \"US\",\n" +
            "               \"types\" : [ \"country\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"92161\",\n" +
            "               \"short_name\" : \"92161\",\n" +
            "               \"types\" : [ \"postal_code\" ]\n" +
            "            }\n" +
            "         ],\n" +
            "         \"formatted_address\" : \"Library Walk, San Diego, CA 92161, USA\",\n" +
            "         \"geometry\" : {\n" +
            "            \"bounds\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : 32.8802738,\n" +
            "                  \"lng\" : -117.237512\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : 32.8769727,\n" +
            "                  \"lng\" : -117.2375693\n" +
            "               }\n" +
            "            },\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : 32.8787741,\n" +
            "               \"lng\" : -117.2375612\n" +
            "            },\n" +
            "            \"location_type\" : \"GEOMETRIC_CENTER\",\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : 32.8802738,\n" +
            "                  \"lng\" : -117.2361916697085\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : 32.8769727,\n" +
            "                  \"lng\" : -117.2388896302915\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"partial_match\" : true,\n" +
            "         \"place_id\" : \"ChIJ2bZbJcQG3IAR2I5SygUESJ0\",\n" +
            "         \"types\" : [ \"route\" ]\n" +
            "      }\n" +
            "   ],\n" +
            "   \"status\" : \"OK\"\n" +
            "}";

    final static String INVALID_JSON = "{\n" +
            "   \"results\" : [\n" +
            "      {\n" +
            "         \"not_geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : 0.0,\n" +
            "               \"lng\" : 0.0\n" +
            "            }\n" +
            "         },\n" +
            "         \"partial_match\" : true,\n" +
            "         \"place_id\" : \"ChIJ2bZbJcQG3IAR2I5SygUESJ0\",\n" +
            "         \"types\" : [ \"route\" ]\n" +
            "      }\n" +
            "   ],\n" +
            "}";

    @Test
    public void testGetJsonSimple() throws JSONException {
        LatLng res = new LatLng( ZERO, ZERO );
        assertThat( Geocode.getJson( SIMPLE_JSON ), is( res ) );
    }

    @Test
    public void testGetJson() throws JSONException {
        LatLng res = new LatLng( LAT, LONG );
        assertThat( Geocode.getJson( JSON ), is( res ) );
    }

    @Test(expected=JSONException.class)
    public void testJsonException() throws JSONException {
        Geocode.getJson( INVALID_JSON );
    }

    @Test
    public void testGetJsonWithNullString() throws JSONException {
        String nil = null;
        LatLng res = null;
        assertThat( Geocode.getJson( nil ), is( res ) );
    }

    @Test
    public void testGetJsonWithEmptyString() throws JSONException {
        String nil = "";
        LatLng res = null;
        assertThat( Geocode.getJson( nil ), is( res ) );
    }
}
