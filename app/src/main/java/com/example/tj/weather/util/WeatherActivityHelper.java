package com.example.tj.weather.util;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.tj.weather.R;
import com.example.tj.weather.WeatherActivity;
import com.example.tj.weather.model.ExtendedWeatherForecastAdapter;
import com.example.tj.weather.model.WeatherForecast;
import com.example.tj.weather.model.WeatherLocation;

import java.util.List;

/**
 * Created by tom on 6/13/2015.
 * The purpose of this class is to reduce code clutter in WeatherActivity.
 * This class will handle instantiation and modification of all ui components,
 * as well as weather download callbacks.
 */
public class WeatherActivityHelper {
    private WeatherActivity activity;

    private Toolbar toolbar;

    private ViewFlipper flipper;

    /*Child views for the current forecast layout.*/
    private TextView dateTimeView, weatherDetailsView, tempView, currentHumidityView,
            currentWindSpeedView;
    private ImageView iconView;

    //Child views of the extended forecast layout.
    private ListView extendedForecastListView;

    //Progress dialog that shows when a download is taking place.
    ProgressDialog progressDialog;


    public WeatherActivityHelper(WeatherActivity activity) {
        this.activity = activity;

        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        activity.setSupportActionBar(toolbar);

        toolbar.setTitle(null);

        LayoutInflater inflater = activity.getLayoutInflater();

        //instantiate the progress dialog for location searches.
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Processing...");

        flipper = (ViewFlipper) activity.findViewById(R.id.flipper);

        View currentForecastLayout = inflater.inflate(R.layout.current_forecast_layout, null);
        View extendedForecastLayout = inflater.inflate(R.layout.extended_forecast_layout, null);
        View extendedForecastHourlyLayout = inflater.inflate(R.layout.extended_forecast_hourly_layout, null);

        //Textviews for the currentView.
        dateTimeView = (TextView) currentForecastLayout.findViewById(R.id.currentDateTimeView);
        tempView = (TextView) currentForecastLayout.findViewById(R.id.currentTempView);
        weatherDetailsView = (TextView) currentForecastLayout.findViewById(R.id.currentWeatherDetailsView);
        currentHumidityView = (TextView) currentForecastLayout.findViewById(R.id.currentHumidityView);
        currentWindSpeedView = (TextView) currentForecastLayout.findViewById(R.id.currentWindSpeedView);

        //ImageView for the currentView.
        iconView = (ImageView) currentForecastLayout.findViewById(R.id.currentIconView);

        //ListView for the extended forecast.
        extendedForecastListView = (ListView) extendedForecastLayout.findViewById(R.id.extended_forecast_listView);

        flipper.setAnimateFirstView(true);
        flipper.setInAnimation(activity, android.R.anim.fade_in);
        flipper.setOutAnimation(activity, android.R.anim.fade_out);
        flipper.addView(currentForecastLayout);
        flipper.addView(extendedForecastLayout);

    }

    /*Handles touch for the view flipper.   For simplicity, I choose to use the fade in and out
   animation.  I could have used a translate animation from left to right or vice versa, depending
   on whether or not the user swiped right or left.

   Move this into helper class to reduce clutter.
    */
    public void onTouchEvent(MotionEvent event) {
        float firstX = 0;
        float lastX = 0;
        float minimumDistance = 50; //User must move a min of 100px to register a swipe.

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                lastX = event.getX();
                break;
        }

        //Check if swiped right.
        if (lastX > (firstX) + (minimumDistance)) { //Right swipe.
            flipper.showNext();
        }

        //Check if swiped left.
        if (lastX < (firstX) - (minimumDistance)) { //Left swipe.
            flipper.showPrevious();
        }
    }

    /* Callback for the AsyncTask that downloads weather data.
* The processing of this data can be moved to a helper class if it gets too large.
* Better yet, I should have used Fragments and a ViewPager.*/
    public void onWeatherDownloadComplete(List<WeatherLocation> weatherLocation) {
        //dismiss the progress dialog.
        progressDialog.dismiss();

        /********************The current forecast*************************
         * The weatherforecast list will be > 0 if everything worked as planned.*/

        if (weatherLocation.size() > 0) {
            WeatherLocation current = weatherLocation.get(0);

            List<WeatherForecast> currentForecast = current.getWeatherForecastList();

            //The WeatherForecast.
            WeatherForecast forecast = currentForecast.get(0);

            //Set the city and state in toolbar.
            if (current.getCity() != null && current.getCountryOrState() != null) {
                toolbar.setTitle((current.getCity() + "," + current.getCountryOrState()).toUpperCase());
            }

            //Set the date and the time.
            dateTimeView.setText(forecast.getDate() + " " + forecast.getTime());

            /*If user enters invalid location, app may crash, so try to prevent that. There is also
            no point in setting anything else when that happens.*/
            Drawable d = null;

            try {
                d = activity.getResources().getDrawable(activity.getResources().getIdentifier("w" + forecast.getIcon(),
                        "drawable", activity.getPackageName()));

                iconView.setImageDrawable(d);

                //Set the temperature.
                tempView.setText(forecast.getTemperature());

                //Set the weather details.
                weatherDetailsView.setText(forecast.getDescription());

                //Set the humidity.
                currentHumidityView.setText("Humidity: " + forecast.getHumidity());

                //Set the wind speed.
                currentWindSpeedView.setText("Wind Speed: " + forecast.getWindSpeed() + " m/s");
            } catch (Resources.NotFoundException e) {
                d = activity.getResources().getDrawable(R.drawable.error);

                iconView.setImageDrawable(d);

                /*
                If something goes wrong, inform user to try again.*/
                onWeatherDownloadError();
            }
        }

        /****************The extended forecast******************************/
        /*Get the WeatherLocation for the extended list.  Any one of the 3 JSON download requests
        could fail, so even if the current forecast request succeeds, I still need to make sure
        that the others did, and if they did, the weatherLocation list will be > 1.
         */
        if (weatherLocation.size() > 1) {

            WeatherLocation extended = weatherLocation.get(1);

            List<WeatherForecast> extendedForecastList = extended.getWeatherForecastList();

            Log.i("size", String.valueOf(extendedForecastList.size()));

            //set the adapter for the extendedForecastListView.
            ExtendedWeatherForecastAdapter extendedWeatherForecastAdapter =
                    new ExtendedWeatherForecastAdapter(activity,
                            R.layout.extended_forecast_hourly_listview_row, extendedForecastList);

            extendedForecastListView.setAdapter(extendedWeatherForecastAdapter);
        }

        /*********************Build and send notifications for wear devices********************/
        //we have an extended forecast, so we can add it to the context stream as a page for wearables.
        if (weatherLocation.size() > 1) {
            //create a wearable extender.
            NotificationCompat.WearableExtender wear = new NotificationCompat.WearableExtender();

            //String to hold the dates and temps.
            String dateAndTemps = "";

            //cycle through the list of forecasts and get data.
            for (WeatherForecast forecast : weatherLocation.get(1).getWeatherForecastList()) {
                dateAndTemps += forecast.getDate() + " " + forecast.getTempDay() + "\n";
            }

            //create a notification and add it as a page to the wearable extender.
            Notification extended = new NotificationCompat.Builder(activity)
                    .setContentTitle("Extended Forecast")
                    .setContentText(dateAndTemps)
                    .build();

            //add the page to the wearable extender.
            wear.addPage(extended);

            //set the notification for the current forecast.  this will be the main notification in the stream.
            Notification notification = new NotificationCompat.Builder(activity)
                    .setSmallIcon(R.drawable.w01d)
                    .setContentTitle("Current Forecast")
                    .setContentText("Temp " + weatherLocation.get(0).getWeatherForecastList().get(0).getTemperature())
                    .extend(wear)
                    .build();

            //create a notificationmanager and send the notification.
            NotificationManagerCompat mgr = NotificationManagerCompat.from(activity);
            mgr.notify(1, notification);
        }
        /***********************The extended hourly forecast************************/
        ///////////////////CURRENTLY UNUSED.  IMPLEMENT IF YOU WANT./////////////////////////
        /*Get the WeatherLocation for the extended list.  Any one of the 3 JSON download requests
        could fail, so even if the current forecast request succeeds, I still need to make sure
        that the others did, and if they did, the weatherLocation list will be >= 2.
         */
        if (weatherLocation.size() > 2) {
            //Not used here.
        }

        //finally dismiss the dialog.
        progressDialog.dismiss();
    } //End OnWeatherDownloadComplete().

    //show a dialog explaining an error occurred.
    public void onWeatherDownloadError() {
        //dismiss the progress dialog.
        progressDialog.dismiss();

        Log.i("Download error", "Please retry.");

        //I am probably on the main thread.  If so, no need for this.  Just Toast.  Check this.
        activity.runOnUiThread(new Runnable() {
            public void run() {
                String message = "IOException from REST source! - Invalid location, Network, or other error. \n" +
                        "One or more forecasts may not have downloaded.";

                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //Used by WeatherActivity to show the progressDialog.
    public void showDialog() {
        progressDialog.show();
    }
}
