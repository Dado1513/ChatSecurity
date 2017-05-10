package com.example.dado.chatsecurity.Service;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.dado.chatsecurity.Activity.ActivityLogin;
import com.example.dado.chatsecurity.Activity.MainActivity;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by dado on 29/09/16.
 */

public class DownloadContat extends Service {
    ArrayList<User> arrayListUser;
    ArrayList<User> attuali;
    ArrayList<Conversazioni> arrayListConversazioni;
    ArrayList<Message> arrayListMessage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(MyApplication.getInstance().getPrefManager().getUser()!=null) {
            new ScaricaContatti().execute();


        }
        return super.onStartCommand(intent, flags, startId);
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
                HttpsURLConnection urlConnection=(HttpsURLConnection)url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.setRequestMethod("POST");

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
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
                        if(MyApplication.getInstance().getDbChat().getUserFromId(id_user)==null)
                            arrayListUser.add(new User(public_key, id_user, username, email, image[image.length-1]));

                        arrayListUser.get(arrayListUser.size()-1).setStatoPersonale(jsonUser.getString("statoPersonale"));

                    }


                    MyApplication.getInstance().getDbChat().insertAllContatti(arrayListUser);

                    //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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
                    for(Message message: arrayListMessage){
                        // Log.d(TAG,message.getTesto()+message.getIdConversaione());
                    }

                    new downloadAndSaveImageMessage().execute();
                    new downloadAndSaveImage().execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
        }

        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
    }


}
