/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */


package org.apache.cordova.geolocation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.inforoeste.mocklocationdetector.MockLocationDetector;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import javax.security.auth.callback.Callback;

public class Geolocation extends CordovaPlugin {

    String TAG = "GeolocationPlugin";
    CallbackContext context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private String LOCATION_PROVIDER = "";
    private int MY_PERMISSIONS_REQUEST = 1000;
    Location loc;

    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };


    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "We are entering execute");
        context = callbackContext;
        if(action.equals("getPermission"))
        {
            if(getMock()){
              Log.e("GPS-DATA", "Erro");
                //context.success("mock-true");
                PluginResult r = new PluginResult(PluginResult.Status.ERROR);
                context.sendPluginResult(r);
              return true;
            }

            if(hasPermisssion())
            {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(r);
                return true;
            }
            else {
                PermissionHelper.requestPermissions(this, 0, permissions);
            }
            return true;
        }
        return false;
    }

    private boolean getMock(){

        LocationManager locationManager = (LocationManager) this.cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if(!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            return true;
        }else{
            if(isGPSEnabled){
                LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
                
            }else if(isNetworkEnabled){
                LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

            }else{
                return true;
            }

            if (ContextCompat.checkSelfPermission(this.cordova.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.cordova.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(this.cordova.getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST);
                }
            }


            if(locationManager.getLastKnownLocation(LOCATION_PROVIDER) != null){
                loc = locationManager.getLastKnownLocation(LOCATION_PROVIDER);

                //Log.e("GPS-DATA", locationManager.getLastKnownLocation(LOCATION_PROVIDER).toString());
                if(loc.getAccuracy() == 1 ||
                        MockLocationDetector.checkForAllowMockLocationsApps(this.cordova.getActivity().getApplicationContext()) ||
                        MockLocationDetector.isLocationFromMockProvider(this.cordova.getActivity().getApplicationContext(),loc)){
                    return true;
                }else{
                    //Log.e("GPS-DATA","Accuracy = " + loc.getAccuracy());
                    return false;
                }

            }else{
                loc = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
                //Log.e("GPS-DATA", "KnowLocation NULL: " + loc);

                return false;

            }
        }

    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if(context != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    LOG.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    context.sendPluginResult(result);
                    return;
                }

            }
            result = new PluginResult(PluginResult.Status.OK);
            context.sendPluginResult(result);
        }
    }

    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    /*
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     */

    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }



}
