package com.example.dado.chatsecurity.BrodcastReceiverLocalizazione;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dado.chatsecurity.Service.DownloadContat;
import com.example.dado.chatsecurity.Service.LocalizationService;

public class MyReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("Intent","RICEVUTO");
        Intent intento = new Intent(context,LocalizationService.class);
        Intent intent1=new Intent(context, DownloadContat.class);
        context.startService(intento);
        context.startService(intent1);


    }
}