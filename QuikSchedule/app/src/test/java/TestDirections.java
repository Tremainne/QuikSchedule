
import group22.quikschedule.Maps.Directions;
import group22.quikschedule.Maps.Geocode;
import org.junit.Test;




public class TestDirections {

    @Test
    public void testCenter() {
        System.out.println(Geocode.nameToLatLng(Directions.codeToName("CENTR")));
    }



}
