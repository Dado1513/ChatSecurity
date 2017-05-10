package com.example.dado.chatsecurity.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Activity.SearchActivity;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dado on 27/08/2016.
 */
public class ContattiAdapterSearch extends RecyclerView.Adapter<ContattiAdapterSearch.ViewHolder>{

    private Context mContext;
    private ArrayList<User> userArrayList;
    private static String today;

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public CircleImageView circleImageView;
        public TextView status;
        public ImageView imageView;
        public CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.nameContatto);
            circleImageView=(CircleImageView)itemView.findViewById(R.id.imgViewListaContatto);
            status=(TextView)itemView.findViewById(R.id.statoPersonale);
            imageView=(ImageView)itemView.findViewById(R.id.plus);

        }
    }

    public ContattiAdapterSearch(Context mContext, ArrayList<User> userArrayList){
        this.mContext=mContext;
        Collections.sort(userArrayList);
        this.userArrayList=userArrayList;
    }

    @Override
    public ContattiAdapterSearch.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).
                inflate(R.layout.contatti_search_layout,parent,false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final ContattiAdapterSearch.ViewHolder holder, int position) {
        final int positionFinal=position;
        final User user= userArrayList.get(position);
        if(user.getID_USER()!= MyApplication.getInstance().getPrefManager().getUser().getID_USER()) {
            holder.name.setText(user.NAME);
            holder.circleImageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(user.getUrlImage()));
            if (userArrayList.get(position).getStatoPersonale() != null && !userArrayList.get(position).getStatoPersonale().equals("null")) {
                holder.status.setText(userArrayList.get(position).getStatoPersonale());
            }
            if(user.amici){
                holder.circleImageView.setBorderColor(Color.GREEN);
                holder.circleImageView.setBorderWidth(3);
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(),R.drawable.ic_close_black_24dp));
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.getInstance().getDbChat().updateUserAmici(userArrayList.get(positionFinal));
                        userArrayList.get(positionFinal).amici=false;
                        ContattiAdapterSearch.this.notifyDataSetChanged();
                    }
                });

            }else{
                holder.circleImageView.setBorderColor(Color.RED);
                holder.circleImageView.setBorderWidth(3);
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(),R.drawable.ic_person_add_black_24dp));
                holder.imageView.setClickable(true);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MyApplication.getInstance(),user.NAME,Toast.LENGTH_LONG).show();
                        MyApplication.getInstance().getDbChat().updateUserAmici(userArrayList.get(positionFinal));
                        userArrayList.get(positionFinal).amici=true;
                        ContattiAdapterSearch.this.notifyDataSetChanged();



                    }
                });

            }

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
        private ContattiAdapterSearch.ClickListener clickListener;

        public RecyclerTouchListener(Context context,final RecyclerView recyclerView,final  ContattiAdapterSearch.ClickListener clickListener ){
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


