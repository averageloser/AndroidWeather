package com.example.tj.weather.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tj.weather.R;

import java.util.List;

/**
 * Created by tom on 4/27/2015.
 * This class is the adapter for the Listview that holds information on the Extended, non-hourly
 * weather forecast.
 */
public class ExtendedWeatherForecastAdapter extends ArrayAdapter<WeatherForecast> {
    private Context context;

    //the list of forecasts.
    private List<WeatherForecast> forecasts;

    public ExtendedWeatherForecastAdapter(Context context, int resource, List<WeatherForecast> objects) {
        super(context, resource, objects);

        this.context = context;

        forecasts = objects;
    }

    public WeatherForecast getItem(int i) {
        return forecasts.get(i);
    }

    /* I am not dealing with a lot of data, so I will not bother using the viewholder pattern. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WeatherForecast forecast = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);

            //if convertview is null, I need to inflate it to access its children.
            convertView = inflater.inflate(R.layout.extended_forecast_listview_row, null);
        }

        //Instantiate all the listview row children.
        TextView extendedForecastDateView = (TextView) convertView.findViewById(R.id.extended_forecast_dateView);
        TextView extendedForecastTempDayView = (TextView) convertView.findViewById(R.id.extended_forecast_tempDayView);
        TextView extendedforecasttempNightView = (TextView) convertView.findViewById(R.id.extended_forecast_tempNightView);
        TextView extendedforecastwindSpeedView = (TextView) convertView.findViewById(R.id.extended_forecast_windSpeedView);
        ImageView extendedForecastIconView = (ImageView) convertView.findViewById(R.id.extended_forecast_iconView);

        //Set the data for the views from the forecast object.
        extendedForecastDateView.setText(forecast.getDate());
        extendedForecastIconView.setImageDrawable(getDrawable(forecast.getIcon()));
        extendedForecastTempDayView.setText(" D: " + forecast.getTempDay());
        extendedforecasttempNightView.setText(" N: " + forecast.getTempNight());
        extendedforecastwindSpeedView.setText(" WS: " + forecast.getWindSpeed() + "m/s");

        return convertView;
    }

    /*Utility method to get a drawable from an icon*/
    private Drawable getDrawable(String icon) {
        Drawable d = null;

        try {
            d = context.getResources().getDrawable(context.getResources().getIdentifier("w" + icon,
                    "drawable", context.getPackageName()));
        } catch (Resources.NotFoundException e) {
            d = context.getResources().getDrawable(R.drawable.error);
        }
        return d;
    }

}
