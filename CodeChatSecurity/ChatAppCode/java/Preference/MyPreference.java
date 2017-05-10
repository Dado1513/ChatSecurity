package com.example.dado.chatsecurity.Preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.example.dado.chatsecurity.Gcm.NotificationUtils;
import com.example.dado.chatsecurity.Model.User;

import java.security.PublicKey;

/**
 * Created by Dado on 29/07/2016.
 */
public class MyPreference  {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    public static final String NOME_FILE="preferenceUser";
    public static final String ID="ID_USER";
    public static final String NAME="NAME";
    public static final String TELEFONO="TELEFONO";
    public static final String EMAIL="EMAIL";
    public static final String URL_IMMAGINE_PROFILO="URL_IMMAGINE_PROFILO";
    public static final String CHIAVE_PUBBLICA="CHIAVE_PUBBLICA";
    public static final String CHIAVE_PRIVATA="CHIAVE_PRIVATE";
    public static final String isLocation="isLocation";
    public static final String lastAccess="lastAccess";
    public static final String passwordMD5="passwordmd5";
    private static final String KEY_NOTIFICATION="notifications";


    public MyPreference(Context context){
        this.context=context;
        sharedPreferences=this.context.getSharedPreferences(NOME_FILE,0);
        editor=sharedPreferences.edit();

    }


    public User getUser(){
        if(sharedPreferences!=null) {

            String id_user = sharedPreferences.getString(this.ID, null);
            String name = sharedPreferences.getString(this.NAME, null);
            String EMAIL = sharedPreferences.getString(this.EMAIL, null);
            String URL_IMMAGINE_PROFILO = sharedPreferences.getString(this.URL_IMMAGINE_PROFILO, null);
            String CHIAVE_PUBBLICA = sharedPreferences.getString(this.CHIAVE_PUBBLICA, null);
            String passwordMD5=sharedPreferences.getString(this.passwordMD5,"xiao");
            String CHIAVE_PRIVATA = sharedPreferences.getString(this.CHIAVE_PRIVATA, null);
            String lastAccess=sharedPreferences.getString(this.lastAccess,null);
            boolean isLocation=sharedPreferences.getBoolean("isLocation",false);

            if(lastAccess!=null){
                return new User(CHIAVE_PUBBLICA,CHIAVE_PRIVATA,id_user,name,EMAIL,URL_IMMAGINE_PROFILO,passwordMD5,isLocation,lastAccess);
            }

            if (CHIAVE_PRIVATA == null && CHIAVE_PUBBLICA == null &&id_user!=null && name!=null && EMAIL!=null
                    && URL_IMMAGINE_PROFILO!= null) {
                User user=new User(id_user, name, EMAIL, URL_IMMAGINE_PROFILO);
                user.setPasswordMD5(passwordMD5);
                return user;
            } else if(CHIAVE_PRIVATA!=null && CHIAVE_PRIVATA!=null && id_user!=null && name!=null && EMAIL!=null
                    && URL_IMMAGINE_PROFILO!= null) {
                User user= new User(CHIAVE_PUBBLICA,CHIAVE_PRIVATA,id_user, name, EMAIL,  URL_IMMAGINE_PROFILO,isLocation);
                user.setPasswordMD5(passwordMD5);
                return user;
            } else
                return null;
        }
        return null;

    }

    public void storeUser(User user){
        editor.putString(this.ID,user.ID_USER);
        editor.putString(this.NAME,user.NAME);
        editor.putString(this.EMAIL,user.EMAIL);
        editor.putString(this.URL_IMMAGINE_PROFILO,user.URL_IMMAGINE_PROFILO);
        //devo distingure la memorizzazione della prima volta
        //a quello di quando scarico l'utente
        editor.putString(this.passwordMD5,user.getPasswordMD5());
        editor.putString(this.CHIAVE_PUBBLICA,user.getStringPublicKey());
        editor.putString(this.CHIAVE_PRIVATA,user.getStringPrivateKey());

        if(user.getLastAccess()!=null){
            editor.putString(this.lastAccess,user.getLastAccess());
        }

        editor.commit();
    }

    public void removeLastAccess(){
        editor.remove(lastAccess);
        editor.commit();
    }

    public void setLocation(boolean location){
        editor.putBoolean(isLocation,location);
        editor.commit();
    }
    public void clear(){
        editor.clear();
        editor.commit();
    }




    //aggiunta delle notifiche
    //potrei dividerle per utenti
    //ogni utente ha la sua notifica personale
    //invece che il messaggio


    public void addNotification(String userName,String notification){
        String oldNotifications=getNotifications(userName);
        if(oldNotifications!=null){
            //accodo la nuova notifica
            oldNotifications+="|"+notification;
        } else {
            oldNotifications=notification;
        }
        editor.putString(userName,oldNotifications);
        editor.commit();
    }

    public String getNotifications(String userName){
        return sharedPreferences.getString(userName,null);
    }

    public void clearNotification(String userName){
        editor.remove(userName);
        editor.commit();
    }



}
