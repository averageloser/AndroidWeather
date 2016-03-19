package com.example.tj.weather.model;

import android.util.Log;

import com.example.tj.weather.util.WeatherModelHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Tom Farrell on 3/21/15.
 * This class is the model for a the weather application, which has methods to download and
 * parse a JSON response for OpenWeatherMap.org.  Single and extended hourly forecasts are supported.
 */
public class WeatherModel {
    /** Some of these aren't used right away, but will be important when presenting data in the ui **/
    private String degreeSymbol; //Farenheit or celcius.
    private String unit = "imperial"; //Unit of measurement.  Imperial is the default. *******Make this an enum later*********
    private String cityName = "new york"; //Default city.
    private String countryOrState = "ny"; //Default state.
    private WeatherModelHelper weatherModelHelper = new WeatherModelHelper(); /* Common utility methods the model
    uses so as to not clutter itself with code that could best be put somewhere else. */

    private final static String API_KEY = "dfc977b030e99d11937349a32db3f4fa";

    /* Used for presenting temp data in the UI. Not implemented.*/
    public String getDegreeSymbol() {
        return degreeSymbol;
    }

    /* If I don't forget, I will make this an enum. */
    public void setUnit(String unit) {
        if (unit.equals("metric")) {
            degreeSymbol = "C";
            unit = "metric";
        } else {
            degreeSymbol = "F";
            unit = "imperial";
        }
    }

    public String getUnit() {
        return unit;
    }

    //gets the weather model util helper class.
    public WeatherModelHelper getWeatherModelHelper() {
        return weatherModelHelper;
    }

    /**
     * Returns a WeatherLocation object containing the forecast for a single day.
     *
     * @param city           - The city of the location.
     * @param countryOrState - The state of the location.
     * @return - Weatherlocation object containing single forecast.
     * @throws IOException
     * @throws JSONException
     */
    public WeatherLocation getCurrentForecast(String city, String countryOrState) throws IOException,
            JSONException {

        cityName = city;

        this.countryOrState = countryOrState;

        String url = "http://api.openweathermap.org/data/2.5/weather?q="
                + URLEncoder.encode(city, "UTF-8")
                +","
                +URLEncoder.encode(countryOrState, "UTF-8")
                +"&units="
                + URLEncoder.encode(unit, "UTF-8")
                + "&APPID=" + API_KEY;

        /*
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + countryOrState +
                "&units=" + unit + "&APPID=" + API_KEY;
        */

        return parseJSONDataCurrentForecast(weatherModelHelper.downloadJSONData(url));
    }


    /**
     * ***Returns a weekly forecast of weather for a particular location.
     *
     * @param city           - The city of the location.
     * @param countryOrState - The state of the location.
     * @return -  WeatherLocation object containing multiple daily Forecast information.
     * @throws IOException
     * @throws JSONException
     */
    public WeatherLocation getWeeklyForecastNoHourly(String city, String countryOrState) throws IOException,
            JSONException {

        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q="
                +URLEncoder.encode(city, "UTF-8")
                +","
                +URLEncoder.encode(countryOrState, "UTF-8")+"&mode=json"
                +"&units="+URLEncoder.encode(unit, "UTF-8")+"&cnt=7"
                + "&APPID=" + API_KEY;

        /*
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "," + countryOrState +
                "&mode=json" + "&units=" + unit + "&cnt=7" + "&APPID=" + API_KEY;
        */

        cityName = city;

        this.countryOrState = countryOrState;

        Log.i("city", city);

        return parseJSONDataWeeklyForecastNoHourly(weatherModelHelper.downloadJSONData(url));
    }

    /*
    Currently not used.
     */
    public WeatherLocation getWeeklyForecastHourly(String city, String countryOrState) throws IOException,
    JSONException {
        String url = "http://api.openweathermap.org/data/2.5/forecast/weather?q="+URLEncoder.encode(city, "UTF-8")
                +","+URLEncoder.encode(countryOrState, "UTF-8")+"&units="+URLEncoder.encode(unit, "UTF-8")+"&cnt=10";

        cityName = city;

        this.countryOrState = countryOrState;

        Log.i("city", city);

        return parseJSONDataWeeklyForecastHourly(weatherModelHelper.downloadJSONData(url));
    }


    /**
     * **************Parses the json data for a single location *******************
     *
     * @param data - The String containing the json data.
     * @return WeatherLocation object containing forecast.
     * @throws JSONException
     */
    private WeatherLocation parseJSONDataCurrentForecast(String data) throws JSONException {
        /*
        sunrise, sunset, date, time, description, icon, temp, temp_max, temp_min, humidity, wind speed.
         */
        WeatherLocation location = new WeatherLocation();
        location.setType("current"); // the type of forecast data this location holds.

        WeatherForecast forecast = new WeatherForecast();

        /* The server responded with http response code 200, so there is some sort of data. Get it
        * and figure out what it is. */
        JSONObject mainObject = new JSONObject(data);

        /* cod is the response for the query.  200 is valid data.  Anything else represents a problem,
        a network issue, location not found, etc.  If there is a problem, recommend that the user
        try again by checking the location, network status, etc. */
        String cod = mainObject.getString("cod");

        if (!cod.equals("200")) {
            location.addWeatherForecast(weatherModelHelper.weatherForecastError(data));
        } else {//Valid data exists, so parse it.
            //Every result has a city name, but not all location objects do, so this happens first.
            String name = mainObject.optString("name");

             /* If the correct name was returned, assign the country or state name. */
            if (name.toLowerCase().equals(cityName.toLowerCase())) {
                location.setCity(cityName);
                location.setCountryOrState(countryOrState);
            }

            /*********Sunrise and Sunset for the location, if available*************/
            JSONObject sys = mainObject.optJSONObject("sys");

            if (sys != null && !sys.optString("sunrise").isEmpty() && !sys.optString("sunset").isEmpty()) {

                long sunrise = mainObject.optLong("sunrise");

                long sunset = mainObject.optLong("sunset");

                location.setSunrise(weatherModelHelper.getDateFromTimestamp(sunrise) + weatherModelHelper.getTimeFromTimestamp(sunrise));

                location.setSunset(weatherModelHelper.getDateFromTimestamp(sunset) + weatherModelHelper.getTimeFromTimestamp(sunset));
            }

            /******************The date and time of the forecast.*************/
            long unixTimestamp = mainObject.optLong("dt");

            forecast.setDate(weatherModelHelper.getDateFromTimestamp(unixTimestamp));
            forecast.setTime(weatherModelHelper.getTimeFromTimestamp(unixTimestamp));

            /******************The weather array*******************/
            JSONArray weather = mainObject.optJSONArray("weather");

            if (weather != null) {
                JSONObject obj = weather.getJSONObject(0);

                forecast.setDescription(obj.optString("description"));
                forecast.setIcon(obj.optString("icon"));
            }

            /*************The main details******************/
            JSONObject mainDetails = mainObject.optJSONObject("main");

            if (mainDetails != null) {
                int temp = (int) mainDetails.optDouble("temp");

                int tempHigh = (int) mainDetails.optDouble("temp_max");

                int tempLow = (int) mainDetails.optDouble("temp_min");

                int humidity = (int) mainDetails.optDouble("humidity");

                forecast.setTemperature(String.valueOf(temp));
                forecast.setTemperatureHigh(String.valueOf(tempHigh));
                forecast.setTemperatureLow(String.valueOf(tempLow));
                forecast.setHumidity(String.valueOf(humidity));
            }

            /**************Wind details****************/
            JSONObject wind = mainObject.optJSONObject("wind");

            if (wind != null) {
                DecimalFormat decimalFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                decimalFormatter.setMaximumFractionDigits(2);

                String windSpeed = decimalFormatter.format(wind.optDouble("speed"));

                //Not used.  Might not use ever.
                String windDirection = weatherModelHelper.getWindDirectionFromDegrees(wind.optInt("deg"));

                forecast.setWindSpeed(windSpeed);
                forecast.setWindDirection(windDirection);
            }
        }

        //Log.i("forecast", forecast.toString());

        location.addWeatherForecast(forecast);

        return location;
    }

    /**
     * Parses the json data for a single location, returning an extended forecast of as many days
     * as are available with no hourly information.
     *
     * @param data - The json data to parse.
     * @return - A WeatherLocation object with forecast data.
     * @throws JSONException
     */

    private WeatherLocation parseJSONDataWeeklyForecastNoHourly(String data) throws JSONException {
        /* date,  icon, tempday, tempNight, windSpeed */
        WeatherLocation location = new WeatherLocation();
        location.setType("extended"); // the type of forecast data this location holds.

        /* The server responded with http response code 200, so there is some sort of data. Get it
        * and figure out what it is. */
        JSONObject mainObject = new JSONObject(data);

        /* cod is the response for the query.  200 is valid data.  Anything else represents a problem,
        a network issue, location not found, etc.  If there is a problem, recommend that the user
        try again by checking the location, network status, etc. */
        String cod = mainObject.getString("cod");

        if (!cod.equals("200")) {
            location.addWeatherForecast(weatherModelHelper.weatherForecastError(data));
        } else {//Valid data exists, so parse it.
            //Set the Location details that we want, if available.

            //the city.
            JSONObject city = mainObject.optJSONObject("city");

            if (city != null) {
                String name = city.optString("name");
                if (name.toLowerCase().equals(cityName.toLowerCase())) {
                    location.setCountryOrState(name);
                }
            }

            //get the array of forecasts.
            JSONArray list = mainObject.optJSONArray("list");

            //This definitely represents the accurate number of forecasts.
            int numForecasts = list.length();

            //start pulling out the data from the forecasts and adding to the location.
            for (int i = 0; i < numForecasts; i++) {
                WeatherForecast forecast = new WeatherForecast();

                //pull out each object from the list.
                JSONObject listObject = list.getJSONObject(i);

                //The date of the forecast as a unix timestamp.
                long unixTimestamp = listObject.optLong("dt");

                // the timestamp is valid, convert it to a string and set it in the forecast.
                forecast.setDate(weatherModelHelper.getDateFromTimestamp(unixTimestamp));

                //Now the temperature for the forecast.
                JSONObject temperature = listObject.optJSONObject("temp");

                if (temperature != null) {
                    int tempDay = (int) temperature.optDouble("day");

                    int tempNight = (int) temperature.optDouble("night");

                    forecast.setTempDay(String.valueOf(tempDay));
                    forecast.setTempNight(String.valueOf(tempNight));
                }

                //The humidity.
                String humidity = listObject.optString("humidity");

                forecast.setHumidity(humidity);

                /************The Weather object.**************/
                JSONArray weather = listObject.optJSONArray("weather");

                if (weather != null) {
                    int weatherSize = weather.length();

                    for (int j = 0; j < weatherSize; j++) {
                        //pull out each object and set their values.  There is likely only one.
                        JSONObject obj = weather.getJSONObject(j);

                        String description = obj.optString("description");

                        String icon = obj.optString("icon");

                        forecast.setDescription(description);
                        forecast.setIcon(icon);
                    }
                }

                /************the wind details.*************/
                int windSpeed = (int) listObject.optDouble("speed");

                String windDirection = weatherModelHelper.getWindDirectionFromDegrees(listObject.optInt("deg"));

                forecast.setWindSpeed(String.valueOf(windSpeed));
                forecast.setWindDirection(windDirection);

                //Log.i("Forecast_Weekly", forecast.toString());

                location.addWeatherForecast(forecast);
            }
        }

        return location;
    }

    /* Just like it sounds.  Parses and returns data for a weekly forecast with hourly information. */
    private WeatherLocation parseJSONDataWeeklyForecastHourly(String data) throws IOException,
            JSONException {
        WeatherLocation location = new WeatherLocation();
        location.setType("hourly"); // the type of forecast data this location holds.

        /* The server responded with http response code 200, so there is some sort of data. Get it
        * and figure out what it is. */
        JSONObject mainObject = new JSONObject(data);

        /* cod is the response for the query.  200 is valid data.  Anything else represents a problem,
        a network issue, location not found, etc.  If there is a problem, recommend that the user
        try again by checking the location, network status, etc. */
        String cod = mainObject.getString("cod");

        if (!cod.equals("200")) {
            location.addWeatherForecast(weatherModelHelper.weatherForecastError(data));
        } else {//Valid data exists, so parse it.
            //Set the Location details that we want, if available.

            //the city.
            JSONObject city = mainObject.optJSONObject("city");

            if (city != null) {
                String name = city.optString("name");
                if (name.toLowerCase().equals(cityName.toLowerCase())) {
                    location.setCountryOrState(name);
                }
            }

            //the List array containing the forecasts.
            JSONArray list = mainObject.optJSONArray("list");

            //the number of forecasts.
            int numForecasts = list.length();

            //pull ouf the details of each forecast and create WeatherForecast objects.
            for (int i = 0; i < numForecasts; i++) {
                WeatherForecast forecast = new WeatherForecast();

                //get each forecast object from the list.
                JSONObject listObject = list.optJSONObject(i);

                if (listObject != null) {
                    //valid date.  continue.

                    /********the time and date***************/
                    long unixTimestamp = listObject.getLong("dt");

                    forecast.setDate(weatherModelHelper.getDateFromTimestamp(unixTimestamp));
                    forecast.setTime(weatherModelHelper.getTimeFromTimestamp(unixTimestamp));

                    /*****************the main object****************/
                    JSONObject main = listObject.optJSONObject("main");

                    if (main != null) {
                        //the temperature.
                        int tempDay =  (int) main.optDouble("temp");

                        //the min temp
                        int tempNight = (int) main.optDouble("temp_min");

                        //the humidity.
                        String humidity = main.optString("humidity");

                        forecast.setTempDay(String.valueOf(tempDay));
                        forecast.setTempNight(String.valueOf(tempNight));
                        forecast.setHumidity(humidity);
                    }

                    /***********The weather Array**************/
                    JSONArray weather = listObject.optJSONArray("weather");

                    if (weather != null) {

                        int numObjects = weather.length();

                        for (int j = 0; j < numObjects; j++) {
                            JSONObject obj = weather.getJSONObject(j);

                            if (obj != null) {
                                //set the description.
                                String description = obj.optString("description");

                                //set the icon.
                                String icon = obj.optString("icon");

                                forecast.setDescription(description);
                                forecast.setIcon(icon);
                            }
                        }
                    }

                    /*****************wind***************/
                    JSONObject wind = listObject.optJSONObject("wind");

                    int windSpeed = (int) wind.optDouble("speed");

                    String windDirection = wind.getString("deg");

                    forecast.setWindSpeed(String.valueOf(windSpeed));
                    forecast.setWindDirection(windDirection);

                    location.addWeatherForecast(forecast);
                }

                //Log.i("forecast", forecast.toString());
            }
        }
        return location;
    }
}
