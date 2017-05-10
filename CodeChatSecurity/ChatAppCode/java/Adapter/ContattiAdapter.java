package com.example.dado.chatsecurity.Adapter;

/**
 * Created by Dado on 19/08/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.util.Collections.sort;

/**
 * Created by Dado on 29/07/2016.
 */
public class ContattiAdapter extends RecyclerView.Adapter<ContattiAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<User> userArrayList;
    private static String today;

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public CircleImageView circleImageView;
        public TextView status;
        public ViewHolder(View itemView) {
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.nameContatto);
            circleImageView=(CircleImageView)itemView.findViewById(R.id.imgViewListaContatto);
            status=(TextView)itemView.findViewById(R.id.statoPersonale);

        }
    }

    public ContattiAdapter(Context mContext, ArrayList<User> userArrayList){
        this.mContext=mContext;
        Collections.sort(userArrayList);
        this.userArrayList=userArrayList;
    }

    @Override
    public ContattiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).
                inflate(R.layout.contatti_layout,parent,false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ContattiAdapter.ViewHolder holder, int position) {

        User user = userArrayList.get(position);

        if(user.getID_USER()!=MyApplication.getInstance().getPrefManager().getUser().getID_USER()) {
            final String urlImage=user.getUrlImage();
            holder.name.setText(user.NAME);
            holder.circleImageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(user.getUrlImage()));
            if (userArrayList.get(position).getStatoPersonale() != null && !userArrayList.get(position).getStatoPersonale().equals("null")) {
                holder.status.setText(userArrayList.get(position).getStatoPersonale());
            }
            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    // Add the buttons
                    builder.setMessage("Immagine del profilo");
                    ImageView imageView=new ImageView(mContext);
                    imageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(urlImage));
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

    }



    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public interface ClickListener{

        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }


    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private GestureDetector gestureDetector;
        private ContattiAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context,final RecyclerView recyclerView,final  ContattiAdapter.ClickListener clickListener ){
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
    public void clear(){

    }
    public void addAll(ArrayList<User>userArrayList){

    }

}
