package com.example.dado.chatsecurity.Gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Dado on 03/08/2016.
 */
public class NotificationUtils {

    //gestore delle notifiche
    private static String TAG=NotificationUtils.class.getSimpleName();
    private Context mContext;

    public NotificationUtils(){

    }

    public NotificationUtils(Context context){
        this.mContext=context;
    }


    public void showNotificationMessage(int idUtente,String mittente, String message, String timeStamp, Intent intent, String nomeImage) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        //icona della notifica
        final int icon = R.mipmap.ic_launcher;


        //vengono chiuse le altre activity se questa attiviti è in fase di running
        //activity intent non viene lanciato se è gia in cima allo stack -> FLAG_ACTIVITY_SINGLE_TOP
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //serve per dare il permesso ad un applicazione per seguire il pezzo di codice della nostra app
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //notifica
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

        //uri di dove si trova la risorsa per la notifica
        final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + mContext.getPackageName() + "/raw/youhavenewmessage");

        if( nomeImage==null) {
            showSmallNotification(idUtente,mBuilder, icon, mittente, message, timeStamp, resultPendingIntent, alarmSound);
            playNotificationSound();
        }else{
            Bitmap bitmap=MyApplication.getInstance().loadImageFromStorageMessage(nomeImage);
            if(bitmap!=null){
                showBigNotification(idUtente,bitmap,mBuilder, icon, mittente, message, timeStamp, resultPendingIntent, alarmSound);

            }else{
                showSmallNotification(idUtente,mBuilder, icon, mittente, message, timeStamp, resultPendingIntent, alarmSound);

            }
            playNotificationSound();
        }
    }


    private void showSmallNotification(int id_utente,NotificationCompat.Builder mBuilder,int icon,String utente,String message,String timestamp, PendingIntent pendingIntent, Uri alarmSound){

        //NotificationCompat -> layout delle notifiche
        //InboxStyle include una lista di piu di 5 stringhe
        NotificationCompat.InboxStyle inboxStyle= new NotificationCompat.InboxStyle();
        if(Config.appenNotificationMessages){
            //memoriza la notifica nei sharedpref
            MyApplication.getInstance().getPrefManager().addNotification(utente,message);
            String oldNotification=MyApplication.getInstance().getPrefManager().getNotifications(utente);
            //splitto le notifiche siccome sono separate dal \
            //ottengo i messaggi relativi ai singoli utenti
            List<String> messages= Arrays.asList(oldNotification.split("\\|"));
            //parto dalla prima cosi da far vedere la piu nuov ain cima
            if(messages.size()<=4) {
                for (int i = messages.size() - 1; i >= 0; i--) {
                    inboxStyle.addLine(messages.get(i));
                }
            }else{
                inboxStyle.setSummaryText(messages.size()+" messaggi ricevuti");
            }
        } else {
            inboxStyle.addLine(message);
        }

        //costruisco l'oggetto notitifica
        Notification notification;
        notification=mBuilder.setSmallIcon(icon).setTicker(utente).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle("Chat Security")
                .setContentIntent(pendingIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setWhen(getTimeMilliSec(timestamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager =(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id_utente,notification);
    }



    //notification con l'immagine ricevuta
    private void showBigNotification(int id_utente,Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String user, String message, String timeStamp, PendingIntent resultPendingIntent, Uri alarmSound) {

        //siccome include una immagine grande
        MyApplication.getInstance().getPrefManager().addNotification(user,message);
        String oldNotification=MyApplication.getInstance().getPrefManager().getNotifications(user);
        //splitto le notifiche siccome sono separate dal \
        //ottengo i messaggi relativi ai singoli utenti
        List<String> messages= Arrays.asList(oldNotification.split("\\|"));

        if(messages.size()>1) {
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            NotificationCompat.InboxStyle inboxStyle= new NotificationCompat.InboxStyle();

            if (Config.appenNotificationMessages) {
                //memoriza la notifica nei sharedpref
                //parto dalla prima cosi da far vedere la piu nuov ain cima
                if (messages.size() <= 4) {
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        inboxStyle.addLine(messages.get(i));
                    }
                } else {
                    inboxStyle.setSummaryText(messages.size() + " messaggi ricevuti");
                }
            } else {
                inboxStyle.addLine(message);
            }


            Notification notification;
            notification = mBuilder.setSmallIcon(icon).setTicker(user).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle("Chat Security")
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setStyle(inboxStyle)
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setContentText(message)
                    .build();
            NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.notify(id_utente, notification);

        }else{

            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle(user);
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
            bigPictureStyle.bigPicture(bitmap);
            Notification notification;
            notification=mBuilder.setSmallIcon(icon).setTicker(user).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle("Chat Security")
                    .setSound(alarmSound)
                    .setStyle(bigPictureStyle)
                    .setContentIntent(resultPendingIntent)
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),icon))
                    .setContentText(message)
                    .build();
            NotificationManager notifyManager= (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.notify(id_utente,notification);


        }
    }

    public void playNotificationSound(){
        try {
            Uri alarmSound= Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+ MyApplication.getInstance().getApplicationContext().getPackageName()+"/raw/youhavenewmessage");
            Ringtone r= RingtoneManager.getRingtone(MyApplication.getInstance(),alarmSound);
            r.play();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //cancellazione delle notifiche
    public static void clearNotifications(){
        NotificationManager notificationManagaer=(NotificationManager)MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManagaer.cancelAll();

    }

    public static long getTimeMilliSec(String timeStamp){
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date date = format.parse(timeStamp);
            return date.getTime();
        }catch (Exception e ){

        }
        return 0;
    }


}
