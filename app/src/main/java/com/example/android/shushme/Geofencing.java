package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

/**
 * Created by User on 17:35 10.01.2018.
 */

public class Geofencing implements ResultCallback {
    private static final int GEOFENCE_RADIUCE = 50;
    private static final int GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;
    private static final String TAG = Geofencing.class.getSimpleName();
    private Context mContext;
    private GoogleApiClient mClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    public Geofencing(Context mContext, GoogleApiClient mClient) {
        this.mContext = mContext;
        this.mClient = mClient;
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
    }

    public void updateGeofencesList(PlaceBuffer places) {
        mGeofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) return;

        for (Place place :
                places) {
            String placeId = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUCE)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent;

        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    public void registerAllGeofences() {
        if (mClient == null || !mClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0)
            return;
        try {
            LocationServices.GeofencingApi
                    .addGeofences(mClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void unRegisterAllGeofences() {
        if (mClient == null || !mClient.isConnected()) return;

        try {
            LocationServices.GeofencingApi
                    .removeGeofences(mClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, "Error: " + result.toString());
    }
}
