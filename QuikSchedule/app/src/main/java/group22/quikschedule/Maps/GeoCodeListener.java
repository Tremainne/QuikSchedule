package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ty Dewes on 11/16/16.
 */

public interface GeoCodeListener {
    void setStart(LatLng start);
    void setEnd(LatLng end);
    void onLatLngComplete();
    void onComplete();
    void onFail();
}
