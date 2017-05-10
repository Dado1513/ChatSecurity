package com.example.dado.chatsecurity.Application;

import android.util.Base64;
import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

/**
 * Created by Dado on 04/08/2016.
 */
public class PersonaleKey {
    private String publicKey,privateKey;
    private static final String TAG=PersonaleKey.class.getSimpleName();


    public PersonaleKey(){
        generateKeys();
    }

    private void generateKeys(){
        try {

            KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA","BC");
            keyPairGenerator.initialize(4096,new SecureRandom());
            KeyPair keyPair= keyPairGenerator.generateKeyPair();

            PublicKey publicKey=keyPair.getPublic();
            PrivateKey privateKey=keyPair.getPrivate();


            byte [] publicKeysBytes=publicKey.getEncoded();
            byte [] privateKeysBytes=privateKey.getEncoded();

            //Log.d(TAG,"1->"+String.valueOf(publicKey.getEncoded()));
            String publicKeyStringa= new String(Base64.encode(publicKeysBytes,Base64.DEFAULT));
            String privateKeyStringa=new String(Base64.encode(privateKeysBytes,Base64.DEFAULT));
            this.privateKey=privateKeyStringa;
            this.publicKey=publicKeyStringa;

            //ora ho le chiavi memorizzate nelle stringhe per recuperarle
            //basta usare il metodo getPrivateKey o getPublicKey dell'oggetto user
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

    }

    public String getPublicKeyString (){
        return publicKey;
    }

    public String getPrivateKey (){
        return privateKey;
    }





}
