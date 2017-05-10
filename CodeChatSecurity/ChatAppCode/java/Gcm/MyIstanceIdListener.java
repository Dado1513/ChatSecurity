package com.example.dado.chatsecurity.Gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Dado on 03/08/2016.
 */
public class MyIstanceIdListener extends InstanceIDListenerService {

    //aggiornamento dell'ID nel caso il vecchio venga compromesso
    private static final String TAG= MyIstanceIdListener.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        //Log.e(TAG,"refresh Token");
        //far partire registergcm
        Intent intent= new Intent(this,GcmIntentService.class);
        intent.putExtra("key","refresh");
        startService(intent);
    }
}
