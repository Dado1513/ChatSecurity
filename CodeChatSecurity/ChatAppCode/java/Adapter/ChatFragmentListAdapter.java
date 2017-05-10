package com.example.dado.chatsecurity.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dado on 21/08/2016.
 */
public class ChatFragmentListAdapter extends RecyclerView.Adapter<ChatFragmentListAdapter.ViewHolder> {


    private Context mContext;
    private ArrayList<Conversazioni>conversazioniArrayList;
    private ArrayList<User> userArrayList;
    private static String today;

    public class ViewHolder extends RecyclerView.ViewHolder{

        //elementi della singola riga
        //immagine,nomeutente,oraultimomessagio,messagginonletti

        public CircleImageView circleImageView;
        public TextView nome,timestamp,lastMessage,count;

        public ViewHolder(View itemView) {
            super(itemView);
            nome=(TextView)itemView.findViewById(R.id.nameUserChat);
            timestamp=(TextView)itemView.findViewById(R.id.timestampChat);
            lastMessage=(TextView)itemView.findViewById(R.id.messageChat);
            count=(TextView)itemView.findViewById(R.id.count);
            circleImageView=(CircleImageView)itemView.findViewById(R.id.imageViewChat);
        }
    }
    public ChatFragmentListAdapter (Context mContext,ArrayList<Conversazioni> conversazionis){
        this.mContext=mContext;
        Collections.sort(conversazionis);
        this.conversazioniArrayList=conversazionis;
        userArrayList= MyApplication.getInstance().getDbChat().getAllAmici();
        Calendar calendar =Calendar.getInstance();
        today=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Conversazioni conversazioni=conversazioniArrayList.get(position);
        final User user=MyApplication.getInstance().getDbChat().getUserFromId(conversazioni.getIdUtente());
        String lastMessage=MyApplication.getInstance().getDbChat().getLastMessageFromConversazione(conversazioni).getTesto();
        holder.lastMessage.setText(lastMessage.trim());
        holder.nome.setText(user.NAME);
        if(conversazioni.getUnreadCount()>0){
            holder.count.setText(String.valueOf(conversazioni.getUnreadCount()));
            holder.count.setVisibility(View.VISIBLE);
        }else{
            holder.count.setVisibility(View.INVISIBLE);
        }

        holder.circleImageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(user.getUrlImage()));
        holder.timestamp.setText(getTimeStamp(conversazioni.getTimestamp()));
        holder.circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                // Add the buttons
                builder.setMessage("Immagine del profilo");
                ImageView imageView=new ImageView(mContext);
                imageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(user.getUrlImage()));
                builder.setView(imageView);
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return conversazioniArrayList.size();
    }

    public static  String getTimeStamp(String dateStr){
        SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp="";
        today=today.length()<2 ? "0"+today :today;

        try {
            //Controllo se il giorno dell'arrivo del messaggio è lo stesso di oggi
            Date date= format.parse(dateStr);
            SimpleDateFormat todayFormat= new SimpleDateFormat("dd");
            String dateToday= todayFormat.format(date);
            format=dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1= format.format(date);
            timestamp=date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }


    public interface ClickListener{

        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }


    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private GestureDetector gestureDetector;
        private ChatFragmentListAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context,final RecyclerView recyclerView,final  ChatFragmentListAdapter.ClickListener clickListener ){
            this.clickListener=clickListener;
            gestureDetector=new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            View child= recyclerView.findChildViewUnder(e.getX(),e.getY());
                            if(child!=null &&clickListener!=null){
                                clickListener.onLongClick(child,recyclerView.getChildPosition(child));
                            }
                        }
                    });

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child= rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clickListener!=null &&gestureDetector.onTouchEvent(e)){
                clickListener.onClick(child,rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
