package com.example.tj.weather.util;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 6/8/2015.
 * License: Public Domain.
 *
 * This class will acquire the users current location, then pass a city and state or country
 * to its listeners.
 */
public class NetworkLocationSearchTask implements LocationListener {
    private Context context;

    private GoogleApiClient googleApiClient;

    private LocationRequest locationTypeRequest;

    //List of registered listeners who will be notified of location updates.
    private List<NetworkLocationChangeListener> listeners;

    //The is the listener that callers implement to be notified of changes.
    public interface NetworkLocationChangeListener {
        void onNetworkLocationChange(String[] location);
    }

    public NetworkLocationSearchTask(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;

        /*Create a LocationRequest object which contains a request for the type of location awareness
        desired.  I need wifi and mobile networks enabled, not gps only.  This is just
        a data object representing the type of location setting.  It is used in conjunction with a
        LocationSettingsRequest object, which is the object representing a complete location setting.*/
        this.locationTypeRequest = LocationRequest.create();
        locationTypeRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        listeners = new ArrayList<NetworkLocationChangeListener>();
    }

    //Adds a new listener to the list of LocationChangeListeners.
    public void addListener(NetworkLocationChangeListener listener) {
        listeners.add(listener);
    }

    //Used by this class to respond to location updates via the FusedLocationProvider.
    @Override
    public void onLocationChanged(Location location) {
        //Get an address from this lat and long using Geocoder class.
        Geocoder gc = new Geocoder(context);

        List<Address> possibleAddresses = null;

        String city = null;

        String state = null;

        if (location != null) {
            try {
                possibleAddresses = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            } catch (IOException e) {
                Log.e("Geocoder error", e.getMessage());
            }

                    /* if we got a list of addresses, try to get city and state from it.*/
            if (possibleAddresses != null) {
                Address address = possibleAddresses.get(0);

                Log.i("LAT & LNG", String.valueOf(address.getLatitude() + " " + address.getLongitude()));

                city = address.getLocality();

                state = address.getAdminArea();

                Log.i("city and state", city + " " + state);

                String[] data = new String[2];
                data[0] = city;
                data[1] = state;

                //Notify listeners of the new data.
                if (city != null || state != null) {

                    for (NetworkLocationChangeListener l : listeners) {
                        l.onNetworkLocationChange(data);
                    }
                }
            }
        } else {
            //the location was null for some reason beyond my control.
            Log.e("Location Search Task", "Location is null");
        }

        //I don't want multiple location updates, so unregister the updatelistener.
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    //Here is where I request that location updates be sent.  I will only do a one time update.
    public void startLocationSearch() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationTypeRequest, this);
    }


}
