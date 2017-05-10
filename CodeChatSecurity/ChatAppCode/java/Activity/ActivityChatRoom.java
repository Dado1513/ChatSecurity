package com.example.dado.chatsecurity.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ChatRoomMessageAdapter;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Gcm.NotificationUtils;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.Model.Utility;
import com.example.dado.chatsecurity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Dado on 20/08/2016.
 */
public class ActivityChatRoom extends AppCompatActivity {
    private String TAG=ActivityChatRoom.class.getSimpleName();
    private RecyclerView recyclerView;
    private ChatRoomMessageAdapter adapter;
    private ArrayList<Message> messages;
    private EditText inputMessage;
    private Button btnSendMessage,addPhoto;
    private static final String name_file = "pref";
    Bitmap bitmap;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private String userChoosenTask;
    final static int REQUEST_CAMERA=1,SELECT_FILE=2;

    Conversazioni conversazioni;
    String user_id_self;
    User other,user;
    String messaggiotoSend;
     final int maxLengthMessage=(4096/8)-11;
    //int maxLengthMessage=15;

    String messageForMe,messageForOther;
    int paginaDaVisualizzare;
    String encodedString, imagePath, fileName;
    PublicKey publicKeyItem;
    PublicKey publicKeyOther;
    String messaggio;
    BroadcastReceiver broadcastReceiver;
    ProgressDialog progressDialog;
    ImageView imageView;
    Boolean invioImmagine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        inputMessage = (EditText) findViewById(R.id.messageChatRoom);
        btnSendMessage = (Button) findViewById(R.id.btn_send_message);
        Intent intent = getIntent();
        paginaDaVisualizzare = intent.getIntExtra("PosizionePagina",0);
        preferences = getSharedPreferences(name_file, 0);
        editor = preferences.edit();
        progressDialog=new ProgressDialog(this);

        invioImmagine=false;
        imageView=(ImageView)findViewById(R.id.imageBackgroudChatRoom);
        String name= getSharedPreferences(Config.nameFileImageUrl,0).getString("nameImage",null);
        if(name!=null){
            imageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(name));
            int alpha=getSharedPreferences(Config.nameFileImageUrl,0).getInt("alpha",0);
            imageView.setAlpha(alpha/(10.0f));
         //   Log.e(TAG,"IMPOSTO IMMAGINE");
        }
//        imageView=(ImageView)findViewById(R.id.imageBackgroudChatRoom);
//      String [] name=MyApplication.getInstance().getPrefManager().getUser().getUrlImage().split("/");
//        imageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(name[name.length-1]));

        Log.e(TAG,"onCreteChat");
        conversazioni = (Conversazioni) intent.getSerializableExtra("conversation");
        //Log.e(TAG,conversazioni.getIdUtente());
        other = (User) intent.getSerializableExtra("user");
        //Log.e(TAG,other.NAME);
        //Log.d(TAG,other.NAME+other.getPublicKey()+other.EMAIL);
        user = MyApplication.getInstance().getPrefManager().getUser();
        publicKeyItem = user.getPublicKey();
        publicKeyOther = other.getPublicKey();
        messages = MyApplication.getInstance().getDbChat().getMessageFromConversation(conversazioni);

        // Log.e(TAG,String.valueOf(messages.size()));
        if (conversazioni.getConversazioniId().equals("0")==false){
            conversazioni.azzeraUnreadCount();
            MyApplication.getInstance().getDbChat().updateConversation(conversazioni);

        }

        addPhoto=(Button)findViewById(R.id.addPhoto);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_viewChatRoom);
        adapter=new ChatRoomMessageAdapter(this,messages,user.getID_USER());
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        if(adapter.getItemCount()>0){
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,adapter.getItemCount()-1);
        }

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cifro il messaggio con le due chiavi pubbliche7
                if (MyApplication.haveInternetConnection()) {
                    adapter.notifyDataSetChanged();
                    if (adapter.getItemCount() > 0)
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                    messaggio = inputMessage.getText().toString();
                    btnSendMessage.setEnabled(false);
                    setRequestedOrientation(getResources().getConfiguration().orientation);
                    if(messaggio.getBytes().length<maxLengthMessage) {
                       // Log.e(TAG, publicKeyItem.toString());
                        //messaggiotoSend = Base64.encodeToString(messaggio.getBytes(), Base64.DEFAULT);
                        //Log.e(TAG, String.valueOf(messaggiotoSend.getBytes().length) + "---->" + String.valueOf(messaggio.getBytes().length));
                        messageForOther = MyApplication.getInstance().cifraMessaggio(publicKeyOther, messaggio);
                        messageForMe = MyApplication.getInstance().cifraMessaggio(publicKeyItem, messaggio);


                        progressDialog.setMessage("Attendere");
                        progressDialog.show();

                        new sendMessageServer().execute();
                    }else {
                        //scompongo il messaggio d acifrare in length()/511
                        //siccome algoritmo accetta parole che abbiano meno di 511
                        //caratteri essendo la lunghezza dei byte del modulo RSA
                        //stringa separata in

                      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                        progressDialog.setMessage("Attendere");

                        progressDialog.show();

                        int max=messaggio.getBytes().length/maxLengthMessage;
                        //Log.d(TAG,String.valueOf(max));
                        max+=1;
                        //array list contente tutte le stringhe
                        ArrayList<String> arrayMessage=new ArrayList<String>();
                        for(int i=0;i<max;i++){
                            if((i+1)*maxLengthMessage<messaggio.getBytes().length) {
                                String attuale=(messaggio.substring(i * maxLengthMessage, (i+1) * (maxLengthMessage)));
                                arrayMessage.add(attuale);
                                Log.e(TAG,attuale);
                            }else {
                                String attuale=(messaggio.substring(i * maxLengthMessage, messaggio.getBytes().length ));
                                arrayMessage.add(messaggio.substring(i * maxLengthMessage, messaggio.getBytes().length));
                                Log.e(TAG,attuale);
                            }
                        }

                        //costruisco la stringa da inviare
                        //string builder da inviare
                        //ogni parte è composta da 693 caratter che dividero nuovamente in ricezione
                        //alla stessa maniera
                        StringBuilder stringBuilderForOther=new StringBuilder("");
                        StringBuilder stringBuilderForMe=new StringBuilder("");
                        for(int i=0;i<arrayMessage.size();i++) {
                            stringBuilderForMe.append(MyApplication.getInstance().cifraMessaggio(publicKeyItem, arrayMessage.get(i)));
                            stringBuilderForOther.append(MyApplication.getInstance().cifraMessaggio(publicKeyOther, arrayMessage.get(i)));
                        }
                        //messaggio da inviare
                        //cifrato con la mia chiave pubblic e con la chiave pubblica del destinatario
                        messageForMe=stringBuilderForMe.toString();
                        messageForOther=stringBuilderForOther.toString();
                        //Log.d(TAG,"cifrato"+cifrato.toString());
                        arrayMessage.clear();

                      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                        new sendMessageServer().execute();
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Attiva La connessione Internet per poter inviare e ricevere i messaggi", Toast.LENGTH_LONG);
                    //toast.setText(R.string.utenteGiaLoggato);
                    TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                    textView.setTextColor(Color.parseColor("#FF4500"));
                    toast.show();
                }

            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("newMessage")) {
                  //devo aggiornale la chat attuale;
                    Conversazioni conversazioneIntente=(Conversazioni)intent.getSerializableExtra("conversazione");
                    //il messaggio arrivato proviene dalla stessa conversazione
                    if(conversazioneIntente.getConversazioniId().equals(conversazioni.getConversazioniId())) {
                        messages.add((Message) intent.getSerializableExtra("message"));
                        conversazioneIntente.azzeraUnreadCount();
                        MyApplication.getInstance().getDbChat().updateConversation(conversazioneIntente);
                        adapter.notifyDataSetChanged();
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                    }
                }
            }
        };

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.haveInternetConnection()) {
                    selectImage();


                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Attiva La connessione Internet per poter inviare e ricevere i messaggi", Toast.LENGTH_LONG);
                    //toast.setText(R.string.utenteGiaLoggato);
                    TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                    textView.setTextColor(Color.parseColor("#FF4500"));
                    toast.show();
                }

            }
        });


    }



    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,MainActivity.class);
        Log.e(TAG,"BACK PRESSED");
        startActivity(i);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        if(adapter.getItemCount()>0){
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,adapter.getItemCount()-1);
        }


        NotificationUtils.clearNotifications();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,new IntentFilter("newMessage"));
    }


    @Override
    protected void onRestart() {
       super.onRestart();
        Log.e(TAG,"onRestart");
        if(adapter.getItemCount()>0){
            Log.e(TAG,"SONO DENTRO"+adapter.getItemCount());
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,adapter.getItemCount()-1);
        }

        NotificationUtils.clearNotifications();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,new IntentFilter("newMessage"));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Log.e(TAG,"onPAuse");
        //MainActivity.numeroPagina=paginaDaVisualizzare;
        progressDialog.dismiss();
        super.onPause();

    }


    private class sendMessageServer extends AsyncTask<Void,Void,Void>{

        String output;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url=new URL(EndPoint.BASE_URL+"sendMessage.php");

                Uri.Builder builder=new Uri.Builder();
                HttpURLConnection httpsURLConnection=(HttpURLConnection)url.openConnection();
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestMethod("POST");
                //se la conversazione non esiste ancora invio un valore 0
                builder.appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER())
                        .appendQueryParameter("root","caputotavellamantovani99")
                        .appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5())
                        .appendQueryParameter("id_conversazione",conversazioni.getConversazioniId())
                        .appendQueryParameter("textOther",messageForOther)
                        .appendQueryParameter("textMy",messageForMe)
                        .appendQueryParameter("idOther",conversazioni.getIdUtente())
                        .appendQueryParameter("isImage","0");

                String data= builder.build().getEncodedQuery();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpsURLConnection.getOutputStream()));
                writer.write(data);
                writer.flush();
                writer.close();


                StringBuilder stringBuilder = new StringBuilder();

                //provare con lo scanner??
                //Scanner in=new Scanner(httpsURLConnection.getInputStream());
                //
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                output = stringBuilder.toString();

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
            try {
                Log.d(TAG,output);
                JSONObject messaggioJson=new JSONObject(output);

                if(!messaggioJson.getBoolean("errore")){
                    if(conversazioni.getConversazioniId().equals("0")){
                        conversazioni.setID(messaggioJson.getString("id_conversation"));
                        Log.d(TAG,"insertConversation");
                        MyApplication.getInstance().getDbChat().addConversazione(conversazioni);

                    }


                    Message message=new Message(
                            messaggioJson.getString("id_message"),
                            conversazioni.getConversazioniId(),MyApplication.getInstance().getPrefManager().getUser().ID_USER,
                            messaggio,
                            messaggioJson.getString("ora"),
                            false);
                    messages.add(message);
                    MyApplication.getInstance().getDbChat().addMessagetoSent(message);
                    adapter.notifyDataSetChanged();
                    inputMessage.setText("");
                    if(adapter.getItemCount()>0){
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,adapter.getItemCount()-1);
                    }



                    playNotificationSound();
                    progressDialog.hide();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);


                }else{
                    Log.e(TAG,messaggioJson.getString("risultato"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public void playNotificationSound(){
        try {
            Uri alarmSound= Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+MyApplication.getInstance().getApplicationContext().getPackageName()+"/raw/notification");
            Ringtone r= RingtoneManager.getRingtone(MyApplication.getInstance(),alarmSound);
            r.play();
            btnSendMessage.setEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //verifica dei permessi e partono i vari intent
    private void selectImage(){
        final CharSequence[] items = { "Scatta Foto", "Scegli dalla galleria",
                "Annulla" };
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityChatRoom.this);
       alertDialog.setTitle("Invia una immagine");

        alertDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(ActivityChatRoom.this);
                if (items[item].equals("Scatta Foto")) {
                    ActivityCompat.requestPermissions(ActivityChatRoom.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scatta Foto";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)&&
                            !(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                            !(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                        cameraIntent();
                    }
                } else if (items[item].equals("Scegli dalla galleria")) {
                    ActivityCompat.requestPermissions(ActivityChatRoom.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scegli dalla galleria";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            && !(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            &&!(ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityChatRoom.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                        galleryIntent();
                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });

        alertDialog.show();
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
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if(requestCode==REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            Log.e(TAG, cursor.getString(column_index));
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};


        imagePath=getPath(getApplicationContext(),selectedImage);
        // Get the cursor
        if(imagePath==null)
            Log.e(TAG,"null");
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
            //  if (cursor != null) {
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // imagePath = cursor.getString(columnIndex);
            cursor.close();
            //            Log.d(TAG,imagePath);


            //le salvo nel caso cambiasse orientazione dello schermo
            editor.putString("imagePath", imagePath);
            editor.commit();
            // Set the Image in ImageView
            //imgPath -> percorso immagine
            // Get the Image's file name
            BitmapFactory.Options options = null;
            options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            Log.e(TAG,"prima creazione bitmap");
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            Log.e(TAG,"split nome");
            String fileNameSegments[] = imagePath.split("/");
            //nome effettivo del file
            fileName = fileNameSegments[fileNameSegments.length - 1];
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityChatRoom.this);
            // Add the buttons
            builder.setMessage("Inviare l'immagine?");
            ImageView imageView=new ImageView(ActivityChatRoom.this);
            imageView.setImageBitmap(bitmap);
            builder.setView(imageView);
            builder.setPositiveButton("Invio", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    invioImmagine=true;

                  //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                    setRequestedOrientation(getResources().getConfiguration().orientation);
                    progressDialog.setMessage("Attendere");
                    progressDialog.show();
                    new compressImageAsincrono().execute();


                }
            });
            builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
      //  }

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
        //immagine da inviare è la thumbnail


        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityChatRoom.this);
        // Add the buttons
        builder.setMessage("Inviare l'immagine?");
        ImageView imageView=new ImageView(ActivityChatRoom.this);
        imageView.setImageBitmap(thumbnail);
        builder.setView(imageView);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                invioImmagine=true;

             //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                setRequestedOrientation(getResources().getConfiguration().orientation);
                progressDialog.setMessage("Attendere");
                progressDialog.show();
                new compressImageAsincrono().execute();


            }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();



    }


    public class compressImageAsincrono extends AsyncTask<Void, Void, Void> {

        String output;
        @Override
        protected Void doInBackground(Void... params) {
            try{
            //ottengo le opzioni dell'oggetto bitmap

            BitmapFactory.Options options = null;
            options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            //carico l'iimmagine bitmap primo controllo se l'utente ne ha caricata una
            //se cosi non fosse prendo l'immagine di default
            if (imagePath != null && !imagePath.isEmpty() && preferences != null && preferences.getString("imagePath", null) != null) {
                bitmap = BitmapFactory.decodeFile(imagePath, options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //lo comprimo
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                //otengo i byte

                //ora qua dovremmo cifrare i byte che compongono il messaggio o con un algoritmo a chiave simmetrica o con rsa salvarlo
                //sul server immagini cifrate non in chiaro e ogni volta prima di ricomporre le immagini decifrarle

                byte[] byte_arr = stream.toByteArray();

                encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
                //si cifra questo e si invia il problema è che android 6 non lo legge tutto si blocca come se ci fossere dei limiti
                Log.e(TAG,"byte-->"+byte_arr.length+"  ;"+"  string-->"+encodedString.length());
                //**********************************//
                URL url = new URL(EndPoint.BASE_URL + "sendMessage.php");
                //Log.e(TAG,""+encodedString.length());
                Uri.Builder builder = new Uri.Builder();
                HttpURLConnection httpsURLConnection = (HttpURLConnection) url.openConnection();
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestMethod("POST");
                //Log.e(TAG,encodedString);
                //se la conversazione non esiste ancora invio un valore 0
                builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER())
                        .appendQueryParameter("root", "caputotavellamantovani99")
                        .appendQueryParameter("password", MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5())
                        .appendQueryParameter("id_conversazione", conversazioni.getConversazioniId())
                        .appendQueryParameter("textOther", encodedString)
                        .appendQueryParameter("textMy", encodedString)
                        .appendQueryParameter("idOther", conversazioni.getIdUtente())
                        .appendQueryParameter("length", String.valueOf(encodedString.length()))
                        .appendQueryParameter("filename", fileName)
                        .appendQueryParameter("isImage", "1");
                //encode->messageFo


                String data = builder.build().getEncodedQuery();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpsURLConnection.getOutputStream()));
                writer.write(data);
                writer.flush();
                writer.close();


                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }

                output = stringBuilder.toString();


            } else {
                Log.e(TAG, "iamgenull");
            }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //stringa da inviare per messaggio
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            invioImmagine=false;

            super.onPostExecute(aVoid);
            try {
                Log.d(TAG,output);
                JSONObject messaggioJson=new JSONObject(output);

                if(!messaggioJson.getBoolean("errore")){
                    if(conversazioni.getConversazioniId().equals("0")){
                        conversazioni.setID(messaggioJson.getString("id_conversation"));
                        Log.d(TAG,"insertConversation");
                        MyApplication.getInstance().getDbChat().addConversazione(conversazioni);

                    }

                    String[] url=messaggioJson.getString("text").split("/");
                    saveToInternalImageSendAndReceive(bitmap,url[url.length-1]);

                    Message message=new Message(
                            messaggioJson.getString("id_message"),
                            conversazioni.getConversazioniId(),MyApplication.getInstance().getPrefManager().getUser().ID_USER,
                            messaggioJson.getString("text"),
                            messaggioJson.getString("ora"),
                            true);
                    messages.add(message);
                    MyApplication.getInstance().getDbChat().addMessagetoSent(message);
                    adapter.notifyDataSetChanged();
                    inputMessage.setText("");
                    if(adapter.getItemCount()>0){
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,adapter.getItemCount()-1);
                    }



                    playNotificationSound();
                    progressDialog.hide();

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }else{
                    Log.e(TAG,messaggioJson.getString("risultato"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String saveToInternalImageSendAndReceive(Bitmap bitmap,String name){
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageChat", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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




    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
