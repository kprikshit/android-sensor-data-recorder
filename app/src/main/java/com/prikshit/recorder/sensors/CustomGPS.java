package  com.prikshit.recorder.sensors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * A java class implemented as a separate listener to GPS only.
 * Now adding network provider also instead of just GPS provider.
 */
 public class CustomGPS implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static Location lastLocation;
    private LocationManager locationManager;
    private boolean gpsEnabled;
    private boolean networkEnabled;
    // we are making this variable global because we don't want to allocate memory every time
    // we call the function getLastLocationInfo()
    // same thing is implemented in other listeners also
    private StringBuilder gpsData;

    /**
     * Location provider
     * either GPS or NETWORK_PROVIDER
     */
    private String gpsLocationProvider = LocationManager.GPS_PROVIDER;
    private String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

    /**
     * The default constructor for this class
     *
     * @param context
     */
    public CustomGPS(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gpsData = new StringBuilder();

        //checking which location providers are enabled and which are not
        gpsEnabled = locationManager.isProviderEnabled(gpsLocationProvider);
        networkEnabled = locationManager.isProviderEnabled(networkLocationProvider);

        locationManager.requestLocationUpdates(gpsLocationProvider, 0, 0, this);
        lastLocation = locationManager.getLastKnownLocation(gpsLocationProvider);
        // if gps has not given any location, then try the network Location provider
        if(lastLocation == null){
            // first, check whether the network is enabled or not.
            if(networkEnabled){
                lastLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("GPS", "location changed at "+ Calendar.getInstance().getTime());
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * return the last known location in a string format.
     * the delimiter should be comma (,)
     * /**
     * <p>
     *     Time is UNIX timestamp as given by the GPS
     *     Format: latitude,longitude,accuracy,altitude,speed,time
     * </p>
     * @return
     */
    public String getLastLocationInfo(){
        if(this.lastLocation != null ) {
            gpsData.setLength(0);
            gpsData.append(lastLocation.getLatitude());
            gpsData.append(",");
            gpsData.append(lastLocation.getLongitude());
            gpsData.append(",");
            gpsData.append(lastLocation.getAccuracy());
            gpsData.append(",");
            gpsData.append(lastLocation.getAltitude());
            gpsData.append(",");
            gpsData.append(lastLocation.getSpeed());
            gpsData.append(",");
            gpsData.append(lastLocation.getTime());
            return gpsData.toString();
        }
        else{
            return "-,-,-,-,-,-";
        }
    }

    public void unregisterListener() {
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    public boolean isNetworkEnabled() {
        return networkEnabled;
    }

    public void setNetworkEnabled(boolean networkEnabled) {
        this.networkEnabled = networkEnabled;
    }

    public static Location getLastLocation(){
        return lastLocation;
    }

    public static void setLastLocation(Location newLoc){
        lastLocation = newLoc;
    }


}
