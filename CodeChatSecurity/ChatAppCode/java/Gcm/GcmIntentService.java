package com.example.dado.chatsecurity.Gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dado.chatsecurity.Activity.MainActivity;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by Dado on 03/08/2016.
 */
public class GcmIntentService extends IntentService {
    //servizi per prendere il token da google e sperdirlo al nostro server
    private static final String TAG= GcmIntentService.class.getSimpleName();
    private static final String register="register";
    private static final String tokenRefresh="refresh";

    public GcmIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String key=intent.getStringExtra("key");
        switch (key) {
            case register:
                //Log.d(TAG,key);
                registerGCM();
                break;
            case tokenRefresh:
                sendRegistrationToServer(registerGCMrefresh());
                break;
        }
    }

    //mando il token al server
    private void sendRegistrationToServer(String token){

        if(token!=null && MyApplication.getInstance().getPrefManager().getUser()!=null){
            new updateGcm().execute(token);
        }
    }


    private class updateGcm extends AsyncTask<String,Void,Void> {

        String rispostaServer;

        @Override
        protected Void doInBackground(String... params) {
            try {


                URL url = new URL(EndPoint.BASE_URL + "updateGCM.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder();
                if (MyApplication.getInstance().getPrefManager().getUser() != null){
                    builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("token", params[0]).appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("password", MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                }
                String query = builder.build().getEncodedQuery();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                writer.write(query);
                writer.flush();
                writer.close();

                urlConnection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);
                rispostaServer = stringBuilder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SSLHandshakeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG,"updateGCM");
        }
    }

    private String registerGCMrefresh(){

        if(MyApplication.haveInternetConnection()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String token = null;

            InstanceID instanceID = InstanceID.getInstance(this);
            try {
                //R.string.gcm_defaultSenderId;
               // GoogleCloudMessaging.INSTANCE_ID_SCOPE
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                //Log.e(TAG,"GCM REGISTRATION TOKEN: "+token);

                //mandiamo il token al nostro server
                //sendRegistrationToServer(token);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return token;
        }else
            return null;
    }


    private void registerGCM(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String token=null;

        InstanceID instanceID=InstanceID.getInstance(this);
        try {
            token=instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE,null);
            //Log.e(TAG,"GCM REGISTRATION TOKEN: "+token);

            //mandiamo il token al nostro server
            //sendRegistrationToServer(token);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent registrazioneCompleta=new Intent(Config.REGISTRATION_COMPLETE);
        registrazioneCompleta.putExtra("token",token);
        //mandiamo l'intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrazioneCompleta);
    }
}
