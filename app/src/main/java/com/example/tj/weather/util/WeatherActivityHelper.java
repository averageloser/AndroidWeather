package com.example.tj.weather.util;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.tj.weather.R;
import com.example.tj.weather.WeatherActivity;
import com.example.tj.weather.database.DBAdapter;
import com.example.tj.weather.database.DBLocation;
import com.example.tj.weather.database.DBManager;
import com.example.tj.weather.database.DBModel;
import com.example.tj.weather.model.ExtendedWeatherForecastAdapter;
import com.example.tj.weather.model.WeatherForecast;
import com.example.tj.weather.model.WeatherLocation;
import com.example.tj.weather.ui.MapLocationView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 6/13/2015.
 * The purpose of this class is to reduce code clutter in WeatherActivity.
 * This class will handle instantiation and modification of all ui components,
 * as well as weather download callbacks.
 */
public class WeatherActivityHelper implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback {
    public static final int DB_LOADER = 1;

    private WeatherActivity activity;

    private Toolbar toolbar;

    private ViewFlipper flipper;

    /*Child views for the current forecast layout.*/
    private TextView dateTimeView, weatherDetailsView, tempView, currentHumidityView,
            currentWindSpeedView;
    private ImageView iconView;

    //Child views of the extended forecast layout.
    private ListView extendedForecastListView;

    //Database listview.
    private ListView databaseListView;

    //The adapter for the databaseListView.
    private DBAdapter databaseListViewAdapter;

    //Progress dialog that shows when a download is taking place.
    private ProgressDialog progressDialog;

    //The loader for the database view.
    private CursorLoader databaseLoader;

    //The Database Manager.
    private DBManager dbManager;

    //the Model for the database.
    private DBModel dbModel;

    //The list of DBLocations from the database.
    private List<DBLocation> dbLocations = new ArrayList<>();

    private View databaseLayout;

    private MapLocationView mapView;

    private boolean mapReady;

    private String currentCity;

    private String currentStateOrCountry;

    private Button nextButton; //for convenience, this button will flip through views in the viewflipper.

    public WeatherActivityHelper(WeatherActivity activity) {
        this.activity = activity;

        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        activity.setSupportActionBar(toolbar);

        toolbar.setTitle(null);

        LayoutInflater inflater = activity.getLayoutInflater();

        //instantiate the progress dialog for location searches.
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Processing...");

        //the db manager.
        dbManager = new DBManager(activity);

        //the model for the database.
        dbModel = new DBModel(dbManager.getWritableDatabase());

        //create the listview adapter, if it is null.
        databaseListViewAdapter = new DBAdapter(activity, null);

        flipper = (ViewFlipper) activity.findViewById(R.id.flipper);

        databaseLayout = inflater.inflate(R.layout.database_view_layout, null);
        View currentForecastLayout = inflater.inflate(R.layout.current_forecast_layout, null);
        View extendedForecastLayout = inflater.inflate(R.layout.extended_forecast_layout, null);
        //View extendedForecastHourlyLayout = inflater.inflate(R.layout.extended_forecast_hourly_layout, null);
        mapView = new MapLocationView(activity, this);

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

        //ListView for the database view.
        databaseListView = (ListView) databaseLayout.findViewById(R.id.database_listView);
        //Assign the database list view a listener for clicks.
        databaseListView.setOnItemClickListener(databaseListViewClickListener);

        //register databast list view for context menu.
        activity.registerForContextMenu(databaseListView);

        databaseListView.setAdapter(databaseListViewAdapter);

        //put something here for empty.  Maybe an imageview, or at least a textview.
        //databaseListView.setEmptyView();

        flipper.setAnimateFirstView(true);
        flipper.setInAnimation(activity, android.R.anim.fade_in);
        flipper.setOutAnimation(activity, android.R.anim.fade_out);
        flipper.addView(currentForecastLayout);
        flipper.addView(extendedForecastLayout);
        flipper.addView(databaseLayout);
        flipper.addView(mapView);

        nextButton = (Button) activity.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flipper.showNext();
            }
        });

        //initialize the loader.
        activity.getSupportLoaderManager().initLoader(DB_LOADER, null, this);
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

            //Grab the weather forecast list.
            List<WeatherForecast> currentForecast = current.getWeatherForecastList();

            //The WeatherForecast.
            WeatherForecast forecast = currentForecast.get(0);

            //Set the city and state in toolbar.
            if (current.getCity() != null && current.getCountryOrState() != null) {
                toolbar.setTitle((current.getCity() + "," + current.getCountryOrState()).toUpperCase());

                //set the current city and state
                currentCity = current.getCity();

                currentStateOrCountry = current.getCountryOrState();
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
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();

            //add the page to the wearable extender.
            wear.addPage(extended);

            //set the notification for the current forecast.  this will be the main notification in the stream.
            Notification notification = new NotificationCompat.Builder(activity)
                    .setSmallIcon(R.drawable.w01d)
                    .setContentTitle("Current Forecast")
                    .setContentText("Temp " + weatherLocation.get(0).getWeatherForecastList().get(0).getTemperature())
                    .setPriority(Notification.PRIORITY_HIGH)
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

        //update the map, if it is ready.
        if (mapReady) {
            mapView.moveMarker(currentCity, currentStateOrCountry);
        }

        //finally dismiss the dialog.
        progressDialog.dismiss();

        flipper.setDisplayedChild(0);
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

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        databaseLoader = new CursorLoader(activity) {
            @Override
            public Cursor loadInBackground() {
                Cursor cursor = dbModel.getAllCursor();
                return cursor;
            }
        };

        return databaseLoader;
    }

    ////////////////////////////////////////The Loader callbacks/////////////////
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("Loader", "Load Finished");

        dbLocations.clear();

        while (data.moveToNext()) {
            DBLocation location = new DBLocation();
            location.setCity(data.getString(1));
            location.setStateOrCountry(data.getString(2));

            dbLocations.add(location);
        }

        databaseListViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        databaseListViewAdapter.swapCursor(null);
    }

    //Adds a location to the database in response to a city search.
    public void insertDBLocation(final String city, final String stateOrCountry) {
        DBLocation location = new DBLocation(city, stateOrCountry);

        boolean unique = true;

        //This is an expensive search.
        for (DBLocation loc : dbLocations) {
            if (location.getCity().equals(loc.getCity()) && location.getStateOrCountry().equals(loc.getStateOrCountry())) {
                unique = false;
            }
        }

        try {
            if (unique && dbModel.insert(location)) {

                Log.i("insert", "location inserted");

                databaseLoader.onContentChanged();
            }
        } catch (SQLiteException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteDBLocation(int position) {
        long id = databaseListViewAdapter.getItemId(position);


        if (dbModel.delete(id)) {
            Log.i("delete", "location deleted");

            //update the adapter.
            databaseLoader.onContentChanged();
        }
    }

    //Deletes all items in the database in response to user pressing the trash icon in the toolbar.
    public void deleteItems() {
        if (dbModel.deleteAll()) {
            Log.i("DB Delete", "complete");

            dbLocations.clear();
        }

        //Tell the loader that the data changed.
        databaseLoader.onContentChanged();
    }

    //////////////////////////////Listener for the listview for single touches.///////////////////////
    AdapterView.OnItemClickListener databaseListViewClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Get the city and stateOrCountry from the item that was clicked.
            DBLocation location = dbLocations.get(position);

            String city = location.getCity();

            String stateOrCountry = location.getStateOrCountry();

            Log.i("db click", city + " " + stateOrCountry);

            //call back to the activity to do the location search.
            activity.onCityChanged(city, stateOrCountry);
        }
    };

    //////////////////////the context menu for the databast list view//////////////////////
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 1, 0, "Delete");
    }

    public void onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        deleteDBLocation(info.position);
    }

    ///////////////////////////Lifecycle methods.////////////////
    public void onCreate(Bundle state) {
        mapView.onCreate(state);
    }

    public void onPause() {
        mapView.onPause();
    }

    public void onResume() {
        mapView.onResume();
    }

    public void onDestroy() {
        mapView.onDestroy();
    }

    public void onLowMemory() {
        mapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle out) {
        mapView.onSaveInstanceState(out);
    }

    ///////////////////Called when my Google map is ready for use./////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("map ready?", Thread.currentThread().getName());

        mapView.setGoogleMap(googleMap);

        mapReady = true;

        //if a location is available yet, move to it.
        mapView.moveMarker(currentCity, currentStateOrCountry);
    }

    //Navigate back to the first viewflipper child.
    public void goHome() {
        flipper.showNext();
    }
}
