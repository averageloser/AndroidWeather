package com.example.tj.weather.util;

import com.example.tj.weather.model.WeatherForecast;
import com.example.tj.weather.model.WeatherLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by tj on 3/24/15.   This class contains methods used by the WeatherModel.  They are here
 * to reduce code clutter in the model.
 */
public final class WeatherModelHelper {

    /**Helper method to download json data and return as a string.
     *
     * @param url - the url for the json web request.
     * @return - String containing all of the json data.
     * @throws java.io.IOException
     */
    public String downloadJSONData(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        StringBuilder sb = null;

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = null;

            sb = new StringBuilder();

        /*Data has been successfully returned from the server, but we don't know what it is yet.
         It might be a json response with a 404, so
         continue.*/
            if (conn.getResponseCode() == 200) {

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
        } finally {
            conn.disconnect();
        }
        return sb.toString();
    }

    /***********Creates a stub WeatherLocation informing the user that there was an error with their
     * request, with either input or the network .**/
    public WeatherForecast weatherForecastError(String data) {
        // add a stub location informing the user of the error.
        WeatherForecast forecast = new WeatherForecast();
        forecast.setDate("error");
        forecast.setHumidity("error");
        forecast.setIcon("w50d");
        forecast.setTempDay("error");
        forecast.setTemperature("error");
        forecast.setTemperatureHigh("error");
        forecast.setTemperatureLow("error");
        forecast.setTempNight("error");
        forecast.setTime("error");
        forecast.setWindDirection("error");
        forecast.setWindSpeed("error");
        forecast.setDescription("Location Not found or network connection issue. \n" +
                "Check your location and or Internet connection and try again.");
        return forecast;
    }

    /**
     * Gets the cardinal direction of wind at a specific angle.
      * @param angle
     * @return
     */
    public String getWindDirectionFromDegrees(int angle) {

        return "100";
    }

    //returns a human readable date from a unix timestamp, in the format of day/month.
    public String getDateFromTimestamp(long unixTimestamp) {
        // the timestamp is valid, convert it to a string and set it in the forecast.
            SimpleDateFormat formatter = new SimpleDateFormat("", Locale.US);

            formatter.applyPattern("dd/MMM");
            String date = formatter.format(new Date(unixTimestamp * 1000));

        return date;
    }

    public String getTimeFromTimestamp(long unixTimestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("", Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());

        formatter.applyPattern("hh:mm a");
        String time = formatter.format(new Date(unixTimestamp * 1000));

        return time;
    }
}
