
import com.google.android.gms.maps.model.LatLng;

import group22.quikschedule.Maps.Directions;
import group22.quikschedule.Maps.Geocode;
import org.junit.Test;
import java.util.HashMap;
import java.util.List;




public class TestDirections {

    @Test
    public void testCenter() {
        Directions d = new Directions();
        List<List<HashMap<String, String>>> l = d.makeRequest(new LatLng(32.8787740,-117.2375612), new LatLng(32.8787741,
                -117.2375612));
        System.out.println(l);
        //System.out.println(Geocode.nameToLatLng(Directions.codeToName("CENTR")));
    }



}
