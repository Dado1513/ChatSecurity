package com.example.dado.chatsecurity.Model;

/**
 * Created by Dado on 04/08/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.example.dado.chatsecurity.Application.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/** database per la memorizzazione dei nostri messaggi con tutti gli altri utenti
 *  delle loro chiavi pubbliche e dei contatti memorizzati
 */
public class DbChat {

    static public final String DB_NAME = "ChatSecurity";
    static public final int DB_VERSION = 1;

    // TABELLA CONTATTI
    static public final String TABLE_CONTATTI = "contatti";
    static public final String CONTATTI_ID = "id_contatto";     static public final int CONTATTI_ID_COL = 0;
    static public final String CONTATTI_USERNAME = "username";     static public final int CONTATTI_USERNAME_COL = 1;
    static public final String CONTATTI_EMAIL = "email";     static public final int CONTATTI_EMAIL_COL = 4;
    static public final String CONTATTI_PUBLIC_KEY = "public_key";     static public final int CONTATTI_PUBLIC_KEY_COL = 2;
    static public final String CONTATTI_URL_IMMAGINE = "url_immagine";     static public final int CONTATTI_URL_IMMAGINE_COL = 3;
    static public final String IS_AMICI="amici"; static public final int IS_AMICI_COL=5;
    static public final String STATO ="stato";static public final int STATO_COL=6;

    // TABELLA MESSAGGI
    static public final String TABLE_MESSAGGI = "messaggi";
    static public final String MESSAGGI_ID = "id_messaggio";     static public final int MESSAGGI_ID_COL = 0;
    static public final String MESSAGGI_ID_CONVERSAZIONE = "id_conversazione";     static public final int MESSAGI_ID_CONVERSAZIONE_COL = 1;
    static public final String MESSAGGI_ID_USER="id_user"; static public final int MESSAGGI_ID_USER_COL=2;
    static public final String MESSAGGI_TESTO = "testo";     static public final int MESSAGGI_TESTO_COL = 3;
    static public final String MESSAGGI_ORA_MESSAGGIO = "ora_messaggio";     static public final int MESSAGGI_ORA_MESSAGGIO_COL = 4;
    static public final String MESSAGGI_IS_IMAGE = "is_image";     static public final int MESSAGGI_IS_IMAGE_COL = 5;


    //TABELLA CONVERSAZIONE
    static public final String TABLE_CONVERSAZIONI = "conversazioni";
    static public final String CONVERSAZIONI_ID = "id";     static public final int CONVERSAZIONI_ID_COL = 0;
    // utilizzato per sapere a chi è diretto o chi ha inviato il messaggio
    static public final String CONVERSAZIONI_ID_UTENTE = "id_utente";     static public final int CONVERSAZIONI_ID_UTENTE_COL = 1;
    public static final String COUNT_CONVERSAZIONE="count"; static public final int COUNT_CONVERSAZIONE_COL=2;

    //TABELLA POSIZIONE
    static public final String TABLE_POSIZIONI = "posizioni";
    static public final String POSIZIONI_ID = "id_posizione";     static public final int POSIZIONI_ID_COL = 0;
    static public final String POSIZIONI_LATITUDINE = "latitudine";     static public final int POSIZIONI_LATITUDINE_COL = 1;
    static public final String POSIZIONI_LONGITUDINE = "longitudine";     static public final int POSIZIONI_LONGITUDINE_COL = 2;


    static public final String CREATE_TABLE_CONTATTI = "CREATE TABLE "
            + TABLE_CONTATTI +" (" + CONTATTI_ID + " " + "TEXT PRIMARY KEY," +
            CONTATTI_USERNAME + " TEXT NOT NULL ,"+
            CONTATTI_PUBLIC_KEY + " TEXT NOT NULL ," +
            CONTATTI_URL_IMMAGINE + " TEXT NOT NULL ,"+
            CONTATTI_EMAIL + " TEXT NOT NULL ,"+
            IS_AMICI+" TEXT NOT NULL DEFAULT 0," +
            STATO + " TEXT DEFAULT NULL "+");";

    static public final String CREATE_TABLE_MESSAGGI = "CREATE TABLE "
            + TABLE_MESSAGGI +" (" + MESSAGGI_ID + " " + "TEXT PRIMARY KEY," +
            MESSAGGI_ID_CONVERSAZIONE + " TEXT NOT NULL,"+
            MESSAGGI_ID_USER + " INTEGER NOT NULL,"+
            MESSAGGI_TESTO + " TEXT NOT NULL ," +
            MESSAGGI_ORA_MESSAGGIO + " TEXT NOT NULL ," +
            MESSAGGI_IS_IMAGE + "INTEGER DEFAULT 0" +
            ");";

    static public final String CREATE_TABLE_CONVERSAZIONE = "CREATE TABLE "
            + TABLE_CONVERSAZIONI +" (" + CONVERSAZIONI_ID + " " + "TEXT PRIMARY KEY," +
            CONVERSAZIONI_ID_UTENTE + " TEXT UNIQUE,"+
            COUNT_CONVERSAZIONE +" TEXT DEFAULT 0"+
            ");";

    static public final String CREATE_TABLE_POSIZIONI = "CREATE TABLE " + TABLE_POSIZIONI
            +" (" + POSIZIONI_ID + " " + "TEXT PRIMARY KEY," +
            POSIZIONI_LATITUDINE + " TEXT NOT NULL,"+
            POSIZIONI_LONGITUDINE + " TEXT NOT NULL);";

    public static final String DROP_TABLE_CONTATTI =
            "DROP TABLE IF EXISTS " + TABLE_CONTATTI;

    public static final String DROP_TABLE_CONVERSAZIONI=
            "DROP TABLE IF EXISTS "+ TABLE_CONVERSAZIONI;

    public static final String DROP_TABLE_POSIZIONI=
            "DROP TABLE IF EXISTS "+ TABLE_POSIZIONI;

    public static final String DROP_TABLE_MESSAGGI=
            "DROP TABLE IF EXISTS "+ TABLE_MESSAGGI;

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name,  SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_TABLE_CONTATTI);
            db.execSQL(CREATE_TABLE_CONVERSAZIONE);
            db.execSQL(CREATE_TABLE_MESSAGGI);
            db.execSQL(CREATE_TABLE_POSIZIONI);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_CONTATTI);
            db.execSQL(DROP_TABLE_CONVERSAZIONI);
            db.execSQL(DROP_TABLE_MESSAGGI);
            db.execSQL(DROP_TABLE_POSIZIONI);
            onCreate(db);
        }

    }

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DbChat(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);

    }

    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    //scrittura db
    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    //chiusura db
    private void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    public void insertAllContatti(ArrayList<User> users){
        this.openWriteableDB();
        for(User user: users){

            ContentValues values = new ContentValues();
            long retvalue = 0;
            values.put(CONTATTI_ID, user.getID_USER());
            values.put(CONTATTI_USERNAME, user.NAME);
            values.put(CONTATTI_PUBLIC_KEY,user.getStringPublicKey());
            values.put(CONTATTI_URL_IMMAGINE,user.getUrlImage());
            values.put(CONTATTI_EMAIL,user.EMAIL);
            values.put(IS_AMICI,0);
            values.put(STATO,user.getStatoPersonale());
            retvalue = db.insert(TABLE_CONTATTI, null, values);
            db.insertWithOnConflict(TABLE_CONTATTI,null,values,CONFLICT_REPLACE);
        }
        this.closeDB();
    }


    public void addContact(User user){
        this.openWriteableDB();

        ContentValues values = new ContentValues();
        long retvalue = 0;
        values.put(CONTATTI_ID, user.getID_USER());
        values.put(CONTATTI_USERNAME, user.NAME);
        values.put(CONTATTI_PUBLIC_KEY,user.getStringPublicKey());
        values.put(CONTATTI_URL_IMMAGINE,user.getUrlImage());
        values.put(CONTATTI_EMAIL,user.EMAIL);
        values.put(IS_AMICI,0);
        values.put(STATO,user.getStatoPersonale());
        retvalue = db.insert(TABLE_CONTATTI, null, values);
        db.insert(TABLE_CONTATTI,null,values);
        this.closeDB();
    }



    public void addAllConversation(ArrayList<Conversazioni> conversazioni){
        openWriteableDB();
        for(Conversazioni conversazione: conversazioni){
            db.execSQL("INSERT INTO "+TABLE_CONVERSAZIONI + " " +
                    "VALUES('"+conversazione.getConversazioniId()+"','"+conversazione.getIdUtente()+"','"+conversazione.getUnreadCount()+"')");
        }
        closeDB();
    }

    public void addConversazione(Conversazioni conversazione){
        openWriteableDB();
        db.execSQL("INSERT INTO "+TABLE_CONVERSAZIONI + " " +
                "VALUES('"+conversazione.getConversazioniId()+"','"+conversazione.getIdUtente()+"','"+conversazione.getUnreadCount()+"')");

        Log.e("DB","addConversation"+conversazione.getConversazioniId());

        closeDB();
    }

    public void updateConversation(Conversazioni conversazione){
        openWriteableDB();

        db.execSQL("UPDATE "+TABLE_CONVERSAZIONI+" SET "+COUNT_CONVERSAZIONE+" = '"+conversazione.getUnreadCount()+"' WHERE "+CONVERSAZIONI_ID+" = '"+conversazione.getConversazioniId()+"'");
        Log.e("DB","updateConversation-->"+ conversazione.getUnreadCount());
        closeDB();
    }


    public void addMessagetoSent(Message messaggio){
        openWriteableDB();
        if(messaggio.getIsImage()) {
            db.execSQL("INSERT INTO " + TABLE_MESSAGGI + " " +
                    "VALUES('" + messaggio.getMessageId() + "','" + messaggio.getIdConversaione() + "','" + messaggio.getIdUser() + "','" + messaggio.getTesto() + "','" + messaggio.getOraMessaggio() + "','1')");

        }else{

            ContentValues values = new ContentValues();
            long retvalue = 0;
            values.put(MESSAGGI_ID, messaggio.getMessageId());
            values.put(MESSAGGI_ID_CONVERSAZIONE, messaggio.getIdConversaione());
            values.put(MESSAGGI_ID_USER,messaggio.getIdUser());
            values.put(MESSAGGI_TESTO,messaggio.getTesto());
            values.put(MESSAGGI_ORA_MESSAGGIO,messaggio.getOraMessaggio());
            retvalue = db.insert(TABLE_MESSAGGI, null, values);
        }
        closeDB();
    }



    public void insertConversation(Conversazioni conversazione){
        openWriteableDB();
        db.execSQL("INSERT INTO "+TABLE_CONVERSAZIONI + " " +
                "VALUES('"+conversazione.getConversazioniId()+"','"+conversazione.getIdUtente()+"','"+conversazione.getUnreadCount()+"')");

        //Log.d("DB","inser conversazione"+ conversazione.getUnreadCount());
        closeDB();
    }
    public void addAllMessage(ArrayList<Message>messages){
        openWriteableDB();
        //bisogna decifrare i messaggi qua

        for(Message messaggio: messages){
            if(messaggio.getIsImage()) {
                messaggio.setMessage_text(messaggio.getTesto());
            }else{
                messaggio.setMessage_text(MyApplication.getInstance().decifraturaMessage(MyApplication.getInstance().getPrefManager().getUser().getPrivateKey(),messaggio.getTesto()));
                messaggio.setMessage_text(messaggio.getTesto().replaceAll("'","\'"));
            }
            if(messaggio.getIsImage()) {
                db.execSQL("INSERT INTO " + TABLE_MESSAGGI + " " +
                        "VALUES('" + messaggio.getMessageId() + "','" + messaggio.getIdConversaione() + "','" + messaggio.getIdUser() + "','" + messaggio.getTesto() + "','" + messaggio.getOraMessaggio() + "','1')");
            }else{

                ContentValues values = new ContentValues();
                long retvalue = 0;
                values.put(MESSAGGI_ID, messaggio.getMessageId());
                values.put(MESSAGGI_ID_CONVERSAZIONE, messaggio.getIdConversaione());
                values.put(MESSAGGI_ID_USER,messaggio.getIdUser());
                values.put(MESSAGGI_TESTO,messaggio.getTesto());
                values.put(MESSAGGI_ORA_MESSAGGIO,messaggio.getOraMessaggio());
                retvalue = db.insert(TABLE_MESSAGGI, null, values);
              /*
                db.execSQL("INSERT INTO " + TABLE_MESSAGGI + " " +
                        "VALUES('" + messaggio.getMessageId() + "','" + messaggio.getIdConversaione() + "','" + messaggio.getIdUser() + "','" + messaggio.getTesto() + "','" + messaggio.getOraMessaggio() + "','0')");
                */
            }
        }
        closeDB();
    }

    public ArrayList<User> getAmici(){
        ArrayList<User> users=new ArrayList<>();
        openReadableDB();
        String where =IS_AMICI+" = '1'";
        Cursor cursor= db.query(TABLE_CONTATTI,null,where,null,null,null,CONTATTI_USERNAME+" ASC");
        while (cursor.moveToNext()){
            users.add(getUserFromCurson(cursor));
        }
        if (cursor != null) {
            cursor.close();
        }
        closeDB();
        return users;
    }

    public ArrayList<User> getAllAmici(){
        ArrayList<User> users=new ArrayList<>();
        openReadableDB();
        String where =CONTATTI_ID +" <> "+MyApplication.getInstance().getPrefManager().getUser().getID_USER();
        Cursor cursor= db.query(TABLE_CONTATTI,null,where,null,null,null,CONTATTI_USERNAME+" ASC");
        while (cursor.moveToNext()){
            users.add(getUserFromCurson(cursor));
        }
        if (cursor != null) {
            cursor.close();
        }
        closeDB();
        return users;
    }

    public static User getUserFromCurson(Cursor cursor){
        if(cursor==null || cursor.getCount()==0){
            return null;
        }else{
            try{
                User user=new User(cursor.getString(CONTATTI_PUBLIC_KEY_COL),
                        cursor.getString(CONTATTI_ID_COL),
                        cursor.getString(CONTATTI_USERNAME_COL),
                        cursor.getString(CONTATTI_EMAIL_COL),
                        cursor.getString(CONTATTI_URL_IMMAGINE_COL)
                        );
                user.setStatoPersonale(cursor.getString(STATO_COL));
                if(cursor.getString(IS_AMICI_COL).equals("0"))
                    user.setAmici(false);
                else
                    user.setAmici(true);
                return user;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public  User getUserFromId(String id){
        String where=CONTATTI_ID+" = '"+id+"'";
        openReadableDB();
        User user;
        Cursor cursor=db.query(TABLE_CONTATTI,null,where,null,null,null,null);
        cursor.moveToNext();
        closeDB();
        user=getUserFromCurson(cursor);
        //Log.d("DB",user.getID_USER());
        return user;
    }

    public Conversazioni getConversazioneFromId(String id){
        String where=CONVERSAZIONI_ID+" = '"+id+"'";
        openReadableDB();
        Conversazioni conversazioni;
        Cursor cursor=db.query(TABLE_CONVERSAZIONI,null,where,null,null,null,null);
        cursor.moveToNext();
        closeDB();
        conversazioni=getConversazioneFromCursor(cursor);
        //Log.d("DB",user.getID_USER());
        return conversazioni;
    }


    public void removeAll(){
        openWriteableDB();
        db.execSQL(DROP_TABLE_CONTATTI);
        db.execSQL(DROP_TABLE_CONVERSAZIONI);
        db.execSQL(DROP_TABLE_MESSAGGI);
        db.execSQL(DROP_TABLE_POSIZIONI);
        dbHelper.onCreate(db);

    }



    public ArrayList<Conversazioni> getAllConversazioni(){
        ArrayList<Conversazioni> conversazionis=new ArrayList<>();
        openReadableDB();
        int index=0;
        Cursor cursor= db.query(TABLE_CONVERSAZIONI,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            Conversazioni conversazioni=getConversazioneFromCursor(cursor);
            if(conversazioni!=null) {
                //Log.d("DB", conversazioni.getIdUtente() + conversazioni.getConversazioniId());
                Message message = getLastMessageFromConversazione(conversazioni);
                if(message!=null) {
                    //Log.d("DB", String.valueOf(message.equals(null)));
                    conversazionis.add(new Conversazioni(conversazioni.getConversazioniId(), conversazioni.getIdUtente(), message.getTesto(), message.getOraMessaggio(),conversazioni.getUnreadCount()));

                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        closeDB();

        return conversazionis;

    }


    public int deleteConversazione(Conversazioni conversazioni){
        openWriteableDB();
        String where=CONVERSAZIONI_ID+" = "+conversazioni.getConversazioniId();
        int delete=db.delete(TABLE_CONVERSAZIONI,where,null);
        String whereMessage=MESSAGGI_ID_CONVERSAZIONE+" = "+ conversazioni.getConversazioniId();
        int deleteMessage=db.delete(TABLE_MESSAGGI,whereMessage,null);
        closeDB();
        return deleteMessage;
    }

    public Message getLastMessageFromConversazione(Conversazioni conversazione){
        Message message=null;
        openReadableDB();
        String where =MESSAGGI_ID_CONVERSAZIONE+" = "+conversazione.getConversazioniId();

        Cursor cursor=db.query(TABLE_MESSAGGI,null,where,null,null,null,MESSAGGI_ORA_MESSAGGIO+" ASC");
        while(cursor.moveToNext()){
            message=getMessageFromCursor(cursor);
        }
        User user=getUserFromConversazioni(conversazione);
        if(message!=null) {
            if (message.getIdUser().equals(user.getID_USER())) {
                if(message.getIsImage()){
                    message.setMessage_text(user.NAME+": immagine");
                }
                else if (message.getTesto().length() > 50) {
                    message.setMessage_text(user.NAME + ":  " + message.getTesto().substring(0, 48) + "...");
                } else {
                    message.setMessage_text(user.NAME + " :  " + message.getTesto());
                }
            } else {
                if(message.getIsImage()){
                  message.setMessage_text("immagine");
                } else if (message.getTesto().length() > 50) {
                    message.setMessage_text(message.getTesto().substring(0, 48) + "...");
                } else {

                    message.setMessage_text(message.getTesto());
                }
            }
        }

        closeDB();
        return message;
    }

    public User getUserFromConversazioni(Conversazioni conversazioni){
        openReadableDB();
        String where=CONTATTI_ID+" = "+conversazioni.getIdUtente();
        Cursor cursor=db.query(TABLE_CONTATTI,null,where,null,null,null,null);
        cursor.moveToNext();
        User user=getUserFromCurson(cursor);
        closeDB();
        return user;
    }

    public Conversazioni getConversazioniFromUser(User user){
        openReadableDB();
        String where=CONVERSAZIONI_ID_UTENTE+" = "+user.getID_USER();
        Cursor cursor=db.query(TABLE_CONVERSAZIONI,null,where,null,null,null,null);
        cursor.moveToNext();
        Conversazioni conversazioni=getConversazioneFromCursor(cursor);
        closeDB();
        return conversazioni;
    }

    private Conversazioni getConversazioneFromCursor(Cursor cursor){
        if(cursor==null || cursor.getCount()==0){
            return null;
        }else{
            try{
                Conversazioni conversazioni=new Conversazioni(cursor.getString(CONVERSAZIONI_ID_COL),
                        cursor.getString(CONVERSAZIONI_ID_UTENTE_COL),
                        cursor.getString(COUNT_CONVERSAZIONE_COL)

                );
                return conversazioni;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }


    private Message getMessageFromCursor(Cursor cursor){
        if(cursor==null || cursor.getCount()==0){
            return null;
        }else{
            try{
                Message message;
                if(cursor.getString(MESSAGGI_IS_IMAGE_COL).equals("0")) {
                    message = new Message(cursor.getString(MESSAGGI_ID_COL),
                            cursor.getString(MESSAGI_ID_CONVERSAZIONE_COL),
                            cursor.getString(MESSAGGI_ID_USER_COL),
                            cursor.getString(MESSAGGI_TESTO_COL),
                            cursor.getString(MESSAGGI_ORA_MESSAGGIO_COL),
                            false
                    );
                }else{
                    message= new Message(cursor.getString(MESSAGGI_ID_COL),
                            cursor.getString(MESSAGI_ID_CONVERSAZIONE_COL),
                            cursor.getString(MESSAGGI_ID_USER_COL),
                            cursor.getString(MESSAGGI_TESTO_COL),
                            cursor.getString(MESSAGGI_ORA_MESSAGGIO_COL),
                            true
                    );
                }
                return message;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public ArrayList<Message> getMessageFromConversation(Conversazioni conversazioni){

        //se user_id=> per decifrare i messaggi usare sempre la propria chiave privata
        //quando li invio ne invio due uno per la table Message cifrati con la chiave pubblica del destinatario
        //l'altro alla tabella messageForMe cifrato con la mia chiave pubblica
        ArrayList<Message> messages=new ArrayList<>();
        openReadableDB();
        String where=MESSAGGI_ID_CONVERSAZIONE+" = "+conversazioni.getConversazioniId();
        Cursor cursor= db.query(TABLE_MESSAGGI,null,where,null,null,null,null);
        while (cursor.moveToNext()){
            messages.add(getMessageFromCursor(cursor));
        }
        if (cursor != null) {
            cursor.close();
        }
        closeDB();

        return messages;

    }
    public ArrayList<User> getAllandSetAmici(String test){


        ArrayList<User> users=new ArrayList<>();
        openReadableDB();
        String where =CONTATTI_ID +" <> "+MyApplication.getInstance().getPrefManager().getUser().getID_USER()+ " AND "+ CONTATTI_USERNAME+" LIKE '"+test+"%' OR "+CONTATTI_USERNAME+" LIKE '%"+test+"'";
        Cursor cursor= db.query(TABLE_CONTATTI,null,where,null,null,null,CONTATTI_USERNAME+" ASC");
        while (cursor.moveToNext()){
            users.add(getUserFromCurson(cursor,getAmiciFromCurson(cursor)));
        }
        if (cursor != null) {
            cursor.close();
        }
        closeDB();
        return users;
    }

    public boolean getAmiciFromCurson(Cursor cursor){
        if(cursor==null || cursor.getCount()==0){
            return false;
        }else{
            try{
                if(cursor.getString(IS_AMICI_COL).equals("0"))
                    return false;
                else
                    return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
    }

    public static User getUserFromCurson(Cursor cursor,boolean friend){
        if(cursor==null || cursor.getCount()==0){
            return null;
        }else{
            try{
                User user=new User(cursor.getString(CONTATTI_PUBLIC_KEY_COL),
                        cursor.getString(CONTATTI_ID_COL),
                        cursor.getString(CONTATTI_USERNAME_COL),
                        cursor.getString(CONTATTI_EMAIL_COL),
                        cursor.getString(CONTATTI_URL_IMMAGINE_COL)
                );
                user.setStatoPersonale(cursor.getString(STATO_COL));
                user.setAmici(friend);
                return user;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public  void updateUserAmici(User user){
        openWriteableDB();
        Log.e("DB","updateAMici"+user.NAME+" ->"+ user.amici);

        //sto passando un amico lo devo aggiornare
        if(!user.amici)
            db.execSQL("UPDATE "+TABLE_CONTATTI+ " SET "+IS_AMICI+" = '1'"+ " WHERE "+ CONTATTI_ID+" = '"+user.getID_USER()+"'" );
        else
            db.execSQL("UPDATE "+TABLE_CONTATTI+ " SET "+IS_AMICI+" = '0'"+ " WHERE "+ CONTATTI_ID+" = '"+user.getID_USER()+"'" );
        closeDB();
    }

    public void updateStatePersonale(String text){
        openWriteableDB();
        ContentValues contentValues=new ContentValues();
        contentValues.put(STATO,text);
        db.update(TABLE_CONTATTI,contentValues,CONTATTI_ID+" = '"+MyApplication.getInstance().getPrefManager().getUser()+"'",null);
        closeDB();

    }

    public void dropTableContatti(){
        openWriteableDB();
        db.execSQL(DROP_TABLE_CONTATTI);
        db.execSQL(CREATE_TABLE_CONTATTI);
        closeDB();
    }


}
