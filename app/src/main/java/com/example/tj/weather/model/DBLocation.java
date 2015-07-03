package com.example.tj.weather.model;

/**
 * Created by tj on 6/23/2015.
 * This class reprents a location in the database containing a city and state.
 */
public class DBLocation {
    private String city;
    private String countryOrState;

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
}
