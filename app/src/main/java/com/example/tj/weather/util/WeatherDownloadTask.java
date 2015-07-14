package com.example.tj.weather.util;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.tj.weather.model.WeatherLocation;
import com.example.tj.weather.model.WeatherModel;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tj on 3/25/15.
 * This is a headless fragment that uses an asynctask to download weather data asynchronously.
 */
public class WeatherDownloadTask extends Fragment {

    //Interface callback to notify listeners of a completed download operation.
    public interface WeatherDownloadListener {
        void onWeatherDownloadComplete(List<WeatherLocation> weatherLocation);

        //Called if there is an exception thrown when download is attempted.
        void onWeatherDownloadError();
    }

    //The list of listeners to be notified of download operations.
    private List<WeatherDownloadListener> weatherDownloadListeners = new ArrayList<WeatherDownloadListener>();

    public WeatherDownloadTask() {
        setRetainInstance(true);
    }

    public void WeatherDownloadListener(WeatherDownloadListener listener) {
        weatherDownloadListeners.add(listener);
    }

    public void beginDownloading(String city, String countryOrState) {
        Log.i("beginDownloading", city + " " + countryOrState);
        new Worker().execute(city, countryOrState);
    }

    /*The async task that down the downloading.  onPostExecute will be called upon completion
    to notify listeners. */
    private class Worker extends AsyncTask<String, Void, List<WeatherLocation>> {

        @Override
        protected List<WeatherLocation> doInBackground(String... location) {
            String city = location[0];
            String countryOrState = location[1];

            List<WeatherLocation> weatherList = new ArrayList<WeatherLocation>();

            WeatherModel model = new WeatherModel();

            try {
                weatherList.add(model.getCurrentForecast(city, countryOrState));
                weatherList.add(model.getWeeklyForecastNoHourly(city, countryOrState));

                /* currently not used.
                weatherList.add(model.getWeeklyForecastHourly(city, countryOrState));
                */
            } catch (IOException | JSONException e) {
                Log.e("download error", e.getMessage());

                for (WeatherDownloadListener listener : weatherDownloadListeners) {
                    listener.onWeatherDownloadError();
                }
            }

            return weatherList;
        }

        @Override
        protected void onPostExecute(List<WeatherLocation> weatherLocation) {
            super.onPostExecute(weatherLocation);

            /*Make sure weatherLocation's size is > 0, because this will be called even if
            doInBackground() throws an exception.  Valid data will always be > 0. */
            Log.i("onPostExecute", "called");

            if (weatherLocation.size() > 0)

            for (WeatherDownloadListener listener : weatherDownloadListeners) {
                listener.onWeatherDownloadComplete(weatherLocation);
            }
        }
    }
}
