package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;

import com.example.dado.chatsecurity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dado on 24/08/2016.
 */


public class SettingsActivity extends AppCompatActivity {

    //possibilita di cambiare la foto
    //di visualizzarla
    //infondo i creatori
    //la possibilita di non ricevere piu messaggi
    //la
    TextView textView;
    Button statePersonale,changePassword,changeImage,changeBackgrounnd;
    String statoPersonaleNuovo;
    String id;
    String stato;
    ProgressDialog progressDialog;

    boolean changeStato=false;
    private final String TAG= SearchActivity.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        CircleImageView circleImageView=(CircleImageView)findViewById(R.id.settingImage);

        progressDialog = new ProgressDialog(this);
        circleImageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(MyApplication.getInstance().getDbChat().getUserFromId(MyApplication.getInstance().getPrefManager().getUser().getID_USER()).getUrlImage()));
        textView=(TextView)findViewById(R.id.statePersonal);
        changePassword=(Button)findViewById(R.id.changePassword);

        SharedPreferences preferences = getSharedPreferences("pref_image", 0);
        SharedPreferences.Editor edit=preferences.edit();
        edit.clear();
        edit.commit();

        //identificativi dell'utente attuale
        id=MyApplication.getInstance().getPrefManager().getUser().getID_USER();
        stato=MyApplication.getInstance().getDbChat().getUserFromId(id).getStatoPersonale();

        if(stato!=null && !stato.equals("null"))
            textView.setText(stato);
        else
            textView.setText("Imposta Uno Stato Personale");

        changeImage=(Button)findViewById(R.id.changeImage);
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,ChangeImageProfile.class));
                finish();
            }
        });

        statePersonale=(Button)findViewById(R.id.changeState);
        statePersonale.setOnClickListener(

                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlertDialogStatePersonale().show();
                    }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,ActivityChangePassword.class));
            }
        });

        changeBackgrounnd = (Button)findViewById(R.id.changeBackground);
        changeBackgrounnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ActivityBackgroundImage.class));

            }
        });
    }


    public AlertDialog.Builder getAlertDialogStatePersonale(){
        AlertDialog.Builder builder =new AlertDialog.Builder(new ContextThemeWrapper(SettingsActivity.this, R.style.myDialog));

        builder.setTitle("Imposta uno Stato Personale");

        // Set up the input
        final EditText input = new EditText(getApplicationContext());
        // Specify the type of input expected; this, for example
        // , sets the input as a password, and will mask the text
        input.setHint("Stato Personale");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if(stato!=null && !stato.equals("null"))
            input.setText(stato);
        builder.setView(input,50,50,50,50);


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    statoPersonaleNuovo = input.getText().toString();
                    //ora devo invaire lo stato al server e aggiornare il mio stato aggiornando il db in remoto e il mio in locale
                    if(MyApplication.haveInternetConnection()) {
                        changeStato=true;

                        setRequestedOrientation(getResources().getConfiguration().orientation);
                        stato = statoPersonaleNuovo;
                        textView.setText(statoPersonaleNuovo);

                        new sendStatoToServerAndSaveLocalDb().execute();

                        progressDialog.setMessage("Attendere");
                        progressDialog.show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Attivare la connesione internet per impostare uno stato",Toast.LENGTH_LONG).show();
                    }

                }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                  }
        });
        return builder;

    }

    private class sendStatoToServerAndSaveLocalDb extends AsyncTask<Void,Void,Void>{

        String rispostaServer;
        @Override
        protected Void doInBackground(Void... params) {

            MyApplication.getInstance().getDbChat().updateStatePersonale(statoPersonaleNuovo);

            try {
                URL url = new URL(EndPoint.BASE_URL + "updateStatePersonal.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER()).
                        appendQueryParameter("state",statoPersonaleNuovo).appendQueryParameter("root","caputotavellamantovani99").
                        appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                String data=builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();

                BufferedReader reader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);
                rispostaServer = stringBuilder.toString();



            }catch (SSLHandshakeException e){

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Log.e(TAG,rispostaServer);
            try {
                JSONObject data=new JSONObject(rispostaServer);
                if(!data.getBoolean("errore")){
                    Toast.makeText(SettingsActivity.this,"Lo stato personale è stato modificato",Toast.LENGTH_LONG).show();

                }else{
                    Toast.makeText(SettingsActivity.this,data.getString("risultato"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }



}
