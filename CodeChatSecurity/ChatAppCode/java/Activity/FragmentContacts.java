package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ChatFragmentListAdapter;
import com.example.dado.chatsecurity.Adapter.ContattiAdapter;
import com.example.dado.chatsecurity.Adapter.ContattiAdapter.RecyclerTouchListener;
import com.example.dado.chatsecurity.Adapter.ContattiAdapterSearch;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.SimpleDividerItemDecoration;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Timer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by Dado on 25/07/2016.
 */
public class FragmentContacts extends Fragment {
    private ArrayList<User> userArrayList;
    private ContattiAdapter adapter;
    private RecyclerView recyclerView;
    BroadcastReceiver broadcastReceiver;
    ArrayList<User> arrayListUserTemp;
    private final String TAG=FragmentContacts.class.getSimpleName();
    SwipeRefreshLayout swipeRefreshLayout;
    static final int numeroPagina = 1;


    public FragmentContacts(){
        userArrayList=MyApplication.getInstance().getDbChat().getAmici();
        Collections.sort(userArrayList);
        adapter=new ContattiAdapter(getContext(),userArrayList);
        Log.e(TAG,""+userArrayList.size());
        adapter.notifyDataSetChanged();


    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.e(TAG,"fragmentContatcOnCreate");

        //da controllare quando creo la recicler view siccome quando la crea me la mette un po a muzzo

        View view=inflater.inflate(R.layout.fragment_contacts,container,false);
        userArrayList=MyApplication.getInstance().getDbChat().getAmici();
        Log.e(TAG,""+userArrayList.size());
        Collections.sort(userArrayList);

/*
        ArrayList<User> users=MyApplication.getInstance().getDbChat().getAllAmici();
        ImageView imageView=(ImageView)view.findViewById(R.id.imgView1);
        imageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(users.get(0).getUrlImage()));

        ImageView imageView1=(ImageView)view.findViewById(R.id.imgView2);
        imageView1.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(users.get(1).getUrlImage()));
*/
        recyclerView =(RecyclerView)view.findViewById(R.id.recycler_view);
        adapter=new ContattiAdapter(getActivity(),userArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new ContattiAdapter.RecyclerTouchListener(getContext(), recyclerView, new ContattiAdapter.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                //da qua far partire l'activity che gestisce la chat con questo utente
                User user = userArrayList.get(position);
                //Toast.makeText(getContext(),user.NAME,Toast.LENGTH_SHORT).show();
                String statoPersonale=user.getStatoPersonale();
                Conversazioni conversazioni=MyApplication.getInstance().getDbChat().getConversazioniFromUser(user);
                if(conversazioni!=null) {
                    Intent intent = new Intent(getActivity(), ActivityChatRoom.class);
                    //Toast.makeText(getActivity(),user.NAME,Toast.LENGTH_SHORT).show();
                    intent.putExtra("conversation", conversazioni);
                    intent.putExtra("user", user);
                    MyApplication.getInstance().setPaginaDaVisualizzare(numeroPagina);
                    startActivity(intent);
                }else{
                    //cpnversazione con id_conversazione=0
                    Conversazioni conversazioni1=new Conversazioni("0",user.getID_USER(),null,null);
                    Intent intent = new Intent(getActivity(), ActivityChatRoom.class);
                    MyApplication.getInstance().setPaginaDaVisualizzare(numeroPagina);
                    //Toast.makeText(getActivity(),user.NAME,Toast.LENGTH_SHORT).show();
                    intent.putExtra("conversation", conversazioni1);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    //Toast.makeText(getActivity(),"Creo una nuova chatRoom" ,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onLongClick(View view, final int position) {
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.myDialog))
                        .setTitle("Rimozione")
                        .setMessage("Vuoi rimuovere dai contatti "+userArrayList.get(position).NAME +"?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //lo devo rimuovere
                                MyApplication.getInstance().getDbChat().updateUserAmici(userArrayList.get(position));

                                userArrayList.get(position).amici=false;
                                userArrayList=MyApplication.getInstance().getDbChat().getAmici();
                                Collections.sort(userArrayList);
                                adapter=new ContattiAdapter(getActivity(),userArrayList);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if(adapter.getItemCount()>=0)
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);

                            }
                        })
                        .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }));


        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipeCOntainer);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //aggiunta dei nuovi contatti semplicemento scorrendo in giu con il diro
                //eseguire un thread che si collega al server scarica i contatti e le immagini e aggiorna tutta la tabella dei contatti
                //elimino la tabella contatti e la aggiorno con una nuova

                //bloccarlo
                if(MyApplication.haveInternetConnection()) {
                    Toast.makeText(getContext(), "Aggiorno Contatti", Toast.LENGTH_LONG).show();

                    new aggiornaContattiThread().execute();
                }
                else{
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(),"Attivare la connessione internet",Toast.LENGTH_LONG).show();
                }

            }
        });

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("updateAmici")){
                    userArrayList=MyApplication.getInstance().getDbChat().getAmici();
                    Collections.sort(userArrayList);
                    adapter=new ContattiAdapter(getActivity(),userArrayList);
                    recyclerView.setAdapter(adapter);
                    if(adapter.getItemCount()>=0)
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);


                }
            }
        };

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver,new IntentFilter("updateAmici"));

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"fragmentContacts on resume");
        userArrayList=MyApplication.getInstance().getDbChat().getAmici();
        Log.e(TAG,""+userArrayList.size());
        Collections.sort(userArrayList);
        adapter=new ContattiAdapter(getActivity(),userArrayList);
        recyclerView.setAdapter(adapter);
        if(adapter.getItemCount()>=0)
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver,new IntentFilter("updateAmici"));




    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public class aggiornaContattiThread extends AsyncTask<Void,Void,Void>{

        String fileJSON ;
        @Override
        protected Void doInBackground(Void... params) {

            //arrayList contenenti i dati dei contatti
            arrayListUserTemp = new ArrayList<>();

            try {
                fileJSON = "";
                URL url = new URL(EndPoint.BASE_URL+"getContatti.php");
                HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.setRequestMethod("POST");

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("root", "caputotavellamantovani99").appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER()).appendQueryParameter("password",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5());
                String data=builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();

                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                }
                fileJSON = stringBuilder.toString();

            }catch (SSLHandshakeException e) {
                Toast.makeText(MyApplication.getInstance(),"Server Non Verificato",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {

                // Log.e(TAG,"user->"+fileJSON);
                JSONObject jsonObject = new JSONObject(fileJSON);
                if(!jsonObject.getBoolean("errore")) {
                    //array di oggetti json
                    JSONArray jsonArray = jsonObject.getJSONArray("user");
                    for (int i =0;i<jsonArray.length();i++) {
                        JSONObject jsonUser = jsonArray.getJSONObject(i);

                        String public_key = jsonUser.getString("publicKey").toString();
                        String id_user = jsonUser.getString("user_id").toString();
                        String username = jsonUser.getString("username").toString();

                        String email = jsonUser.getString("email").toString();
                        String id_immagine_profilo = jsonUser.getString("id_immagine").toString();

                        String url_immagine_profilo = jsonUser.getString("urlImage").toString();
                        String[] image=url_immagine_profilo.split("/");
                        //Log.e(TAG,id_user);
                        arrayListUserTemp.add(new User(public_key, id_user, username, email, image[image.length-1]));
                        arrayListUserTemp.get(arrayListUserTemp.size()-1).setStatoPersonale(jsonUser.getString("statoPersonale"));

                    }
                    //cancella la tabella attuale e la ripopolo coni nuovi utenti ora devo scaricare le immagini
                    MyApplication.getInstance().getDbChat().dropTableContatti();
                    MyApplication.getInstance().getDbChat().insertAllContatti(arrayListUserTemp);
                    for(User u:userArrayList){
                        u.setAmici(false);
                        MyApplication.getInstance().getDbChat().updateUserAmici(u);
                        u.setAmici(true);
                    }

                    userArrayList=MyApplication.getInstance().getDbChat().getAmici();
                    adapter=new ContattiAdapter(getActivity()
                            ,userArrayList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
                    new downloadAndSaveImage().execute();
                }else{
                    Toast.makeText(getActivity(),jsonObject.getString("risultato"),Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }


    private class downloadAndSaveImage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            //tutti gli user dei nostri contatti
            for(User user:arrayListUserTemp){
                Bitmap b=getBitmapFromURL(EndPoint.BASE_URL+"uploadedimages/"+user.getUrlImage());
                String path=saveToInternalStorage(b,user.getUrlImage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            }

        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //funzione che salva l'immagine internamente dandole un nome
        private String saveToInternalStorage(Bitmap bitmapImage,String name) {
            ContextWrapper cw = new ContextWrapper(MyApplication.getInstance());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageProfile", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }

    }


}
