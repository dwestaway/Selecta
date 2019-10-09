package com.foodfriend.selecta;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by Dan on 02/03/2018.
 */

public class GPSLocation implements LocationListener{

    Context context;

    public GPSLocation(Context c) {
            context = c;
    }

    //get location longitude and latitude values
    public Location getLocation() {

        //check if GPS permission is enabled by user
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"GPS permission is not granted",Toast.LENGTH_SHORT).show();

            return null;
        }

        LocationManager locationM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); //have to typecast to a LocationManager

        boolean GPSEnabled = locationM.isProviderEnabled(LocationManager.GPS_PROVIDER); //check if GPS is enabled

        if(GPSEnabled == true)
        {
            locationM.requestLocationUpdates(LocationManager.GPS_PROVIDER,6000,10,this);

            Location location = locationM.getLastKnownLocation(LocationManager.GPS_PROVIDER); //get location

            return location;
        }
        else
            {
            Toast.makeText(context,"Please enable the GPS", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
