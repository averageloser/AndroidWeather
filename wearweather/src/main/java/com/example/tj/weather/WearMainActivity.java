package com.example.tj.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tj.weather.util.WearMessageTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WearMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, WearMessageTask.WearMessageListener {
    private static final int SPEECH_REQUEST_CODE = 0;
    private Button startSpeech;
    private GoogleApiClient googleApiClient;
    private WearMessageTask wearMessageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        connectToGoogleApiServices();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                startSpeech = (Button) stub.findViewById(R.id.startSpeech);
                startSpeech.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSpeech.setEnabled(false);
                        speechRecognizer();
                    }
                });
            }
        });
    }

    private void speechRecognizer() {
        if (googleApiClient.isConnected()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Connection to Google API Services has not completed. + \n +" +
                    "Try again.", Toast.LENGTH_LONG).show();
        }
    }

    /*This is called when the speech recognizer activity finishes. This is where I send data to the phone. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            //I have some data.
            final List<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            //Spawn new Thread and send data to the phone, but maybe I should just use an asynctask...
            wearMessageTask = new WearMessageTask(googleApiClient);
            wearMessageTask.addWearMessageListener(this);
            wearMessageTask.execute(text.get(0));

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void connectToGoogleApiServices() {
        //Check to see if Google Play Services is installed.
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

        int result = availability.isGooglePlayServicesAvailable(this);

        //if google play services is not installed, this should prompt user to download it.
        if (result != ConnectionResult.SUCCESS) {
            availability.getErrorDialog(this, result, 0);
        } else {
            //instantiate googleApiClient for play location services, if we haven't already.
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Wearable.API)
                        .build();
            }

            /*Attempt connection to google play services here.   The rest of the location services
            will be handled in the connect() callback.
             */
            googleApiClient.connect();
        }
    }

    ////////////////////////////Lifecycle methods////////////////////////////
    public void onStart() {
        super.onStart();

        googleApiClient.connect();
    }

    public void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    ////////////////////////////Google api client callbacks.///////////////////////
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected to Google API Services.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connected to Google API Services Suspended.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connected to Google API Services Failed.", Toast.LENGTH_SHORT).show();
    }

    /////////////////////WearMessageTask callback.//////////////////////////
    @Override
    public void onMessageSent(String message) {
        Log.i("message", message);

        //enable the speech button after message sent.
        startSpeech.setEnabled(true);
    }
}
