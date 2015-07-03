package com.example.tj.weather.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.tj.weather.WeatherActivity;

/**
 * Created by tom on 6/12/2015.
 * This class notifies me when location settings change.
 */
public class LocationSettingsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("location settings", "executed");
    }
}
