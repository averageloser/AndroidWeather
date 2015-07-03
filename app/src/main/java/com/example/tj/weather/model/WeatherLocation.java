package com.example.tj.weather.model;

import com.example.tj.weather.model.WeatherForecast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tj on 3/24/15.
 * This class represents a specific location and a list of WeatherForecast objects for it.
 */
public class WeatherLocation {
    private String city;
    private String countryOrState;
    private String sunrise;
    private String sunset;
    private String type; //the type of forecasts this location contains, current, extended, hourly.
    private List<WeatherForecast> weatherForecastList;

    public WeatherLocation() {
       weatherForecastList = new LinkedList<>();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryOrState() {
        return countryOrState;
    }

    public void setCountryOrState(String countryOrState) {
        this.countryOrState = countryOrState;
    }

    public List<WeatherForecast> getWeatherForecastList() {
        return weatherForecastList;
    }

    public void addWeatherForecast(WeatherForecast forecast) {
        weatherForecastList.add(forecast);
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String toString() {
        return "\nCity: " + city + "\n+State :" + countryOrState + "\nSunrise: " + sunrise + "\nSunset: " + sunset;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
