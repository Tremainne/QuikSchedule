package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class: GeoCodeListener
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/16/16
 *
 * Description: Interface for defining GeoCodeListener
 *
 * @author Tynan Dewes
 * @author Christoph Steefel
 */
public interface GeoCodeListener {
    /**
     * set a start value for a route
     *
     * @param start the value to set the start to
     */
    void setStart(LatLng start);

    /**
     * set the end value for a route
     *
     * @param end the end value to set to
     */
    void setEnd(LatLng end);

    /**
     * Called when a Geocode Lookup completes, generally checks if routing should occur.
     */
    void onLatLngComplete();

    /**
     * Called when a directions lookup is complete, does something with results.
     */
    void onGeocodeListenerComplete();

    /**
     * Called if the directions lookup fails, should handle error.
     */
    void onGeocodeListenerFail();
}
