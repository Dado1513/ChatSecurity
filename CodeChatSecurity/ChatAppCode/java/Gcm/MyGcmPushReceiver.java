package com.example.dado.chatsecurity.Gcm;

import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.dado.chatsecurity.Activity.FragmentContacts;
import com.example.dado.chatsecurity.Activity.MainActivity;
import com.example.dado.chatsecurity.Adapter.ChatFragmentListAdapter;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.google.android.gms.games.appcontent.MultiDataBufferRef;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Dado on 03/08/2016.
 */
public class MyGcmPushReceiver extends GcmListenerService {
    //listenere delle notifiche per quando arrivano messaggi dal server di google
    private static final String TAG=MyGcmPushReceiver.class.getSimpleName();
    private NotificationUtils notificationUtils;
    String messaggio,mittente,id_conversation,timestamp,id_mittente,id_messaggio,publicKey,email,urlImage;
    Bundle date;
    final int maxLengthMessage=693;
    User user;
    boolean image;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        super.onMessageReceived(from, data);
        date=data;
        //Log.e(TAG,date.getString("mittente"));
        //Log.e(TAG,date.getString("message"));
        processMessageForUser();


    }

    private void processMessageForUser(){
        new threadRecuperoMessaggio().execute();
    }


    //qua devo differenziare se il messaggio inviato è un immagine oppure
    //un messaggio con solo il testo
    private class threadRecuperoMessaggio extends AsyncTask<Void,Void,Void>{
        Bitmap b;

        @Override
        protected Void doInBackground(Void... params) {
           // Log.d(TAG,date.toString());
            PrivateKey privateKey=MyApplication.getInstance().getPrefManager().getUser().getPrivateKey();

            //messaggio= new String(Base64.decode(date.getString("message").getBytes(),Base64.DEFAULT));
            if(date.getString("flag").equals("100")){

                String urlImage=date.getString("message");

                Bitmap bitmapImage=getBitmapFromURL(EndPoint.BASE_URL+urlImage);
                String[] nomeImmagine=urlImage.split("/");
                saveToInternalImageSendAndReceive(bitmapImage,nomeImmagine[nomeImmagine.length-1]);

                messaggio = nomeImmagine[nomeImmagine.length-1];
                image = true;

            } else{

                image = false;
                if(date.getString("message").length()<693) {
                    messaggio = MyApplication.getInstance().decifraturaMessage(privateKey, date.getString("message"));
                }else {

                    messaggio = date.getString("message");
                    int max = messaggio.length() / maxLengthMessage;


                    ArrayList<String> message = new ArrayList<>();

                    for (int i = 0; i < max; i++) {
                        if ((i + 1) * maxLengthMessage < messaggio.getBytes().length) {
                            String attuale = (messaggio.substring(i * maxLengthMessage, (i + 1) * (maxLengthMessage)));
                            message.add(attuale);
                            //Log.e(TAG,attuale);
                        } else {
                            String attuale = (messaggio.substring(i * maxLengthMessage, messaggio.getBytes().length));
                            message.add(messaggio.substring(i * maxLengthMessage, messaggio.getBytes().length));
                            // Log.e(TAG,attuale);
                        }
                    }

                    StringBuilder messageRicevuto = new StringBuilder("");
                    for (int i = 0; i < message.size(); i++) {
                        messageRicevuto.append(MyApplication.getInstance().decifraturaMessage(privateKey, message.get(i)));
                    }

                    messaggio = messageRicevuto.toString();
               }

            }
            mittente = date.getString("mittente");
            id_messaggio = date.getString("id_messaggio");
            id_mittente = date.getString("id_mittente");
            id_conversation = date.getString("id_conversation");
            timestamp = date.getString("ora");
            publicKey = date.getString("publicKey");
            email = date.getString("email");
            String url_immagine_profilo = date.getString("urlImage");
            String[] image = url_immagine_profilo.split("/");

            urlImage = image[image.length - 1];
            user = MyApplication.getInstance().getDbChat().getUserFromId(id_mittente);
            //aggiorno l'utente
            if (user == null) {
                //devo scaricare la sua  immagine e aggiornare

                user = new User(publicKey, id_mittente, mittente, email, urlImage);
                //sto salvando le immagini essando che utente non esiste
                Log.e(TAG,"urlImage"+user.getUrlImage());
                b = getBitmapFromURL(EndPoint.BASE_URL + "uploadedimages/" + user.getUrlImage());


                MyApplication.getInstance().getDbChat().addContact(user);

                Log.e(TAG,""+user.amici);
                MyApplication.getInstance().getDbChat().updateUserAmici(user);
                FragmentContacts fragmentContacts = new FragmentContacts();
                user.amici=true;
            }else {
                Log.e(TAG, "" + user.amici);
                if (!user.amici) {
                    user.amici = false;
                    MyApplication.getInstance().getDbChat().updateUserAmici(user);
                    FragmentContacts fragmentContacts = new FragmentContacts();
                }
                user.amici=true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Conversazioni conversazione=MyApplication.getInstance().getDbChat().getConversazioniFromUser(user);

            Message message=new Message(id_messaggio,id_conversation,id_mittente,messaggio,timestamp,image);
            if(conversazione==null){
                String path = saveToInternalStorage(b, user.getUrlImage());
                conversazione =new Conversazioni(id_conversation,id_mittente,messaggio,timestamp);
                conversazione.addUnreadCount();
                //Log.e(TAG,"create a conversazione"+conversazione.getConversazioniId());
                User user=MyApplication.getInstance().getDbChat().getUserFromConversazioni(conversazione);
                if(user!=null){
                    user.setAmici(false);
                    MyApplication.getInstance().getDbChat().updateUserAmici(user);
                    user.setAmici(true);

                }
                MyApplication.getInstance().getDbChat().addConversazione(conversazione);
                MyApplication.getInstance().getDbChat().addMessagetoSent(message);
                Intent newMessage=new Intent("newMessage");
                newMessage.putExtra("message",message);
                newMessage.putExtra("conversazione",conversazione);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newMessage);

            }else{
                //Log.d(TAG,"messaggi non letti "+conversazione.getUnreadCount());

                MyApplication.getInstance().getDbChat().addMessagetoSent(message);
                //Log.e(TAG,"update conversazione"+conversazione.getConversazioniId());
                conversazione.addUnreadCount();
                MyApplication.getInstance().getDbChat().updateConversation(conversazione);
                User user=MyApplication.getInstance().getDbChat().getUserFromConversazioni(conversazione);
                if(user!=null){
                    user.setAmici(false);
                    MyApplication.getInstance().getDbChat().updateUserAmici(user);
                    user.setAmici(true);

                }
                Intent newMessage=new Intent("newMessage");
                newMessage.putExtra("message",message);
                newMessage.putExtra("conversazione",conversazione);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newMessage);
            }

            //se app in foreground
            if(!MyApplication.isAppIsInBackground(getApplicationContext())){
                Log.e(TAG,"appInForeground");
                //niente da fare apparte aggiornare le row inserendo il nuov messaggio e il nuovo utente
                //la nuova conversazione
                Toast.makeText(getApplicationContext(),"nuovo messaggio da "+user.NAME,Toast.LENGTH_SHORT).show();
                NotificationUtils notificationUtils=new NotificationUtils();
                notificationUtils.playNotificationSound();
            }else{
                Log.e(TAG,"appInBackground");
                //in questo caso bisogna creare la notofica
                Intent resultIntent=new Intent(getApplicationContext(), MainActivity.class);
                if(!image){
                    showNotificationMessage(getApplicationContext(),user.NAME,user.NAME+" : "+message.getTesto(),message.getOraMessaggio(),resultIntent);
                }else{
                    showNotificationMessageWithBigImage(getApplicationContext(),user.NAME,user.NAME+" : immagine ",message.getOraMessaggio(),resultIntent,message.getTesto());
                }
            }
            Intent intent=new Intent("updateAmici");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


        }

        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                /*
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                */
                Log.e(TAG,connection.getContentLength()+"");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = null;
                try {
                    is = connection.getInputStream ();
                    byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
                    int n;

                    while ( (n = is.read(byteChunk)) > 0 ) {
                        baos.write(byteChunk, 0, n);
                    }
                }
                catch (IOException e) {
                    System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
                    e.printStackTrace ();
                    // Perform any other exception handling that's appropriate.
                }
                finally {
                    if (is != null) { is.close(); }
                }


                //byte [] image=MyApplication.getInstance().decifraturaMessage(privateKey,new String(baos.toByteArray())).getBytes();
                Bitmap myBitmap=BitmapFactory.decodeByteArray(baos.toByteArray(),0,baos.size());


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

    private void showNotificationMessageWithBigImage(Context applicationContext, String title, String message, String timestamp, Intent resultIntent, String image) {
        notificationUtils=new NotificationUtils(applicationContext);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(Integer.valueOf(user.getID_USER()),title,message,timestamp,resultIntent,image);

    }

    //solo il testo
    private void showNotificationMessage(Context applicationContext, String title, String message, String timestamp, Intent resultIntent) {
        notificationUtils=new NotificationUtils(applicationContext);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(Integer.valueOf(user.getID_USER()),title,message,timestamp,resultIntent,null);
    }




}
