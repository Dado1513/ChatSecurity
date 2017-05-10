package com.example.dado.chatsecurity.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by giuliotavella on 26/08/16.
 */
public class PasswordSmarrita  extends AppCompatActivity {

    Button bottone;
    EditText emailEditText;
    String email;
    TextInputLayout inputLayoutEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_smarrita);
        bottone = (Button) findViewById(R.id.buttonePasswordSmarrita);
        emailEditText = (EditText) findViewById(R.id.emailPasswordSmarrita);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email_passwordSmarrita);
        bottone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                email = emailEditText.getText().toString();
                Log.e("mailscritta =",email);
                if(!email.equals("") && MyApplication.haveInternetConnection() && email!=null) {

                    new controlloEmailRecuperoPassword().execute();
                    bottone.setClickable(false);
                }
            }
        });
    }



    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError("Inserire Email Valida");

            // Log.e(TAG, "Email Non Valida");
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    class controlloEmailRecuperoPassword extends AsyncTask<Void,Void,Void>{
        String rispostaServer;
        @Override
        protected Void doInBackground(Void... params) {

            URL url= null;
            try {
                url = new URL(EndPoint.BASE_URL+"controlloMailRecuperoPassword.php");

            HttpURLConnection urlConnection =(HttpURLConnection)url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            Uri.Builder builder=new Uri.Builder();
            builder.appendQueryParameter("mail",email);
            String query=builder.build().getEncodedQuery();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            writer.write(query);
            writer.flush();
            writer.close();

            urlConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            // Log.e(TAG,urlConnection.getPermission().toString());
            StringBuilder stringBuilder = new StringBuilder();
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null)
                stringBuilder.append(nextLine);
            rispostaServer = stringBuilder.toString();
            //Log.e("login",rispostaServer);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(Integer.valueOf(rispostaServer)>0){
                new MandaMail().execute();
            }else{
                inputLayoutEmail.setError("Email non Esistente");

            }
        }
    }

    class MandaMail extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://tavellamantovani.altervista.org/zendMail.php");
                URLConnection con = url.openConnection();
                con.setDoOutput(true);

                /* wrapper the output stream of the connection with PrintWiter so that we can write plain text to the stream */
                PrintWriter wr = new PrintWriter(con.getOutputStream(), true);

                 /* set up the parameters into a string and send it via the output stream */
                StringBuilder parameters = new StringBuilder();


                String data = URLEncoder.encode("mail", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                parameters.append(data);
                wr.println(parameters);
                wr.close();
                InputStream inputStream = new BufferedInputStream(con.getInputStream());
                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                //Log.e("mail->", stringBuilder.toString());

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(PasswordSmarrita.this,"Email Inviata",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),ActivityIniziale.class));
            finish();
        }
    }
}
