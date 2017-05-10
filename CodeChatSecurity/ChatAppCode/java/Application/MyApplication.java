package com.example.dado.chatsecurity.Application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Activity.ActivityIniziale;
import com.example.dado.chatsecurity.Model.DbChat;
import com.example.dado.chatsecurity.Preference.MyPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by Dado on 29/07/2016.
 */
public class MyApplication extends Application {


    private static MyApplication myApplication;
    DbChat dbChat;
    private static int paginaDaVisualizzare=0;
    private static final String TAG= MyApplication.class.getSimpleName();
    MyPreference myPreference;


    public void onCreate() {
        super.onCreate();
        myApplication = this;
        dbChat = new DbChat(this);

    }

    public static synchronized MyApplication getInstance() {

        return myApplication;
    }

    public MyPreference getPrefManager() {
        if (myPreference == null) {
            myPreference = new MyPreference(this);
        }
        return myPreference;
    }


    public int getPaginaDaVisualizzare(){
        return paginaDaVisualizzare;
    }

    public void setPaginaDaVisualizzare(int g)
    {
        paginaDaVisualizzare = g;
    }
    public DbChat getDbChat() {
        if (dbChat == null) {
            dbChat = new DbChat(this);
        }
        return dbChat;
    }

    public static boolean haveInternetConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void logout() {
        if(haveInternetConnection())
            new removeGCM().execute();
        else{
            Toast.makeText(getApplicationContext(),"Connessione ad Internet Assente, impossibile effettuare il logout",Toast.LENGTH_LONG).show();
        }
    }


    private class removeGCM extends AsyncTask<Void, Void, Void> {

        String rispostaServer;

        @Override
        protected Void doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL(EndPoint.BASE_URL + "removeGCM.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER())
                        .appendQueryParameter("root", "caputotavellamantovani99")
                        .appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
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

            } catch (SSLHandshakeException e) {
                Toast.makeText(getApplicationContext(), "Server non Verificato", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
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
            try {
                //Log.d(MyApplication.class.getSimpleName(),rispostaServer);
                JSONObject dataObj = new JSONObject(rispostaServer);
                if (!dataObj.getBoolean("errore")) {

                    myPreference.clear();
                    dbChat.removeAll();
                    //cancellare anche il gcm sul server cosi da rendere impossibile la ricezione dei messaggi dopo essere stati sloggati
                    Intent intent = new Intent(getApplicationContext(), ActivityIniziale.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),"Impossibile il logout, riprovare piu tardi",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }




    //funzione per recuperare l'immagine dal path usato
    public Bitmap loadImageFromStorage(String name) {

        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageProfile", Context.MODE_PRIVATE);

            File f=new File(directory.getAbsolutePath(), name);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  Bitmap loadImageFromStorageMessage(String name){
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageChat", Context.MODE_PRIVATE);

            File f=new File(directory.getAbsolutePath(), name);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }


    public String decifraturaMessage(PrivateKey privateKey,String messaggio){
        try {

            StringBuilder messageRicevuto = new StringBuilder("");
            //Cipher cipher=Cipher.getInstance("RSA/CBC","BC");

            Cipher cipher=Cipher.getInstance("RSA","BC");
            cipher.init(Cipher.DECRYPT_MODE,privateKey);

            int maxLengthMessage = 693;
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


            for (int i = 0; i < message.size(); i++) {
                byte [] decifrato=cipher.doFinal(Base64.decode(message.get(i),Base64.DEFAULT));
                messageRicevuto.append(new String(decifrato));
            }
            return messageRicevuto.toString();


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String cifraMessaggio(PublicKey publicKey,String message){

        Cipher cipher = null;
        try {
            //cipher = Cipher.getInstance("RSA/CBC", "BC");

            cipher = Cipher.getInstance("RSA", "BC");
            //Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] criptato = cipher.doFinal(message.getBytes());
            String returnString = Base64.encodeToString(criptato, Base64.DEFAULT);


            return returnString;
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG,"impossibileCifrareIlMessaggioTroppoLungo");
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }


    //codice per verificare se l'app è in foregroun o in background cosi per fa vedere la notofica o meno
    public static boolean isAppIsInBackground(Context context){
        boolean isInBackground=true;
        ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //controllo se l'applicazione e in foreground o in background-> due metodi diversi a seconda della versione istallata
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT_WATCH){
            List<ActivityManager.RunningAppProcessInfo> runningProcess=am.getRunningAppProcesses();
            for(ActivityManager.RunningAppProcessInfo processInfo : runningProcess){
                if(processInfo.importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    for(String activeProcess : processInfo.pkgList){
                        if(activeProcess.equals(context.getPackageName())){
                            isInBackground=false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo=am.getRunningTasks(1);
            ComponentName componentInfo=taskInfo.get(0).topActivity;
            if(componentInfo.getPackageName().equals(context.getPackageName())){
                isInBackground=false;
            }
        }

        return isInBackground;
    }

}