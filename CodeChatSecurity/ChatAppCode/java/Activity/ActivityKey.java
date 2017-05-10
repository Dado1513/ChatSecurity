package com.example.dado.chatsecurity.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;

/**
 * Created by Dado on 26/08/2016.
 */
public class ActivityKey extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);
        TextView textView=(TextView)findViewById(R.id.keyPublicShow);

        textView.setText(MyApplication.getInstance().getPrefManager().getUser().getPublicKey().toString());

    }
}
