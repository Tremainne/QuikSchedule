package group22.quikschedule.Maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ty Dewes on 11/16/16.
 */

public interface GeoCodeListener {
    public void setStart(LatLng start);
    public void setEnd(LatLng end);
    public void onLatLngComplete();
    public void onComplete();
}
