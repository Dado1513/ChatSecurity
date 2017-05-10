package com.example.dado.chatsecurity.Model;

import android.util.Base64;

import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Dado on 29/07/2016.
 */
public  class User implements Serializable,Comparable<User> {

    public String ID_USER, NAME, EMAIL, URL_IMMAGINE_PROFILO,LATITUDINE,LONGITUDINE;
    public  boolean amici=false;
    private String ChiavePubblica, ChiavePrivata,passwordMD5,lastAccess,statoPersonale;
    //distinzione tra utente estraneo e noi
    public boolean SELF = false,isLocation;

    public User(String publicKey, String privateKey, String id_user, String name, String ID_USER, String NAME, String EMAIL, String URL_IMMAGINE_PROFILO) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;
        this.SELF = false;

    }

    public User(String CHIAVE_PUBBLICA, String CHIAVE_PRIVATA, String ID_USER, String NAME, String EMAIL, String URL_IMMAGINE_PROFILO,boolean isLocation) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;
        this.ChiavePubblica = CHIAVE_PUBBLICA;
        this.ChiavePrivata = CHIAVE_PRIVATA;
        this.SELF = true;
        this.isLocation=isLocation;

    }

    public User (int i,String identificativo, String NAME,  String URL_IMMAGINE_PROFILO,String LATITUDINE, String LONGITUDINE)
    {
        this.ID_USER=identificativo;
        this.NAME = NAME;
        this.LATITUDINE = LATITUDINE;
        this.LONGITUDINE = LONGITUDINE;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;

    }

    public User(String ID_USER, String NAME,
                String EMAIL, String URL_IMMAGINE_PROFILO) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;


    }



    public User(String CHIAVE_PUBBLICA,
                String CHIAVE_PRIVATA, String ID_USER, String NAME,
                String EMAIL, String URL_IMMAGINE_PROFILO,String passwordMD5,boolean isLocation,String lastAccess) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;
        this.passwordMD5=passwordMD5;
        this.ChiavePubblica = CHIAVE_PUBBLICA;
        this.ChiavePrivata = CHIAVE_PRIVATA;
        this.SELF = true;
        this.isLocation=isLocation;
        this.lastAccess=lastAccess;

    }

    public User(String CHIAVE_PUBBLICA, String CHIAVE_PRIVATA, String ID_USER, String NAME, String EMAIL, String URL_IMMAGINE_PROFILO,String passwordMD5) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;
        this.ChiavePubblica = CHIAVE_PUBBLICA;
        this.ChiavePrivata = CHIAVE_PRIVATA;
        this.passwordMD5=passwordMD5;
        this.SELF = true;

    }

    public User(String CHIAVE_PUBBLICA, String ID_USER, String NAME, String EMAIL, String URL_IMMAGINE_PROFILO) {
        this.ID_USER = ID_USER;
        this.NAME = NAME;
        this.EMAIL = EMAIL;
        this.URL_IMMAGINE_PROFILO = URL_IMMAGINE_PROFILO;
        this.ChiavePubblica = CHIAVE_PUBBLICA;
        this.SELF = false;

    }

    //cosi ogni volta che mi loggo riscarico le chiavi pubbliche e private dal server e le salvo
    public void setChiavePubblica(String CHIAVE_PUBBLICA) {
        this.ChiavePubblica = CHIAVE_PUBBLICA;
    }

    public void setChiavePrivata(String CHIAVE_PRIVATA) {

        this.ChiavePrivata = CHIAVE_PRIVATA;
    }

    public String getID_USER(){
        return ID_USER;
    }

    public String getPasswordMD5(){
        return passwordMD5;
    }

    //chiavePubblica dell'utente
    public PublicKey getPublicKey() {

        byte[] sigBytes = Base64.decode(ChiavePubblica, Base64.DEFAULT);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(sigBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setStatoPersonale(String statoPersonale){
        this.statoPersonale=statoPersonale;
    }

    public String getStatoPersonale(){
        return this.statoPersonale;
    }

    //riprendo la chiave privata dell'utente
    public PrivateKey getPrivateKey() {
        byte[] sigBytes = Base64.decode(ChiavePrivata, Base64.DEFAULT);
        PKCS8EncodedKeySpec x509KeySpec = new PKCS8EncodedKeySpec(sigBytes);
        KeyFactory keyFact = null;
        try {
            //bouncy castle
            keyFact = KeyFactory.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            return keyFact.generatePrivate(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStringPublicKey(){
        return this.ChiavePubblica;
    }

    public String getStringPrivateKey(){
        return this.ChiavePrivata;
    }

    //metodi statici per recuperare le chiavi nel caso non volessimo istanziare un utente

    public static PublicKey getPublicKey(String publicKey){
        byte[] sigBytes = Base64.decode(publicKey, Base64.DEFAULT);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(sigBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey(String ChiavePrivata) {
        byte[] sigBytes = Base64.decode(ChiavePrivata, Base64.DEFAULT);
        PKCS8EncodedKeySpec x509KeySpec = new PKCS8EncodedKeySpec(sigBytes);
        KeyFactory keyFact = null;
        try {
            keyFact = KeyFactory.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            return keyFact.generatePrivate(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return ID_USER;
    }

    public String getUrlImage(){
        return URL_IMMAGINE_PROFILO;
    }

    @Override
    public int compareTo(User another) {
        if(this.NAME.toLowerCase().compareTo(another.NAME.toLowerCase())>0)
            return 1;
        else
            return -1;
    }

    public boolean getLocation(){
        return this.isLocation;
    }

    public String getLastAccess(){
        return lastAccess;
    }

    public void setAmici(boolean friend){
        this.amici=friend;
    }

    public void setPasswordMD5(String passwordMD5){
        this.passwordMD5=passwordMD5;
    }
}
