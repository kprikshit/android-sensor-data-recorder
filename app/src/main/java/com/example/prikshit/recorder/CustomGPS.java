package com.example.prikshit.recorder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationRequest;

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

    private Location lastLocation;
    private LocationManager locationManager;
    private Context appContext;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

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
        appContext = context;
        // registering the locationManager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        /**
         * <p>checking which location providers are enabled and which are not</p>
         */
        isGPSEnabled = locationManager.isProviderEnabled(gpsLocationProvider);
        isNetworkEnabled = locationManager.isProviderEnabled(networkLocationProvider);

        // requesting location updates
        locationManager.requestLocationUpdates(gpsLocationProvider, 0, 0, this);
        lastLocation = locationManager.getLastKnownLocation(gpsLocationProvider);
        //if gps has not given any location, then try the network location
        if(lastLocation == null){
            // first, check whether the network is enabled or not.
            Log.d("using the network provider now","using network");
            if(isNetworkEnabled){
                lastLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            }
        }
    }

    /**
     * What happens when location is changed:
     * update the variable lastLocation
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
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
     * Returns the last known location
     */
    public Location getLastLocation() {
        return this.lastLocation;
    }

    /**
     * removing location requests
     */
    public void unregisterListener() {
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }
}
