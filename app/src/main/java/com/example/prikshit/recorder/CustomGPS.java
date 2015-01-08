package com.example.prikshit.recorder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class CustomGPS implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private Location lastLocation;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private LocationRequest locationRequest;

    /**
     * Location provider
     * either GPS or NETWORK_PROVIDER
     */
    private String locationProvider = LocationManager.GPS_PROVIDER;

    public CustomGPS(Context context){
        // registering the locationManager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // requesting location updates
        locationManager.requestLocationUpdates(locationProvider,0,0,this);
        lastLocation = locationManager.getLastKnownLocation(locationProvider);
    }

    /**
     * What happens when location is changed
     * update the variable lastLocation
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
    public Location getLastLocation(){
        return this.lastLocation;
    }
}
