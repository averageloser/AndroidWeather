
package com.example.tj.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.tj.weather.WeatherActivity;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearWeatherListenerService extends WearableListenerService {
    public WearWeatherListenerService() {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
        intent.putExtra("message", new String(messageEvent.getData()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
