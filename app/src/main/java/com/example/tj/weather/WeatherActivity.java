
package com.example.tj.weather;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.tj.weather.model.WeatherLocation;
import com.example.tj.weather.receivers.LocationSettingsReceiver;
import com.example.tj.weather.ui.CitySearchDialogFragment;
import com.example.tj.weather.util.NetworkLocationSearchTask;
import com.example.tj.weather.util.NetworkLocationSettingsVerifier;
import com.example.tj.weather.util.WeatherActivityHelper;
import com.example.tj.weather.util.WeatherDownloadTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.List;

import static com.example.tj.weather.ui.CitySearchDialogFragment.CityChangeListener;

/**
 * @Author Tom Farrell
 * The main Activity for the Android Weather application.  License: Public Domain.
 */
public class WeatherActivity extends AppCompatActivity implements CityChangeListener,
        WeatherDownloadTask.WeatherDownloadListener, NetworkLocationSearchTask.NetworkLocationChangeListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        NetworkLocationSettingsVerifier.LocationSettingsVerifierListener {

    //The helper class for WeatherActivity.
    private WeatherActivityHelper weatherActivityHelper;

    //The city search DialogFragment
    private CitySearchDialogFragment citySearchDialog = new CitySearchDialogFragment();

    //The Fragment that does the weather downloading.
    private WeatherDownloadTask weatherDownloader = new WeatherDownloadTask();

    /*Boolean to tell me if proper location services are enabled (wifi and network). Low power use.
      Toggled as servies are available.*/
    private boolean locationSupported;

    //Is the app doing a search?
    private boolean processingSearch;

    //Did the user do a manual search by location?
    private boolean manualSearch;

    /*The LocationService handles location changes via google play services FusedLocationAPI.
       This is instantiated in the onconnected listener, as there is no point in creating it at
       all if the current environment doesn't support location services. e.g. location turned off.*/
    private NetworkLocationSearchTask networkLocationSearchTask;

    //Google api client for play services.  Currently only used for aquiring location.
    private GoogleApiClient googleApiClient;

    //The utlity class that does location settings verification.
    NetworkLocationSettingsVerifier networkLocationSettingsVerifier;

    //The Broadcast receiver that notifies of a change in location settings.
    LocationSettingsReceiver locationSettingsReceiver = new LocationSettingsReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        weatherActivityHelper = new WeatherActivityHelper(this);

        //Add the Fragments to the Activity.
        addFragments();

    }//end onCreate()

    public void connectToGoogleApiServices() {
        //Check to see if Google Play Services is installed.
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

        int result = availability.isGooglePlayServicesAvailable(this);

        //if google play services is not installed, this should prompt user to download it.
        if (result != ConnectionResult.SUCCESS) {
            availability.getErrorDialog(this, result, 0).show();
        } else {
            //instantiate googleApiClient for play location services, if we haven't already.
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .addApi(Wearable.API)
                        .build();
            }

            /*Attempt connection to google play services here.   The rest of the location services
            will be handled in the connect() callback.
             */
            googleApiClient.connect();
        }
    }

    //Add headless Fragments used for processing.
    private void addFragments() {
        FragmentManager mgr = getSupportFragmentManager();
        FragmentTransaction trans = mgr.beginTransaction();

        //register this activity as a listener to the weather downloader.
        weatherDownloader.WeatherDownloadListener(this);

        //add both Fragments to the Activity.
        trans.add(weatherDownloader, "weatherDownloader");
        trans.commit();
    }


    /*Handles touch for the view flipper.  See WeatherActivityHelper for details.*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        weatherActivityHelper.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the toolbar/actionbar, if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.city_search);
        Drawable searchIcon = searchItem.getIcon();
        searchIcon.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        searchItem.setIcon(searchIcon);

        MenuItem locationSearchItem = menu.findItem(R.id.location_search);
        Drawable locationSearchIcon = locationSearchItem.getIcon();
        locationSearchIcon.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        searchItem.setIcon(searchIcon);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Need this for adding and showing fragments.
        FragmentManager manager = getSupportFragmentManager();

        switch (id) {
            case R.id.city_search:
                if (!processingSearch) {
                    //the user is doing a manual search.
                    manualSearch = true;

                    citySearchDialog.show(manager, "CitySearchDialog");
                }
                break;
            case R.id.location_search:
                if (!processingSearch) {
                    networkLocationSettingsVerifier.checkLocationServices();

                    break;
                }
            case R.id.delete_locations:
                weatherActivityHelper.deleteItems();
        }
        return super.onOptionsItemSelected(item);
    }

    //Call back to the WeatherActivityHelper to handle the creation of the context menu.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        weatherActivityHelper.onCreateContextMenu(menu, v, menuInfo);
    }

    //Forward clicks to the context menu to the helper for processing.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        weatherActivityHelper.onContextItemSelected(item);

        return super.onContextItemSelected(item);
    }

    ////////////////Weather Download Task Callbacks/////////////////
    /*Called when a weather forecast download is complete.  See WeatherActivityHelper for details.*/
    @Override
    public void onWeatherDownloadComplete(List<WeatherLocation> weatherLocation) {
        weatherActivityHelper.onWeatherDownloadComplete(weatherLocation);
    }

    /* Some error occurred while downloading or parsing data from the rest source.  It is most
    likely an IOException.  THIS IS NOT CALLED ON THE MAIN THREAD! */
    @Override
    public void onWeatherDownloadError() {
        weatherActivityHelper.onWeatherDownloadError();
    }

    ////////////////////////////Lifecycle methods/////////////////////////////////////
    public void onStart() {
        super.onStart();

        registerReceiver(locationSettingsReceiver, new IntentFilter("android.location.MODE_CHANGED"));

        /*If google api client is not connected, or it not connecting, try to connect.  This will
        happen when the activity is restarted i.e. coming from background to foreground.*/
        connectToGoogleApiServices();
    }

    public void onStop() {
        super.onStop();

        unregisterReceiver(locationSettingsReceiver);

        //Disconnect from google play services here.
        googleApiClient.disconnect();

        locationSupported = false;
    }

    /**
     * /////////////////callback for a city change.//////////////////
     */
    //The user has requested that the city be changed.
    @Override
    public void onCityChanged(String city, String stateOrCountry) {

        processingSearch = true;

        weatherActivityHelper.showDialog();

        weatherDownloader.beginDownloading(city, stateOrCountry);

        processingSearch = false;

        //If the user is doing a manual search for a location, attempt to add it to the db.
        if (manualSearch) {
            //add this location to the database, if it doesn't already exist.
            weatherActivityHelper.InsertDBLocation(city, stateOrCountry);

            manualSearch = false;
        }
    }

    public void onResume() {
        super.onResume();
    }

    //////////////Callback for the location change listener.//////////////
    @Override
    public void onNetworkLocationChange(String[] location) {
        /* The location has changed.  Pull out the city and state, then request a download.
            The city is the first element and the state the second.  Now call onCityChanged
            with the new values.
         */

        Log.i("Thread", Thread.currentThread().getName());

        onCityChanged(location[0], location[1]);
    }

    /////////////////////////////Google Play Services callbacks below//////////////////////////
    /* Called when successfully connected to google play services.*/
    @Override
    public void onConnected(Bundle bundle) {
        //Here is where I check to make sure that location is set.
        if (networkLocationSettingsVerifier == null) {
            networkLocationSettingsVerifier = new NetworkLocationSettingsVerifier(googleApiClient);
            networkLocationSettingsVerifier.addLocationSettingsVerifierListener(this);
        }

        //perform the initial location forecast search.
        networkLocationSettingsVerifier.checkLocationServices();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //We lost the connection to play services on the device for whatever reason.
        locationSupported = false;

        Toast.makeText(this, "Location suspended, GoogleApiClient onConnectionSuspended()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "GoogleApiClient onConnectionFailed()", Toast.LENGTH_LONG).show();

        locationSupported = false;
    }

    ///////////////////////callbacks for the location settings verifier.////////////////////////
    @Override
    public void onNetworkLocationSettingsVerified() {
        Log.i("Thread", Thread.currentThread().getName());

        locationSupported = true;

        //Instantiate the locationservicetask, if it has not already been done.
        if (networkLocationSearchTask == null) {
            networkLocationSearchTask = new NetworkLocationSearchTask(WeatherActivity.this,
                    googleApiClient);
            networkLocationSearchTask.addListener(WeatherActivity.this);
        }

        //now do a search fo the current location and update the ui.
        networkLocationSearchTask.startLocationSearch();
    }

    @Override
    public void onNetworkLocationSettingsNotVerified() {
        locationSupported = false;

        new AlertDialog.Builder(this)
                .setTitle("Location problem")
                .setMessage("Problem connecting to location services.  Check your settings.")
                .show();
    }

    @Override
    public void onNewIntent(Intent intent) {
        wearSearchRequest(intent);
    }

    private void wearSearchRequest(Intent intent) {
        String location = intent.getStringExtra("message");

        if (location != null) {
            Geocoder geocoder = new Geocoder(this);

            List<Address> addressList = null;

            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addressList != null) {
                Address address = addressList.get(0);

                if (address != null) {
                    Log.i("address city", address.getLocality().toLowerCase() + " " + address.getAdminArea().toLowerCase());

                    onCityChanged(address.getLocality().toLowerCase(), address.getAdminArea().toLowerCase());
                }
            }
        }
    }
}
