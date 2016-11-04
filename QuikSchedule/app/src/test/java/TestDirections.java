
import com.google.android.gms.maps.model.LatLng;

import group22.quikschedule.Maps.Directions;
import group22.quikschedule.Maps.Geocode;
import org.junit.Test;





public class TestDirections {

    @Test
    public void testCenter() {
       Directions.makeRequest(new LatLng(32.8787740,-117.2375612), new LatLng(32.8787741,
                -117.2375612));
        Geocode.nameToLatLng(Directions.codeToName("CENTR"));
        try {
            Thread.sleep(60000);
        }catch (InterruptedException e) {
            e.printStackTrace();;
        }
        System.out.println(Directions.staticDirections);
        System.out.println(Geocode.getStaticLatLng());
    }



}
