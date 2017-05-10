                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      package com.example.dado.chatsecurity.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Dado on 31/07/2016.
 */
public class ChatRoomMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static String TAG=ChatRoomMessageAdapter.class.getSimpleName();
    private String userId;
    private int SELF=100;
    private int SELFIMAGE=200;
    private int OTHERIMAGE=-100;
    private static String today;
    private Context context;
    private ArrayList<Message> messageArrayList;


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView message,timestamp;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.messageChatRoomImage);
            message=(TextView)itemView.findViewById(R.id.messageChatRoom);
            timestamp=(TextView)itemView.findViewById(R.id.timestampChatRoom);
        }
    }


    public ChatRoomMessageAdapter(Context context,ArrayList<Message> messageArrayList,String userId){
        this.context=context;
        this.messageArrayList=messageArrayList;
        this.userId=userId;
        Calendar calendar= Calendar.getInstance();
        today=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        //controllare anche se è una immagine
        if(viewType==SELFIMAGE){
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.chat_item_self_image, parent, false);
        }
        else if(viewType==OTHERIMAGE){
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.chat_item_other_image, parent, false);
        }
        else if(viewType==SELF){
            itemView= LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.chat_item_self,parent,false);
        } else {
            itemView=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_other,parent,false);
        }

        return new ViewHolder(itemView);
    }


    @Override
    public int getItemViewType(int position) {
        Message message= messageArrayList.get(position);
        if(message.getIdUser().equals(userId)){
           if(message.getIsImage())
                return SELFIMAGE;
            return SELF;
        }
        if(message.getIsImage())
            return OTHERIMAGE;
        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Message message=messageArrayList.get(position);
        String timeStamp= getTimeStamp(message.getOraMessaggio());

        if(message.getIdUser()!=null){
            User user= MyApplication.getInstance().getDbChat().getUserFromId(message.getIdUser());
            if(user!=null)
                timeStamp=user.NAME+", "+timeStamp;
            else{
                timeStamp=MyApplication.getInstance().getPrefManager().getUser().NAME+", "+timeStamp;
            }

        }
        if(message.getIsImage()){
            //byte [] decodeBytes=message.getTesto().getBytes();
            final String[] messaggio=message.getTesto().split("/");
            final Bitmap bitmap= MyApplication.getInstance().loadImageFromStorageMessage(messaggio[messaggio.length-1]);
            ((ViewHolder)holder).imageView.setImageBitmap(bitmap);
            ((ViewHolder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    // Add the buttons
                    builder.setMessage("Salvare l'immagine?");
                    ImageView imageView=new ImageView(context);
                    imageView.setImageBitmap(bitmap);
                    builder.setView(imageView);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String root = Environment.getExternalStorageDirectory().toString();
                            saveImage(bitmap,messaggio[messaggio.length-1]);
                            Toast.makeText(MyApplication.getInstance(),"SALVO IMMAGINE in "+root+"/imagechatSecurity/"+messaggio[messaggio.length-1],Toast.LENGTH_SHORT).show();

                        }
                    });
                    builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            ((ViewHolder) holder).timestamp.setText(timeStamp);
        }else {
            ((ViewHolder)holder).message.setText(message.getTesto());
            ((ViewHolder) holder).timestamp.setText(timeStamp);
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }
    public static String getTimeStamp(String dateStr){
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp="";
        today=today.length()<2 ? "0" +today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }


    private void saveImage(Bitmap finalBitmap,String messaggio) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/imageChatSecurity");
        myDir.mkdirs();
        File file = new File(myDir, messaggio);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}