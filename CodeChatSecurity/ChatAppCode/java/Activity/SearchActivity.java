package com.example.dado.chatsecurity.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ChatFragmentListAdapter;
import com.example.dado.chatsecurity.Adapter.ContattiAdapterSearch;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.SimpleDividerItemDecoration;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Dado on 12/08/2016.
 */
public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    //aggiungere il listener se arriva un nuovo messaggio
    //broadcastReceiver
    //un amico non rimane dopo che lo selezionato trovare il modo
    public ArrayList <User> users;
    RecyclerView recyclerView;
    ContattiAdapterSearch adapterSearch;
    final String TAG=SearchActivity.class.getSimpleName();
    AlertDialog.Builder alertAddAmici,alertRemoveAmici;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        searchView = (SearchView) findViewById(R.id.search);
        searchView.setIconifiedByDefault(false);
        searchView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        searchView.setQueryHint("Username da cercare");


        alertAddAmici= new AlertDialog.Builder(new ContextThemeWrapper(SearchActivity.this, R.style.myDialog));
        alertRemoveAmici=new AlertDialog.Builder(new ContextThemeWrapper(SearchActivity.this,R.style.myDialog));
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);

        recyclerView=(RecyclerView)findViewById(R.id.recycler_viewSearch);
        users=MyApplication.getInstance().getDbChat().getAllAmici();
        Collections.sort(users);
        adapterSearch=new ContattiAdapterSearch(this,users);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplication()));
        recyclerView.setLayoutManager(linearLayoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getApplicationContext(),"CLICK",Toast.LENGTH_LONG).show();
                if (query != null && !query.equals("")) {
                    users = MyApplication.getInstance().getDbChat().getAllandSetAmici(query);
                    Collections.sort(users);
                    adapterSearch = new ContattiAdapterSearch(getApplicationContext(), users);
                    recyclerView.setAdapter(adapterSearch);
                    adapterSearch.notifyDataSetChanged();
                    if(adapterSearch.getItemCount()>0)
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
                }

                return false;
            }

            // quando cambia il testo
            @Override
            public boolean onQueryTextChange(String newText) {
                //creo una query per il db in locale passando come parametro il nome che sta digitando
                //ogni volta interrogo il db oppure li recupero tutti subito e cro un array di utente da fare vedere solo con le rispettive
                //lettere digitate new text nel nome

                //Toast.makeText(getApplicationContext(),newText,Toast.LENGTH_LONG).show();
                if(newText!=null && !newText.equals("")) {
                    users = MyApplication.getInstance().getDbChat().getAllandSetAmici(newText);
                    Collections.sort(users);
                    adapterSearch = new ContattiAdapterSearch(getApplicationContext(), users);
                    recyclerView.setAdapter(adapterSearch);
                    adapterSearch.notifyDataSetChanged();
                    if(adapterSearch.getItemCount()>0)
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);

                }
                return false;
        }});





    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        users=MyApplication.getInstance().getDbChat().getAllAmici();
        Collections.sort(users);
        //adapterSearch=new ContattiAdapterSearch(SearchActivity.this,users);
        //recyclerView.setAdapter(adapterSearch);
        //recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,null,0);

    }


}

