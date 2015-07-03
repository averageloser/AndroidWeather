package com.example.tj.weather.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tj.weather.WeatherActivity;

public class TimeZoneReceiver extends BroadcastReceiver {
    public TimeZoneReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /*Create an Intent to launch the WeatherActivity.*/
        Intent weatherIntent = new Intent(context, WeatherActivity.class);

        //If the activity is already at the top of the stack and running, don't restart it.
        weatherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Start the Activity.
        context.startActivity(weatherIntent);

        Log.i("receiver", "fired receiver");
    }
}
