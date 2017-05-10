package com.example.dado.chatsecurity.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ChatFragmentListAdapter;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.SimpleDividerItemDecoration;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Dado on 25/07/2016.
 */
public class FragmentChat extends Fragment {

    ArrayList<Conversazioni>conversazioni;
    ChatFragmentListAdapter adapter;
    RecyclerView recyclerView;
    private String TAG=FragmentChat.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;
    static final int numeroPagina = 0;

    public FragmentChat(){

        conversazioni=MyApplication.getInstance().getDbChat().getAllConversazioni();
        Collections.sort(conversazioni);
        adapter=new ChatFragmentListAdapter(getContext(),conversazioni);
        adapter.notifyDataSetChanged();

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_chat,container,false);

        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_view_chat);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        conversazioni= MyApplication.getInstance().getDbChat().getAllConversazioni();
        Collections.sort(conversazioni);
        adapter=new ChatFragmentListAdapter(getContext(),conversazioni);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new ChatFragmentListAdapter.RecyclerTouchListener(getContext(),recyclerView, new ChatFragmentListAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversazioni conversazione=conversazioni.get(position);
                User user=MyApplication.getInstance().getDbChat().getUserFromConversazioni(conversazione);
               // Toast.makeText(getContext(),conversazione.getIdUtente(),Toast.LENGTH_SHORT).show();
                if(conversazione!=null) {
                    Intent intent = new Intent(getActivity(), ActivityChatRoom.class);
                    //Toast.makeText(getActivity(),user.NAME,Toast.LENGTH_SHORT).show();
                    intent.putExtra("conversation", conversazione);
                    intent.putExtra("user", user);
                    MyApplication.getInstance().setPaginaDaVisualizzare(numeroPagina);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, final int position) {
                Toast.makeText(getActivity(),"LONG CLIK",Toast.LENGTH_SHORT).show();

                new AlertDialog.Builder(getActivity())
                        .setTitle("Cancellare Chat")
                        .setMessage("Sei sicuro di voler cancellare la chat selezionata?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                int delete=MyApplication.getInstance().getDbChat().deleteConversazione(conversazioni.get(position));
                                conversazioni.remove(position);
                                Collections.sort(conversazioni);
                                adapter=new ChatFragmentListAdapter(getContext()
                                        ,conversazioni);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if(adapter.getItemCount()>0)
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                                Toast.makeText(getContext(),"You DELETE-> "+delete,Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                Toast.makeText(getContext(),"You not DELETE",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }));


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("newMessage")) {
                    // Log.e(TAG, "broadcastRicevuto");

                    Log.e(TAG,"ricevuto un messaggio nel fragment chat");
                    //devo controllare che la conversazione non ci sia gia
                    Conversazioni conversazioneIntent=(Conversazioni)intent.getSerializableExtra("conversazione");
                    //Log.e(TAG,conversazioneIntent.getIdUtente()+" =>"+((Message)intent.getSerializableExtra("message")).getTesto()+" =>messaggi non letti=>"+conversazioneIntent.getUnreadCount());

                    //conversazioni=MyApplication.getInstance().getDbChat().getAllConversazioni();
                    int index=0;
                    Log.e(TAG,"prima conversazione->"+String.valueOf(conversazioni.size()));
                    ListIterator<Conversazioni> iterator=conversazioni.listIterator();
                    while(iterator.hasNext()){
                        //Log.d(TAG,"next"+String.valueOf(iterator.nextIndex()));
                        //Log.d(TAG,"prev"+String.valueOf(iterator.previousIndex()));
                        Conversazioni c=iterator.next();
                        if(c.getConversazioniId().equals(conversazioneIntent.getConversazioniId())){
                            Message message=((Message)intent.getSerializableExtra("message"));
                            c.setLastMessage(message.getTesto());
                            c.setTimestamp(message.getOraMessaggio());
                            iterator.remove();
                            c.setUnreadCount(conversazioneIntent.getUnreadCount());
                            conversazioneIntent=c;

                        }
                    }

                    conversazioni.add(conversazioneIntent);
                    //Log.e(TAG,"dopo conversazione->"+String.valueOf(conversazioni.size()));
                    //fragmentChat.conversazioni.add((Conversazioni)intent.getSerializableExtra("conversazione"));
                    //conversazioni=MyApplication.getInstance().getDbChat().getAllConversazioni()
                    if(conversazioni.size()>0)
                        Collections.sort(conversazioni);


                    adapter=new ChatFragmentListAdapter(getContext()
                            ,conversazioni);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);

                }
            }
        };
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
      LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver,new IntentFilter("newMessage"));
        conversazioni=MyApplication.getInstance().getDbChat().getAllConversazioni();
        Collections.sort(conversazioni);

    }

    @Override
    public void onPause() {
        //Log.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG,"onStart");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver,new IntentFilter("newMessage"));
    }
}
