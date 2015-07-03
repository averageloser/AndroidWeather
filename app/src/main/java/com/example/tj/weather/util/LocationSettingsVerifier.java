package com.example.tj.weather.util;

import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.tj.weather.WeatherActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 6/14/2015.
 * License: public domain.
 * Utility class that verifies location services and reports back to listeners.
 * It is important to make sure that the methods in this class are not called before a
 * connection to Google Play Location Services is established.
 */
public class LocationSettingsVerifier {
    public interface LocationSettingsVerifierListener {
        void onLocationSettingsVerified();

        //Called when location settings do not meet application requirements.
        void onLocationSettingsNotVerified();
    }

    private GoogleApiClient googleApiClient;

    private List<LocationSettingsVerifierListener> listeners;

    /*The type of location request I want i.e. a low power search (network and wifi).  Used to check
    location status and also used by LocationSearchTask. */
    private LocationRequest locationLowPoweRequest;

    public LocationSettingsVerifier(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;

        listeners = new ArrayList();

         /*Create a LocationRequest object which contains a request for the type of location awareness
        desired.  I need wifi and mobile networks enabled, not gps only.  This is just
        a data object representing the type of location setting.  It is used in conjunction with a
        LocationSettingsRequest object, which is the object representing a complete location setting.
         */
        locationLowPoweRequest = LocationRequest.create();
        locationLowPoweRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    public void addLocationSettingsVerifierListener(LocationSettingsVerifierListener listener) {
        listeners.add(listener);
    }

    private void notifyListenersLocationServicesVerified() {
        for (LocationSettingsVerifierListener listener : listeners) {
            listener.onLocationSettingsVerified();
        }
    }

    private void notifyListenersLocationServicesNotVerified() {
        for (LocationSettingsVerifierListener listener : listeners) {
            listener.onLocationSettingsNotVerified();
        }
    }

    public void checkLocationServices() {
        //This is the object that holds the request for the use of low power location functionality.
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationLowPoweRequest)
                .build();

        /*Now I call checlLocationSettings() in the SettingsAPI, which returns a pending result of type
        LocationSettingsResult.  This is an async operation, so I need to set up a callback for it.*/
        PendingResult<LocationSettingsResult> result
                = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                if (!result.getLocationSettingsStates().isNetworkLocationUsable()) {
                    //Location setting are inadequate.  Notify Listeners.
                    notifyListenersLocationServicesNotVerified();
                } else {
                    //Location settings are adequate, so notify listeners.
                    notifyListenersLocationServicesVerified();
                    }
                }
        });
    }
}
