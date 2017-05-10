package com.example.dado.chatsecurity.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Gcm.GcmIntentService;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

public class ActivityIniziale extends AppCompatActivity {

    Button registrati,login;
    public static final String TAG= ActivityIniziale.class.getSimpleName();
    BroadcastReceiver mRegistrationBroadcastReceiver;
    String token;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //MyApplication.getInstance().getDbChat().removeAll();
        //Log.d(TAG,String.valueOf(MyApplication.getInstance().getPrefManager().getUser()));

        if(MyApplication.getInstance().getPrefManager().getUser()!=null){
            //sono gia loggato //vado nella chat
            //ottengo il gcm posso essermi loggato con un altro telefono

            if(MyApplication.getInstance().haveInternetConnection() && checkPlayServices()) {
                registerGCM();

            }else{
                startActivity(new Intent(this,MainActivity.class));
            }
        }else {


            setContentView(R.layout.activity_iniziale);
            registrati = (Button) findViewById(R.id.registrati);
            login = (Button) findViewById(R.id.login);
            login.setOnClickListener(new MyListener());
            registrati.setOnClickListener(new MyListener());
            MyApplication.getInstance().getDbChat().removeAll();

        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    token = intent.getStringExtra("token");
                    // Log.e(TAG,intent.getStringExtra("token"));
                    new updateGcm().execute();

                }
            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        //loadImagefromGallery();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mRegistrationBroadcastReceiver,new IntentFilter(Config.REGISTRATION_COMPLETE));



    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    private void registerGCM() {
        Intent intent =new Intent(this, GcmIntentService.class);
        //Log.d(TAG,"REGISTER");
        intent.putExtra("key","register");
        startService(intent);
    }

    //**********************************************************************************************//
    //************************************SEND DATA SERVER*****************************************//
    //controllo se il dispositico puo supportare le librerie
    //e ottennimento del gcm
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability =GoogleApiAvailability.getInstance();
        int resultCode=apiAvailability.isGooglePlayServicesAvailable(this);
        //Log.d(TAG,String.valueOf(resultCode)+"->"+ConnectionResult.SUCCESS);
        if(resultCode!= ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            } else{
                //Log.i(TAG,"DEVICE NON supportato GOOGLE API SERVICE");
                Toast.makeText(getApplicationContext(),"Google Play Services Non supportati",Toast.LENGTH_LONG);
                finish();
            }
            return false;
        }
        return true;

    }



    public class MyListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.login:
                    Intent intent=new Intent(getApplicationContext(),ActivityLogin.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                case R.id.registrati:
                    Intent intent1=new Intent(getApplicationContext(),ActivityRegistrazione1.class);
                    //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    break;
            }

        }
    }

    private class updateGcm extends AsyncTask<Void,Void,Void> {

        String rispostaServer;

        @Override
        protected Void doInBackground(Void... params) {
            try {


                URL url = new URL(EndPoint.BASE_URL + "updateGCM.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("token", token).appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
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
                //Log.e("login",rispostaServer);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SSLHandshakeException e) {
                Toast.makeText(getApplicationContext(), "Impossibile Effettuare il login Server non Verificato", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Log.e(TAG,rispostaServer);
            try {
                JSONObject object=new JSONObject(rispostaServer);
                if(!object.getBoolean("errore")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                }
                //Log.d(TAG,rispostaServer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
