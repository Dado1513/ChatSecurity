package com.example.dado.chatsecurity.Activity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dado on 29/08/16.
 */
//activity che gestira il cambio password
public class ActivityChangePassword extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        FragmentChangePassword1 changePassword1=new FragmentChangePassword1();
        FragmentManager manager=getSupportFragmentManager();//create an instance of fragment manager
        FragmentTransaction transaction=manager.beginTransaction();//create an instance of Fragment-transaction
        transaction.replace(R.id.changePasswordActivity ,changePassword1, "Frag_Top_tag");
        transaction.commit();

    }



}
