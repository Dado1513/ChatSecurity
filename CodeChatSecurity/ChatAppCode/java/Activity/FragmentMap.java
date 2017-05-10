package com.example.dado.chatsecurity.Activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ContattiAdapter;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.CircleImageView;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.DbChat;
import com.example.dado.chatsecurity.Model.Posizioni;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import static com.google.android.gms.wearable.DataMap.TAG;


public class FragmentMap extends android.support.v4.app.Fragment {

    MapView mapView;
    GoogleMap map;
    ArrayList<User> arrayList;
    private ArrayList<User> userArrayList;
    ArrayList<User> arrayListUserTemp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        arrayList = new ArrayList<>();
        RitornoPosizioni ritornoPosizioni = new RitornoPosizioni();
        try{
           Void r = ritornoPosizioni.execute().get();

            aggiornaContattiThread a = new aggiornaContattiThread();
            Void objects = a.execute().get();
        }catch (Exception e){e.printStackTrace();}

/**************************/
//parte per creare l'immagine bella per la mappa //

        // add the marker

        /**********************/
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);


        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        LatLng punto1 = new LatLng(42, 8);
        LatLng punto2 = new LatLng(42, 9);
        CircleImageView circleImageView= new CircleImageView(getContext());
        //circleImageView.setImageBitmap(MyApplication.getInstance().loadImageFromStorage("32.jpg"));

        //aggiungere un immaggine invece del classico mark
        // map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(circleImageView.getmBitmap(),50,50,true))).position(punto1).title("Punto 1"));


        LatLng you=null;
        LatLng prova=null;
        //bisogna caricare tutte le immagini bitmap qua
        for(int i=0;i< arrayList.size();i++) {
            User user = arrayList.get(i);
            LatLng MELBOURNE = new LatLng(Float.parseFloat(user.LATITUDINE), Float.parseFloat(user.LONGITUDINE));
            Log.e(TAG,user.NAME+"->"+MELBOURNE.toString());
            prova=MELBOURNE;
            String id = user.getID_USER();
            if(!id.equals(MyApplication.getInstance().getPrefManager().getUser().getID_USER()) &&  MyApplication.getInstance().getDbChat().getUserFromId(id).amici) {
                User locale = MyApplication.getInstance().getDbChat().getUserFromId(id);
                Log.e("percorso",locale.URL_IMMAGINE_PROFILO);
//                Marker melbourne = map.addMarker(new MarkerOptions().position(MELBOURNE).title(user.NAME).snippet("Population: 4,137,400").icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(MyApplication.getInstance().loadImageFromStorage( locale.URL_IMMAGINE_PROFILO), 50, 50, true))));
                int radius = 100;
                int stroke = 4;
                float verticalAnchor = 0.944f;
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap((int) radius, (int) radius + 25, conf);
                Canvas canvas = new Canvas(bmp);

                //immagine da inserire
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.temp);
                Bitmap bitmap=MyApplication.getInstance().loadImageFromStorage(locale.getUrlImage());
                Log.e("MAp",bitmap.toString());
                // creates a centered bitmap of the desired size
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, (int) radius - stroke, (int) radius - stroke, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.parseColor("#125688"));
                //paint.setColor(0xff464646);
                paint.setStyle(Paint.Style.FILL);

                // the triangle laid under the circle
                int pointedness = 20;
                Path path = new Path();
                path.setFillType(Path.FillType.EVEN_ODD);
                path.moveTo(radius / 2, radius + 15);
                path.lineTo(radius / 2 + pointedness, radius - 10);
                path.lineTo(radius / 2 - pointedness, radius - 10);
                canvas.drawPath(path, paint);

                // gray circle background
                RectF rect = new RectF(0, 0, radius, radius);
                canvas.drawRoundRect(rect, radius / 2, radius / 2, paint);

                // circle photo
                paint.setShader(shader);
                rect = new RectF(stroke, stroke, radius - stroke, radius - stroke);
                canvas.drawRoundRect(rect, (radius - stroke) / 2, (radius - stroke) / 2, paint);

                Marker marker = map.addMarker(new MarkerOptions().position(MELBOURNE).icon(BitmapDescriptorFactory.fromBitmap(bmp)).anchor(0.5f, verticalAnchor).title(locale.NAME));

            }
            else if(id.equals(MyApplication.getInstance().getPrefManager().getUser().getID_USER())) {
                map.addMarker(new MarkerOptions().position(MELBOURNE).title("YOU").icon(BitmapDescriptorFactory.defaultMarker()));
                you=MELBOURNE;
            }




        }
        //aggiumnta del marker personalizzato circolare con l'immaginbe
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(punto1);


        CameraUpdate cameraUpdate;
        if(you!=null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(you, 17);
            Log.e(TAG,"you");
        }else {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(prova, 17);
            Log.e(TAG,"prova");
        }
        if(MyApplication.haveInternetConnection())
            map.animateCamera(cameraUpdate);
        else{

            Toast toast = Toast.makeText(getActivity(), "Attiva La connessione Internet per poter avere la mappa aggiornata", Toast.LENGTH_LONG);
            //toast.setText(R.string.utenteGiaLoggato);
            TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
            textView.setTextColor(Color.parseColor("#FF4500"));
            toast.show();
        }

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if(mapView!=null)
            mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public String ritornoUsernameDaIdUtente(int id)
    {
        try
        {
            String idString = String.valueOf(id);
            URL url = new URL(EndPoint.BASE_URL+"getUsernameFromIdUser.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            Uri.Builder builder = new Uri.Builder();
            //attacco tutti i parametri che voglio inviare al server
            builder.appendQueryParameter("id", idString);
            String query = builder.build().getEncodedQuery();
            writer.write(query);
            writer.flush();
            writer.close();


            urlConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null)
                stringBuilder.append(nextLine);

            String messaggioInput = stringBuilder.toString();
            return messaggioInput;

        }catch (Exception e) {

            e.printStackTrace();
            return null;
        }

    }

    class RitornoPosizioni extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                URL url = new URL(EndPoint.BASE_URL + "getPosition.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("id",MyApplication.getInstance().getPrefManager().getUser().getID_USER());
                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();

                urlConnection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);

                String messaggioInput = stringBuilder.toString();
                parserFileJson(messaggioInput);


            }catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
        public void parserFileJson(String builder)
        {
            Log.d("TAG",builder);
            try {

                JSONArray jsonArray = new JSONArray(builder);
                for(int i =0;i<jsonArray.length();i++)
                {
                    JSONObject jsonObject=jsonArray.getJSONObject(i);

                    //???????????????????????????????????????????????????????????????????????????????
                    String idUtente = jsonArray.getJSONObject(i).getString("id_utente");
                    String latitudine = jsonArray.getJSONObject(i).getString("latitudine");
                    String longitudine = jsonArray.getJSONObject(i).getString("longitudine");
                    String username = ritornoUsernameDaIdUtente(Integer.parseInt(idUtente));
                    String percorsoImmagine = EndPoint.BASE_URL + "uploadedimages/"+idUtente+".jpg";
                    User user = new User(0,idUtente,username,percorsoImmagine,latitudine,longitudine);
                    arrayList.add(user);
                    //????????????????????????????????
                }


            }catch(Exception e){
                e.printStackTrace();
            }

        }
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

                 Log.e(TAG,"user->"+fileJSON);
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
                    /*
                    MyApplication.getInstance().getDbChat().dropTableContatti();
                    MyApplication.getInstance().getDbChat().insertAllContatti(arrayListUserTemp);
                    if(userArrayList.size()>0)
                        for(User u:userArrayList){
                            u.setAmici(false);
                            MyApplication.getInstance().getDbChat().updateUserAmici(u);
                            u.setAmici(true);
                        }

                    userArrayList=MyApplication.getInstance().getDbChat().getAmici();
                    */
                    if(arrayListUserTemp.size()>0) {
                        for (User u : arrayListUserTemp) {
                            if(MyApplication.getInstance().getDbChat().getUserFromId(u.getID_USER())==null){
                                MyApplication.getInstance().getDbChat().addContact(u);
                            }
                        }
                    }
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