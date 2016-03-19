package com.example.tj.weather.ui;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.example.tj.weather.util.WeatherActivityHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

/**
 * Created by tj on 7/29/2015.
 */
public class MapLocationView extends MapView {
    private Context context;
    private WeatherActivityHelper weatherActivityHelper;
    private GoogleMap googleMap;
    private Marker location;

    public MapLocationView(Context context, WeatherActivityHelper weatherActivityHelper) {
        super(context);

        this.context = context;

        this.weatherActivityHelper = weatherActivityHelper;

        getMapAsync(weatherActivityHelper);
    }

    public void setGoogleMap(GoogleMap map) {
        googleMap = map;
    }

    public void moveMarker(String city, String statOrCountry) {
        new AsyncTask<String[], Void, LatLng>() {

            @Override
            protected LatLng doInBackground(String[]... params) {
                String[] names = params[0];
                String city = names[0];
                String stateOrCountry = names[1];
                Address addr = null;
                LatLng temp = null;

                Geocoder gc = new Geocoder(context);
                try {
                    addr = gc.getFromLocationName(city + ", " + stateOrCountry, 1).get(0);

                    temp = new LatLng(addr.getLatitude(), addr.getLongitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return temp;
            }

            @Override
            protected void onPostExecute(LatLng latLng) {
                //move the markeer here.
                location = googleMap.addMarker(new MarkerOptions().position(latLng));

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //googleMap.animateCamera(CameraUpdateFactory.zoomTo(50000));
            }
        }.execute(new String[] {city, statOrCountry});
    }//end moveMarker()


}
