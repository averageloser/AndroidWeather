package com.example.tj.weather.database;

/**
 * Created by tj on 6/23/2015.
 * This class reprents a location in the database containing a city and state.
 */
public class DBLocation {
    private String city;
    private String stateOrCountry;

    public DBLocation() {

    }
    public DBLocation(String city, String stateOrCountry) {
        setCity(city);
        setStateOrCountry(stateOrCountry);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateOrCountry() {
        return stateOrCountry;
    }

    public void setStateOrCountry(String countryOrState) {
        this.stateOrCountry = countryOrState;
    }

    public String toString() {
        return city + ", " + stateOrCountry;
    }
}
