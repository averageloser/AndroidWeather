package com.example.tj.weather.util;

import android.os.AsyncTask;

import com.example.tj.weather.WearMainActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tj on 7/14/2015.
 * An asynctask that will send messages from wear node to phone.
 * Maybe this should be a headless Fragment that holds an asynctask?
 */
public class WearMessageTask extends AsyncTask<String, Void, String> {
    public interface WearMessageListener {
        void onMessageSent(String message);
    }

    private GoogleApiClient googleApiClient;

    private List<WearMessageListener> listeners = new ArrayList<WearMessageListener>();

    public void addWearMessageListener(WearMessageListener listener) {
        listeners.add(listener);
    }

    public WearMessageTask(GoogleApiClient client) {
        googleApiClient = client;
    }

    @Override
    protected String doInBackground(String... params) {
        String message = params[0];

        //send message to neaby nodes.
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            if (node.isNearby()) {
                //Node is in range, so send it the data.
                Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "", message.getBytes()).await();
            }
        }

        return message;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        //notify listeners.
        for (WearMessageListener listener : listeners) {
            listener.onMessageSent(s);
        }
    }
}
