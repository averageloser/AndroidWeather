/*
 * License: Public Domain.
 */
package com.example.tj.weather.model;

/**
 * Represents a specific day at this location.  For simplicity, everything is a String.
 */
public class WeatherForecast {

    private String date;
    private String time;
    private String description; //light snow, etc.
    private String temperature;
    private String tempDay;
    private String tempNight;
    private String temperatureHigh;
    private String temperatureLow;
    private String humidity;
    private String icon;
    private String windSpeed;
    private String windDirection;

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the temperature
     */
    public String getTemperature() {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     * @return the humidity
     */
    public String getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTemperatureHigh() {
        return temperatureHigh;
    }

    public void setTemperatureHigh(String temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }

    public String getTemperatureLow() {
        return temperatureLow;
    }

    public void setTemperatureLow(String temperatureLow) {
        this.temperatureLow = temperatureLow;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String toString() {
        return "\nDate : "+date+"\nTime :"+time+"\nDesc: "+description+"\n"+"Temp: "+
                temperature+"\nHigh Temp: "+temperatureHigh+"\nLow Temp: "+temperatureLow+"\nHumidity:"+
                humidity+"\nIcon: "+icon+"\nWind Speed: "+windSpeed+"\nWin Dir: "+windDirection+"\n" +
                "Temp Day: " + tempDay + "\nTemp Night: " +tempNight;
    }

    public String getTempDay() {
        return tempDay;
    }

    public void setTempDay(String tempDay) {
        this.tempDay = tempDay;
    }

    public String getTempNight() {
        return tempNight;
    }

    public void setTempNight(String tempNight) {
        this.tempNight = tempNight;
    }
}