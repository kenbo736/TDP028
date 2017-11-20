package com.example.kenbo736.chatapp2;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            return;
        }
        String plats = event.getTriggeringGeofences().get(0).getRequestId();
        Intent tempIntent = new Intent(MainActivity.RECEIVE_PLATS);
        tempIntent.putExtra("plats", plats);
        LocalBroadcastManager.getInstance(this).sendBroadcast(tempIntent);

    }
}