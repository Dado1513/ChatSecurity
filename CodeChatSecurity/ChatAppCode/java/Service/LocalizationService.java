package com.example.dado.chatsecurity.Service;

import android.Manifest;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.util.Log;


import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;


public class LocalizationService extends Service {


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.e("onLocationChanged ", "onLocationChanged");
            Log.e("Latitudine ", String.valueOf(location.getLatitude()));
            Log.e("Latitudine ", String.valueOf(location.getLongitude()));

            String latitudine = String.valueOf(location.getLatitude());
            String longitudine = String.valueOf(location.getLongitude());


            if(MyApplication.getInstance().getPrefManager().getUser().getLocation())
                new MyClass().execute(latitudine,longitudine);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onDestroy() {
        Log.e("Distrutto ", "distrutto");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onStartCommando ", "onStartCommando");

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("NON VANNO I PERMESSI ", "NON VANNO PROPRIO");

            return 0;
        }

        Log.e("CHIEDO La posizione ", "CHIEDO---");


        // non preciso come richiesta posizione ma finziona ogni 20 secondi circa solo con la posizione data
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,0,locationListener);

        return super.onStartCommand( intent,  flags,  startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class MyClass extends AsyncTask<String,Void,Void> {



        @Override
        protected Void doInBackground(String... params) {
            try {

                Log.e("doInBackground","doInBackground");

                String idUser = MyApplication.getInstance().getPrefManager().getUser().getID_USER();
                String latitudine = params[0];
                String longitudine = params[1];

                URL url = new URL(EndPoint
                        .BASE_URL+"updatePosition.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                //attacco tutti i parametri che voglio inviare al server
                builder.appendQueryParameter("id_utente", idUser)
                        .appendQueryParameter("latitudine", latitudine)
                        .appendQueryParameter("longitudine",longitudine);

                //potrei appendere una stringa di riconoscimento dell'app
                //.appendQueryParameter("riconscimento","");

                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();


                urlConnection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) stringBuilder.append(nextLine);

                Log.e("MMM",stringBuilder.toString());

            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
