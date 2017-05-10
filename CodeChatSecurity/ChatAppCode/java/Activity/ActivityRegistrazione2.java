package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Application.PersonaleKey;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Gcm.GcmIntentService;
import com.example.dado.chatsecurity.Manifest;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.Model.Utility;
import com.example.dado.chatsecurity.R;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Dado on 03/08/2016.
 */

/*
 *  parte in cui gestiamo l'immagine dell'utente
 *
 *   creiamo le chiavi pubbliche e private di esso
 *   e poi otteniamo il token da google
 *   e infine premendo il tasto invio inviamo tutto al server
 *   nostro ed effettiamo l'effettiva registrazione dell'utente
 */

public class ActivityRegistrazione2 extends AppCompatActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private static final String TAG = ActivityRegistrazione2.class.getSimpleName();
    PersonaleKey personaleKey;
    Button loadImage, sendDataServer, Retry;
    String encodedString, imagePath, fileName, username, email, password,token;
    Bitmap bitmap;
    private ArrayList<User> arrayListContatti;
    private ArrayList<Conversazioni> arrayListConversazioni;
    private ArrayList<Message> arrayListMessage;
    private static int RESULT_LOAD_IMG = 1;
    private static final String name_file = "pref";
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CircleImageView imgView;
    ProgressDialog prgDialog;
    boolean fineRegistrazioneChiavi=false;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST=9000;
    Intent intentActivityRegistrazione;
    private String userChoosenTask;
    final static int REQUEST_CAMERA=1,SELECT_FILE=2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione2);
        Log.e(TAG,"inizioPersonalKey");
        prgDialog = new ProgressDialog(this);

        setRequestedOrientation(getResources().getConfiguration().orientation);
        prgDialog.setMessage("Attendere");
        prgDialog.show();
        new GenerazioneChiavi().execute();
        Log.e(TAG, "finePersonalKey");
        loadImage = (Button) findViewById(R.id.loadImage);
        intentActivityRegistrazione = getIntent();
        imgView = (CircleImageView) findViewById(R.id.imgView);
        preferences = getSharedPreferences(name_file, 0);
        editor = preferences.edit();
        if (preferences == null || preferences.getString("imagePath", null) == null) {
            //se il file sharedPreference è vuoto allora dobbiamo usre le immagine di default
            preferences = getSharedPreferences(name_file, 0);
            editor = preferences.edit();
            imgView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.profile_default));
        } else {
            imgView.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));

        }


        //quando cambieremo activity usare finish()per far si che l'utente non possa piu tornare indietro
        if (intentActivityRegistrazione != null) {
            username = intentActivityRegistrazione.getStringExtra("username");
            email = intentActivityRegistrazione.getStringExtra("email");
            password = intentActivityRegistrazione.getStringExtra("password");
            //Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        }

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        ((Button) findViewById(R.id.retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityRegistrazione1.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });


        //button per invio dei dati
        sendDataServer =(Button)findViewById(R.id.sendDataToServer);
        sendDataServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.getInstance().haveInternetConnection()) {

                    if(fineRegistrazioneChiavi) {
                        if (checkPlayServices()) {
                            sendDataServer.setEnabled(false);
                            setRequestedOrientation(getResources().getConfiguration().orientation);
                            prgDialog.setMessage("Ottennimento GCM");
                            prgDialog.show();
                            registerGCM();

                        } else {
                            Log.d(TAG, "Device non supportato");
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"Attendere qualche secondo ",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Per effettuare la registrazione bisogna avere una connessione internet",Toast.LENGTH_LONG).show();

                }
            }
        });


        //broadcast receiver che ascolta l'intent che ci dice che abbiamo ottenuto il gcm
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //se cattura l'intent che mi esegue la registrazione
                //per il token al gcm
                //sono qua sotto
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){

                    token=intent.getStringExtra("token");
                    // Log.e(TAG,intent.getStringExtra("token"));
                    uploadImage();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mRegistrationBroadcastReceiver,new IntentFilter(Config.REGISTRATION_COMPLETE));


        if (preferences == null || preferences.getString("imagePath", null) == null) {
            preferences = getSharedPreferences(name_file, 0);
            editor = preferences.edit();
        } else {
            //per mandare i dati guardo se l'utente ha caricato o meno l'immagine
            imgView.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));

        }


    }



    @Override
    protected void onResume() {

        super.onResume();
        //Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        //loadImagefromGallery();
/*
        if (preferences == null || preferences.getString("imagePath", null) == null) {
            //se il file sharedPreference è vuoto allora dobbiamo usre le immagine di default
            preferences = getSharedPreferences(name_file, 0);
            editor = preferences.edit();
            imgView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.profile_default));
        } else {
            imgView.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));

        }


        //quando cambieremo activity usare finish()per far si che l'utente non possa piu tornare indietro
        if (intentActivityRegistrazione != null) {
            username = intentActivityRegistrazione.getStringExtra("username");
            email = intentActivityRegistrazione.getStringExtra("email");
            password = intentActivityRegistrazione.getStringExtra("password");
            //Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        }

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        ((Button) findViewById(R.id.retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityRegistrazione1.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });


        //button per invio dei dati
        sendDataServer =(Button)findViewById(R.id.sendDataToServer);
        sendDataServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.getInstance().haveInternetConnection()) {

                    if (checkPlayServices()) {
                        sendDataServer.setEnabled(false);
                        prgDialog.setMessage("Ottennimento GCM");
//                        prgDialog.show();
                        registerGCM();

                    } else {
                        Log.d(TAG, "Device non supportato");
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Per effettuare la registrazione bisogna avere una connessione internet",Toast.LENGTH_LONG).show();
                }
            }
        });


        //broadcast receiver che ascolta l'intent che ci dice che abbiamo ottenuto il gcm
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //se cattura l'intent che mi esegue la registrazione
                //per il token al gcm
                //sono qua sotto
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){

                    token=intent.getStringExtra("token");
                    // Log.e(TAG,intent.getStringExtra("token"));
                    uploadImage();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mRegistrationBroadcastReceiver,new IntentFilter(Config.REGISTRATION_COMPLETE));


        if (preferences == null || preferences.getString("imagePath", null) == null) {
            preferences = getSharedPreferences(name_file, 0);
            editor = preferences.edit();
        } else {
            //per mandare i dati guardo se l'utente ha caricato o meno l'immagine
            imgView.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));

        }
        */
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        prgDialog.dismiss();
    //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onPause();
    }

    @Override
    protected void onStop() {
        prgDialog.dismiss();
     //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prgDialog.dismiss();
  //      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        Intent intent = new Intent(getApplicationContext(), ActivityRegistrazione1.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        startActivity(intent);

    }

    //*******************************************************************//
    //**************per la visualizzazione delle immagini****************//
    //******************************************************************//


    //verifica dei permessi e partono i vari intent
    private void selectImage(){
        final CharSequence[] items = { "Scatta Foto", "Scegli dalla galleria",
                "Annulla" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegistrazione2.this);
        builder.setTitle("Carica Immagine Profilo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(ActivityRegistrazione2.this);
                if (items[item].equals("Scatta Foto")) {
                    ActivityCompat.requestPermissions(ActivityRegistrazione2.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scatta Foto";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)&&
                    !(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                            !(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                        cameraIntent();
                    }
                } else if (items[item].equals("Scegli dalla galleria")) {
                    ActivityCompat.requestPermissions(ActivityRegistrazione2.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scegli dalla galleria";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            && !(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            &&!(ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityRegistrazione2.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                        galleryIntent();
                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //intent per accedere alla fotocamer
    private void cameraIntent(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_CAMERA);
    }

    //accedere alla galleria
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Scatta Foto"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Scegli dalla galleria"))
                        galleryIntent();
                } else {
                //code for deny
                }
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if(requestCode==REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
            Log.d(TAG,imagePath);

            //le salvo nel caso cambiasse orientazione dello schermo
            editor.putString("imagePath", imagePath);
            editor.commit();
            // Set the Image in ImageView
            //imgPath -> percorso immagine
            if (imgView != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {

                    imgView.setImageBitmap(BitmapFactory.decodeFile(imagePath));

                }else
                    Toast.makeText(this, "BIitmap caricare Immagine", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Impossibile caricare Immagine", Toast.LENGTH_SHORT).show();
            }
            // Get the Image's file name
            String fileNameSegments[] = imagePath.split("/");
            //nome effettivo del file
            fileName = fileNameSegments[fileNameSegments.length - 1];
        }

    }

    private void onCaptureImageResult(Intent data){

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String nameFile=System.currentTimeMillis()+"jpg";
        File destination = new File(Environment.getExternalStorageDirectory(),
                nameFile);
        FileOutputStream fo;

        try{
            destination.createNewFile();
            fo=new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        fileName=nameFile;
        imagePath=destination.getAbsolutePath();
        editor.putString("imagePath", imagePath);
        editor.commit();
        Log.d(TAG,imagePath);
        if(imgView!=null)
            imgView.setImageBitmap(thumbnail);

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
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else{
                Log.i(TAG,"DEVICE NON supportato GOOGLE API SERVICE");
                Toast.makeText(getApplicationContext(),"Google Play Services Non supportati",Toast.LENGTH_LONG);
                finish();
            }
            return false;
        }
        return true;

    }

    private void registerGCM() {
        Intent intent =new Intent(this, GcmIntentService.class);
        //Log.d(TAG,"REGISTER");
        intent.putExtra("key","register");
        startService(intent);
    }


    //*****************************METODI PER INVIARE TUTTO AL SERVER ***********************//////////////
    //conversione delle immagine
    public void uploadImage() {
        // When Image is selected from Gallery

 //       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        prgDialog.setMessage("Conversione dell'Immagine");
        prgDialog.show();
        // Convert image to String using Base64
        encodeImageToString();
        // When Image is not selected from Gallery

    }

    //codifica dell'immagine in stringa cosi da potrela invuare al server
    public void encodeImageToString() {

        new compressImageAsincrono().execute();
    }



    //**************compressione dell'immagine*******************//////////
    public class compressImageAsincrono extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //ottengo le opzioni dell'oggetto bitmap
            BitmapFactory.Options options = null;
            options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            //carico l'iimmagine bitmap primo controllo se l'utente ne ha caricata una
            //se cosi non fosse prendo l'immagine di default
            if (imagePath != null && !imagePath.isEmpty() && preferences != null && preferences.getString("imagePath", null) != null)
                bitmap = BitmapFactory.decodeFile(imagePath, options);
            else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default);
                fileName="profile_default.jpg";
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //lo comprimo
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            //otengo i byte
            byte[] byte_arr = stream.toByteArray();
            //immagine di default
            encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

      //      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            prgDialog.setMessage("Invio i dati al server");
            prgDialog.show();
            new sendDataToServer().execute(encodedString);


        }
    }

    //**************invio di tutto al server *****************/////////
    public class sendDataToServer extends AsyncTask<String, Void, Void> {

        String messaggioInput;

        @Override
        protected Void doInBackground(String... params) {
            try {
                //URL url=new URL("http://webdev.dibris.unige.it/~S3928125/saveImageOnServer.php");
                URL url = new URL(EndPoint
                        .BASE_URL+"registrazione.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                //attacco tutti i parametri che voglio inviare al server
                builder.appendQueryParameter("image", params[0])
                        .appendQueryParameter("filename", fileName)
                        .appendQueryParameter("username",username)
                        .appendQueryParameter("email",email)
                        .appendQueryParameter("password",password)
                        .appendQueryParameter("publicKey",personaleKey.getPublicKeyString())
                        .appendQueryParameter("privateKey",personaleKey.getPrivateKey())
                        .appendQueryParameter("gcm",token);
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
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);

                messaggioInput = stringBuilder.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SSLHandshakeException e) {
                //se certificato no valido
                Toast.makeText(getApplicationContext(),"RIPROVARE PIU TARDI",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(ActivityRegistrazione2.this, ActivityIniziale.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
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
            Log.d(TAG, messaggioInput);

      //      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            try {
                JSONObject dataObj = new JSONObject(messaggioInput);
                if (!dataObj.getBoolean("errore")) {


                    //Log.d(TAG,messaggioInput);
                    //Log.d(TAG,dataObj.toString());
                    // salvo l'user cosi lo posso recuperare

                    //Log.e(TAG,personaleKey.getPublicKeyString()+"; "+ personaleKey.getPrivateKey()+"; "+ dataObj.getString("id")+"; "+username+"; "+ email);
                    User user_self = new User(personaleKey.getPublicKeyString(), personaleKey.getPrivateKey(), dataObj.getString("id"), username, email,dataObj.getString("urlImage"),dataObj.getString("password"));
                    user_self.setPasswordMD5(ActivityLogin.md5(password));

                    MyApplication.getInstance().getPrefManager().storeUser(user_self);


                    //Toast.makeText(getApplicationContext(), dataObj.getString("id"), Toast.LENGTH_LONG).show();

                    new ScaricaContatti().execute();

                } else {
                    Toast.makeText(getApplicationContext(), dataObj.getString("risultato"), Toast.LENGTH_LONG).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Toast.makeText(getApplicationContext(), "REGISTRAZIONE COMPLETATA", Toast.LENGTH_LONG).show();
        }
    }



    private class GenerazioneChiavi extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            personaleKey = new PersonaleKey();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prgDialog.dismiss();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            fineRegistrazioneChiavi=true;
        }
    }


        private class ScaricaContatti extends AsyncTask<Void,Void,Void> {


        String fileJSON ;
        @Override
        protected Void doInBackground(Void... params) {

            arrayListContatti = new ArrayList<>();

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

                JSONObject jsonObject = new JSONObject(fileJSON);
                if(!jsonObject.getBoolean("errore")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("user");

                    for (int i =0;i<jsonArray.length();i++) {

                        JSONObject jsonUser = jsonArray.getJSONObject(i);
                        //controllo che gli utenti non siano io
                        //Log.e(TAG,MyApplication.getInstance().getPrefManager().getUser().toString());
                        try {
                                String public_key = jsonUser.getString("publicKey").toString();
                                String id_user = jsonUser.getString("user_id").toString();
                                String username = jsonUser.getString("username").toString();
                                String email = jsonUser.getString("email").toString();
                                String id_immagine_profilo = jsonUser.getString("id_immagine").toString();
                                String url_immagine_profilo = jsonUser.getString("urlImage").toString();

                                String[] image=url_immagine_profilo.split("/");
                                //Log.e(TAG,image[image.length-1]);

                                String statoPersonale=jsonUser.getString("statoPersonale");
                                // se l'id è uguale a quello con cui sono entrato allora non deve aggiungere nula
                                //MyApplication.getInstance().getBitmapFromURL(EndPoint.BASE_URL+id_immagine_profilo);
                                // scarico solo il nome del file relativo alla immagine del profilo
                                //Log.e(TAG,id_user);
                                arrayListContatti.add(new User(public_key, id_user, username, email, image[image.length-1]));
                                arrayListContatti.get(arrayListContatti.size()-1).setStatoPersonale(statoPersonale);

                        }catch (Exception e){
                            Log.d(TAG,e.getMessage());
                        }

                    }


                //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    //inserisco tutti i contatti nel DB
                    MyApplication.getInstance().getDbChat().insertAllContatti(arrayListContatti);

                    new downloadAndSaveImage().execute();


                }else{
                    Toast.makeText(getApplicationContext(),jsonObject.getString("risultato"),Toast.LENGTH_SHORT).show();
                }


            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    private class downloadAndSaveImage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            //tutti gli user dei nostri contatti

            for(User user:arrayListContatti){
                Log.e(TAG, user.getUrlImage());
                Bitmap b=getBitmapFromURL(EndPoint.BASE_URL+"uploadedimages/"+user.getUrlImage());
                String path=saveToInternalStorage(b,user.getUrlImage());
                Log.d(TAG,path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prgDialog.hide();
            prgDialog.dismiss();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Intent intent=new Intent(ActivityRegistrazione2.this,MainActivity.class);
            intent.putExtra("main ricevuto",true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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

}

