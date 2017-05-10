package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Dado on 01/08/2016.
 */
public class ActivityRegistrazione1 extends AppCompatActivity {


    public static final String TAG = ActivityRegistrazione1.class.getSimpleName();
    private EditText inputUsername, inputEmail, inputPassowrd, inputConfermaPassword;
    private TextInputLayout inputLayoutUsername, inputLayoutEmail, inputLayoutPassword, inputLayoutConferma;
    private Button avanti;
    private String email,password,username;
    ProgressDialog progressDialog,dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        dialog=new ProgressDialog(ActivityRegistrazione1.this);
        progressDialog=new ProgressDialog(ActivityRegistrazione1.this);
        //per cancellare quello che puo rimanere salvato dall'activiti precedente
        final SharedPreferences preferences=getSharedPreferences("pref",0);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.commit();
        //progressDialog.setCancelable(false);
        ((Button) findViewById(R.id.avanti)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidEmail(inputEmail.getText().toString()) && !inputUsername.getText().toString().equals("") && inputUsername.getText().toString() != null
                        && validaPassword() ) {

                    //qua andiamo alla seconda activity Registrazione 2 che supporta l'inserimento dell'immagine passandoli come parametri tutti i valori appena memorizzati
                    username= inputUsername.getText().toString().trim();
                    password=inputPassowrd.getText().toString().trim();
                    email=inputEmail.getText().toString().trim();

                    if(MyApplication.haveInternetConnection()) {
                      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                        setRequestedOrientation(getResources().getConfiguration().orientation);
                        progressDialog.setMessage("WAITING");
                        progressDialog.show();
                        new ControlloEsistenzaUtente().execute();

                    } else {
                        Toast.makeText(getApplicationContext(),"Connessione internet assente",Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),"Completa Tutti I campi",Toast.LENGTH_LONG).show();
                }
            }
        });


        inputLayoutUsername = (TextInputLayout) findViewById(R.id.input_layout_username);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutConferma = (TextInputLayout) findViewById(R.id.input_layout_confermapassword);


        inputUsername = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassowrd = (EditText) findViewById(R.id.password);
        inputConfermaPassword = (EditText) findViewById(R.id.confermapasword);
        inputConfermaPassword.setEnabled(false);
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputUsername.addTextChangedListener(new MyTextWatcher(inputUsername));

        inputPassowrd.addTextChangedListener(new MyTextWatcher(inputPassowrd));
        Intent intent=getIntent();
        if(intent!=null && intent.getStringExtra("email")!=null){
            inputEmail.setText(intent.getStringExtra("email"));
            inputUsername.setText(intent.getStringExtra("username"));
            requestFocus(inputPassowrd);
        }
    }

    @Override
    public void onBackPressed() {
        progressDialog.hide();
        progressDialog.dismiss();

      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        startActivity(new Intent(this, ActivityIniziale.class));
        finish();

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        SharedPreferences preferences=getSharedPreferences("pref",0);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onStop() {
        Log.e(TAG,"STOP");
        progressDialog.hide();
        progressDialog.dismiss();
   //     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.e(TAG,"onPause");
        progressDialog.hide();
        progressDialog.dismiss();
    //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onPause();
    }

    @Override
    protected void onRestart() {

        Log.e(TAG,"onRestart");
        super.onRestart();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError("Inserire Email Valida");
            requestFocus(inputEmail);

            Log.e(TAG, "Email Non Valida");
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validaPassword() {
        if (!inputPassowrd.getText().toString().trim().equals("")) {
            if (inputConfermaPassword.getText().toString().trim() != null && inputPassowrd.getText().toString().trim().equals(inputConfermaPassword.getText().toString().trim())) {
                inputLayoutConferma.setErrorEnabled(false);

                return true;
            } else {
                inputLayoutConferma.setError("Password Non corrispondenti");
                return false;
            }
        } else {
            inputConfermaPassword.setEnabled(false);
            requestFocus(inputPassowrd);
            return false;
        }

    }

    private void enableConferma() {

        if (inputPassowrd.getText().toString().trim() != null) {

            if (inputConfermaPassword.getText().toString().trim() != null || !inputConfermaPassword.getText().toString().trim().equals("")) {
                if (!inputPassowrd.getText().toString().trim().equals(inputConfermaPassword.getText().toString().trim())) {
                    inputLayoutConferma.setError("Password Non corrispondenti");

                } else {
                    inputLayoutConferma.setErrorEnabled(false);

                }
            }
            inputConfermaPassword.setEnabled(true);
            inputConfermaPassword.addTextChangedListener(new MyTextWatcher(inputConfermaPassword));

        }

    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class MyTextWatcher implements TextWatcher {


        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          switch (view.getId()) {
                case R.id.confermapasword:
                    validaPassword();
                    enableConferma();
                    break;
            }


        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.email:
                    validateEmail();
                    break;
                case R.id.confermapasword:
                    validaPassword();
                    break;
                case R.id.password:
                    enableConferma();
                    break;
            }
        }
    }


    class ControlloEsistenzaUtente extends AsyncTask<Void, Void, Void>
    {
        String rispostaServer;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.e(TAG,"PARTENXZA THREAD");
                URL url = new URL(EndPoint.BASE_URL+"ControlloEsistenzaUtente.php");
                HttpURLConnection httpsURLConnection = (HttpURLConnection) url.openConnection();
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setRequestMethod("POST");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpsURLConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("email",email).appendQueryParameter("username",username);

                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();
                // ricevo la risposta


                httpsURLConnection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);
                rispostaServer = stringBuilder.toString();

               // Log.e(TAG,stringBuilder.toString());
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

            if(rispostaServer!=null) {

                progressDialog.hide();
                progressDialog.dismiss();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                try {
                    JSONObject dataObj = new JSONObject(rispostaServer);
                    if (dataObj.getBoolean("errore") == false) {

                        Log.e(TAG,"FINE THREAD");
                        switch (Integer.parseInt(dataObj.getString("risultato"))) {
                            case 0:
                                inputLayoutEmail.setError("Indirizzo email già utilizzato");
                                inputLayoutUsername.setError("Username già utilizzato");

                                break;
                            case 1:
                                inputLayoutEmail.setError("Email già utilizzato");
                                break;
                            case 2:
                                inputLayoutUsername.setError("Username già utilizzato");
                                break;
                            case 3:
                                Intent intent = new Intent(ActivityRegistrazione1.this, ActivityRegistrazione2.class);
                                intent.putExtra("username", username);
                                intent.putExtra("password", password);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                break;

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), dataObj.getString("risultato"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }

    }


}

