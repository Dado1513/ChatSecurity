package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Gcm.GcmIntentService;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by Dado on 02/08/2016.
 */
public class ActivityLogin extends AppCompatActivity {

    private static final String TAG = ActivityLogin.class.getSimpleName();
    Button sendData;
    ProgressDialog progressDialog;
    private EditText inputEmail, inputPassowrd;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private TextView passwordDimenticata;
    ArrayList<User> arrayListUser;
    ArrayList<Conversazioni> arrayListConversazioni;
    ArrayList<Message> arrayListMessage;
    private String email,password,token;
    BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email_login);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password_login);
        inputEmail = (EditText) findViewById(R.id.emailLogin);
        inputPassowrd = (EditText) findViewById(R.id.passwordLogin);
        passwordDimenticata=(TextView)findViewById(R.id.passwordDimenticata);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));

        passwordDimenticata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplication(),PasswordSmarrita.class));

            }
        });


        if (checkPlayServices()) {
            if (MyApplication.haveInternetConnection()) {
                registerGCM();
            }
        }
                sendData=(Button)findViewById(R.id.sendDataToServerLogin);
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controllare gli input
                if (checkPlayServices()) {
                    if (MyApplication.haveInternetConnection()) {
                        if (inputPassowrd.getText().toString().trim() != null && !inputPassowrd.getText().toString().trim().equals("") && validateEmail()) {
                            email = inputEmail.getText().toString().trim();
                            password = inputPassowrd.getText().toString().trim();
                         //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                            setRequestedOrientation(getResources().getConfiguration().orientation);
                            progressDialog.setMessage("Verifica Dati");
                            progressDialog.show();
                            new controlUser().execute();
                            // apro la pagina dei fragment
                        } else {
                            Toast.makeText(getApplicationContext(), "Riempire Tutti I campi", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Attivare la connessione Internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //se cattura l'intent che mi esegue la registrazione
                //per il token al gcm
                //sono qua sotto
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
                    token=intent.getStringExtra("token");
                    //Log.e(TAG,intent.getStringExtra("token"));
                    sendData.setEnabled(true);

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
        progressDialog.hide();
        progressDialog.dismiss();

    //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onPause();
    }

    private void registerGCM() {
        Intent intent =new Intent(this, GcmIntentService.class);
        Log.d(TAG,"REGISTER");
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

    private class controlUser extends AsyncTask<Void,Void,Void>{

        String rispostaServer;
        @Override
        protected Void doInBackground(Void... params) {

            try {


                URL url=new URL(EndPoint.BASE_URL+"controlloLogin.php");
                HttpURLConnection urlConnection =(HttpURLConnection)url.openConnection();
                String passwordmd5= md5(password);

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("email",email).appendQueryParameter("password",passwordmd5).appendQueryParameter("root","caputotavellamantovani99").appendQueryParameter("token",token);
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
            }catch (SSLHandshakeException e){
                Toast.makeText(getApplicationContext(),"Impossibile Effettuare il login Server non Verificato",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

           Log.d(TAG,rispostaServer);
            try {
                JSONObject object= new JSONObject(rispostaServer);
                if(object.getBoolean("abilitato") && !object.getBoolean("errore")){

                   // Log.e(TAG,object.getString("id_user"));
                   // Log.e(TAG,object.getString("privateKey"));

                    User user=new User(
                            object.getString("publicKey"),
                            object.getString("privateKey"),
                            object.getString("id_user"),
                            object.getString("name"),
                            object.getString("email"),
                            object.getString("urlImage"),
                            md5(password),
                            false,
                            object.getString("ultimoAccesso"));
                    user.SELF=true;

                    //Log.e(TAG,user.getID_USER());

                    MyApplication.getInstance().getPrefManager().storeUser(user);
                    //Toast.makeText(ActivityLogin.this,md5(password),Toast.LENGTH_LONG).show();
                   // Log.e(TAG,MyApplication.getInstance().getPrefManager().getUser().getID_USER());
                    //controllo se user è stato salvato
                    //Log.d(TAG,user.toString());
                    //progressDialog.hide();

                    setRequestedOrientation(getResources().getConfiguration().orientation);
                    //ora scarico tutto i contatti
                    new ScaricaContatti().execute();
                    //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    //aggiornamentoDB();
                    //startActivity(intent);
                    //finish();
                }else{

                    progressDialog.hide();

                  //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    Toast.makeText(getApplicationContext(), "Password o Email non valida", Toast.LENGTH_SHORT).show();
                    //nel caso aggiungere un solo utente loggato alla volta e non due con lo stesso account
                   /* if(object.getString("risultato").equals("login")){

                        //toast con il quale possiamo cambiare colore al test
                        Toast toast=Toast.makeText(getApplicationContext(),"Utente Gia Loggato",Toast.LENGTH_LONG);
                        //toast.setText(R.string.utenteGiaLoggato);
                        TextView textView=(TextView)toast.getView().findViewById(android.R.id.message);
                        textView.setTextColor(Color.parseColor("#FF4500"));
                        toast.show();
                    }else{
                        Log.e(TAG,object.getString("risultato"));
                    }*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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

        }

        @Override
        public void afterTextChanged(Editable s) {
            validateEmail();

        }
    }

    private class ScaricaContatti extends AsyncTask<Void,Void,Void> {


        String fileJSON ;
        @Override
        protected Void doInBackground(Void... params) {

            //arrayList contenenti i dati dei contatti
            arrayListUser = new ArrayList<>();

            try {
                fileJSON = "";
                URL url = new URL(EndPoint.BASE_URL+"getContatti.php");
                HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.setRequestMethod("POST");

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                String data=builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();

                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                fileJSON = stringBuilder.toString();

            }catch (SSLHandshakeException e) {
                Toast.makeText(MyApplication.getInstance(),"Server Non Verificato",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {

              // Log.e(TAG,"user->"+fileJSON);
                JSONObject jsonObject = new JSONObject(fileJSON);
                if(!jsonObject.getBoolean("errore")) {
                    //array di oggetti json
                    JSONArray jsonArray = jsonObject.getJSONArray("user");
                    for (int i =0;i<jsonArray.length();i++) {
                        JSONObject jsonUser = jsonArray.getJSONObject(i);
                        //controllo che gli utenti non siano io
                        //Log.e(TAG,MyApplication.getInstance().getPrefManager().getUser().toString());
                        //Log.e(TAG,String.valueOf(MyApplication.getInstance().getPrefManager().getUser()==null));

                        String public_key = jsonUser.getString("publicKey").toString();
                        String id_user = jsonUser.getString("user_id").toString();
                        String username = jsonUser.getString("username").toString();

                        String email = jsonUser.getString("email").toString();
                        String id_immagine_profilo = jsonUser.getString("id_immagine").toString();

                        String url_immagine_profilo = jsonUser.getString("urlImage").toString();


                        String[] image=url_immagine_profilo.split("/");
                            //Log.e(TAG,image[image.length-1]);


                            // se l'id è uguale a quello con cui sono entrato allora non deve aggiungere nula
                            //MyApplication.getInstance().getBitmapFromURL(EndPoint.BASE_URL+id_immagine_profilo);
                            // scarico solo il nome del file relativo alla immagine del profilo
                        Log.e(TAG,id_user);
                        arrayListUser.add(new User(public_key, id_user, username, email, image[image.length-1]));

                        arrayListUser.get(arrayListUser.size()-1).setStatoPersonale(jsonUser.getString("statoPersonale"));

                    }


                    MyApplication.getInstance().getDbChat().insertAllContatti(arrayListUser);
                    new scaricaConversazioni().execute();
                   /* for(User user: arrayList){
                        System.out.println(user);
                    }*/
                    //parte la nuova activity
                    //inserisco i contatti i messaggi


                }else{
                    Toast.makeText(getApplicationContext(),jsonObject.getString("risultato"),Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    private class scaricaConversazioni extends AsyncTask<Void,Void,Void>{

        String conversation;
        @Override
        protected Void doInBackground(Void... params) {
            arrayListConversazioni=new ArrayList<>();
            try {
                URL url = new URL(EndPoint.BASE_URL + "getConversation.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("root", "caputotavellamantovani99").
                        appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER())
                        .appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                String data=builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();

                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                conversation = stringBuilder.toString();

            }catch (SSLHandshakeException e){
                e.printStackTrace();
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
           // Log.d(TAG,"conversazioni->"+conversation);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(conversation);

                if(!jsonObject.getBoolean("errore")) {
                    //array di oggetti json
                    JSONArray jsonArray = jsonObject.getJSONArray("conversazioni");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonConversazioni = jsonArray.getJSONObject(i);
                        //salvo solo l'identificativo dell'altro utente
                        //Log.e(TAG,MyApplication.getInstance().getPrefManager().getUser().toString());
                        //Log.e(TAG,jsonConversazioni.getString("user_2"));
                        if(jsonConversazioni.getString("user_1").equals(MyApplication.getInstance().getPrefManager().getUser().getID_USER())){
                            //Log.d(TAG,"add->"+jsonConversazioni.getString("user_2"));
                            arrayListConversazioni.add(new Conversazioni(jsonConversazioni.getString("id_conversation"),jsonConversazioni.getString("user_2")));
                            User user=MyApplication.getInstance().getDbChat().getUserFromConversazioni(arrayListConversazioni.get(i));
                            MyApplication.getInstance().getDbChat().updateUserAmici(user);

                        }else{
                            arrayListConversazioni.add(new Conversazioni(jsonConversazioni.getString("id_conversation"),jsonConversazioni.getString("user_1")));
                           // Log.d(TAG,"add->"+jsonConversazioni.getString("user_1"));
                            User user=MyApplication.getInstance().getDbChat().getUserFromConversazioni(arrayListConversazioni.get(i));
                            MyApplication.getInstance().getDbChat().updateUserAmici(user);

                        }
                    }
                    MyApplication.getInstance().getDbChat().addAllConversation(arrayListConversazioni);
                    /*for(Conversazioni c:arrayListConversazioni){
                        Log.e(TAG,c.getIdUtente());
                    }
                    */
                new scaricaMessaggi().execute();

                }else{
                    Log.e(TAG,jsonObject.getString("risultato"));
                }
            } catch (JSONException e) {
                    e.printStackTrace();
            }
        }
    }

    private class scaricaMessaggi extends AsyncTask<Void,Void,Void> {

        String message;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                arrayListMessage = new ArrayList<>();
                URL url = new URL(EndPoint.BASE_URL + "getMessage.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                String data = builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();


                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                message = stringBuilder.toString();
            } catch (SSLHandshakeException e) {
                Toast.makeText(getApplicationContext(), "SERVERE NON VERIFICATO", Toast.LENGTH_LONG).show();
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
            Log.d(TAG,"message->"+message);
            JSONObject jsonObject = null;
            String lastAccess=MyApplication.getInstance().getPrefManager().getUser().getLastAccess();
            try {
                jsonObject = new JSONObject(message);

                if (!jsonObject.getBoolean("errore")) {
                    //array di oggetti json
                    JSONArray jsonArray = jsonObject.getJSONArray("messaggi");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonMessage = jsonArray.getJSONObject(i);
                        boolean image;
                        if(jsonMessage.getString("is_image").equals("0"))
                            image=false;
                        else
                            image=true;
                        //String text=MyApplication.getInstance().decifraturaMessage(MyApplication.getInstance().getPrefManager().getUser().getPrivateKey(),jsonMessage.getString("text"));
                        arrayListMessage.add(new Message(jsonMessage.getString("id_message"),
                                jsonMessage.getString("id_conversation"),
                                jsonMessage.getString("id_user"),
                                jsonMessage.getString("text"),
                                jsonMessage.getString("created_at"),
                                image));

                       // Log.e(TAG,lastAccess);
                      //  Log.e(TAG,"-> messaggio"+jsonMessage.getString("created_at"));
                        if(lastAccess!=null)
                        if(lastAccess.compareTo(jsonMessage.getString("created_at"))<0){
                            Conversazioni c=MyApplication.getInstance().getDbChat().getConversazioneFromId(jsonMessage.getString("id_conversation"));
                            c.addUnreadCount();
                            MyApplication.getInstance().getDbChat().updateConversation(c);
                            //Log.e(TAG,"sono dentro");
                        }

                    }
                    MyApplication.getInstance().getDbChat().addAllMessage(arrayListMessage);



                    new downloadAndSaveImage().execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                input.close();
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //funzione che salva l'immagine internamente dandole un nome
        private String saveToInternalStorage(Bitmap bitmapImage,String name) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageChat", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }
    }


    private class downloadAndSaveImage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            //tutti gli user dei nostri contatti
            for(User user:arrayListUser){
                Bitmap b=getBitmapFromURL(EndPoint.BASE_URL+"uploadedimages/"+user.getUrlImage());
                String path=saveToInternalStorage(b,user.getUrlImage());
               // Log.d(TAG,path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new downloadAndSaveImageMessage().execute();

        }

        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //funzione che salva l'immagine internamente dandole un nome
        private String saveToInternalStorage(Bitmap bitmapImage,String name) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageProfile", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }

    }

    public class downloadAndSaveImageMessage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            for(Message m:arrayListMessage){
                if(m.getIsImage()){
                    String [] name=m.getTesto().split("/");
                    Bitmap bitmap=getBitmapFromURL(EndPoint.BASE_URL+"imageMessage/"+name[name.length-1]);
                    saveToInternalStorage(bitmap,name[name.length-1]);
                }
            }
            return null;
        }
        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //funzione che salva l'immagine internamente dandole un nome
        private String saveToInternalStorage(Bitmap bitmapImage,String name) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageChat", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
            progressDialog.dismiss();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Intent intent=new Intent(ActivityLogin.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
    }




}

