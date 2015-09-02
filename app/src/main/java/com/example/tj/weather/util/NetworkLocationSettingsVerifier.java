package com.example.tj.weather.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
 * Utility class that verifies network and location services and reports back to listeners.
 * It is important to make sure that the methods in this class are not called before a
 * connection to Google Play Location Services is established.
 * <p/>
 * <p/>
 */
public class NetworkLocationSettingsVerifier {
    private ConnectivityManager cm;

    public interface LocationSettingsVerifierListener {
        void onNetworkLocationSettingsVerified();

        //Called when location settings do not meet application requirements.
        void onNetworkLocationSettingsNotVerified();
    }

    private GoogleApiClient googleApiClient;

    private List<LocationSettingsVerifierListener> listeners;

    /*The type of location request I want i.e. a low power search (network and wifi).  Used to check
    location status and also used by NetworkLocationSearchTask. */
    private LocationRequest locationBalancedRequest;

    public NetworkLocationSettingsVerifier(GoogleApiClient googleApiClient, ConnectivityManager cm) {
        this.googleApiClient = googleApiClient;

        this.cm = cm;

        listeners = new ArrayList();

         /*Create a LocationRequest object which contains a request for the type of location awareness
        desired.  I need wifi and mobile networks enabled, not gps only.  This is just
        a data object representing the type of location setting.  It is used in conjunction with a
        LocationSettingsRequest object, which is the object representing a complete location setting.
         */
        locationBalancedRequest = LocationRequest.create();
        locationBalancedRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    public void addLocationSettingsVerifierListener(LocationSettingsVerifierListener listener) {
        listeners.add(listener);
    }

    private void notifyListenersLocationServicesVerified() {
        for (LocationSettingsVerifierListener listener : listeners) {
            listener.onNetworkLocationSettingsVerified();
        }
    }

    private void notifyListenersLocationServicesNotVerified() {
        for (LocationSettingsVerifierListener listener : listeners) {
            listener.onNetworkLocationSettingsNotVerified();
        }
    }

    public void checkLocationServices() {
        //This is the object that holds the request for the use of low power location functionality.
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationBalancedRequest)
                .build();

        /*Now I call checlLocationSettings() in the SettingsAPI, which returns a pending result of type
        LocationSettingsResult.  This is an async operation, so I need to set up a callback for it.*/
        PendingResult<LocationSettingsResult> result
                = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                if (isDataNetworkActive() && result.getLocationSettingsStates().isNetworkLocationUsable()) {
                    notifyListenersLocationServicesVerified();
                } else {
                    ///Location setting are inadequate.  Notify Listeners.
                    notifyListenersLocationServicesNotVerified();
                }
            }
        });
    }

    private boolean isDataNetworkActive() {
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        Log.i("NetworkInfoSize", String.valueOf(netInfo.length));

        for (NetworkInfo ni : netInfo) {
            int type = ni.getType();

            if (type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE) {
                if (ni.isConnected()) {
                    return true;
                }
            }
        }

        return false;
    }
}
