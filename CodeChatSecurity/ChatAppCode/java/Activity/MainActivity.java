package com.example.dado.chatsecurity.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dado.chatsecurity.Adapter.ChatFragmentListAdapter;
import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.NotificationUtils;
import com.example.dado.chatsecurity.Model.Conversazioni;
import com.example.dado.chatsecurity.Model.DbChat;
import com.example.dado.chatsecurity.Model.Message;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;
import com.example.dado.chatsecurity.Service.LocalizationService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    int[] tabIcons = {
            R.drawable.ic_chat_white_36dp,
            R.drawable.ic_contacts_white_36dp
    };
    static int numeroPagina = 1;

    BroadcastReceiver broadcastReceiver;
    FragmentContacts fragmentContacts;
    FragmentChat fragmentChat;
    FragmentMap fragmentMap;
    User user;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_scorrevoli);
        user = MyApplication.getInstance().getPrefManager().getUser();

        SharedPreferences preferences = getSharedPreferences("pref_image", 0);
        SharedPreferences.Editor edit=preferences.edit();
        edit.clear();
        edit.commit();

        Log.e(TAG,String.valueOf(user.isLocation));
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if(user.isLocation) {
            Intent intent = new Intent(this, LocalizationService.class);
            startService(intent);
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        invalidateOptionsMenu();
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (MyApplication.getInstance().getDbChat().getAllConversazioni() == null || MyApplication.getInstance().getDbChat().getAllConversazioni().size() == 0) {
            fragmentContacts = new FragmentContacts();
            fragmentChat = new FragmentChat();
            fragmentMap = new FragmentMap();
        } else {
            fragmentChat = new FragmentChat();
            fragmentContacts = new FragmentContacts();
            fragmentMap = new FragmentMap();

        }
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        if (!MyApplication.haveInternetConnection()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Attiva La connessione Internet per poter inviare e ricevere i messaggi", Toast.LENGTH_LONG);
            //toast.setText(R.string.utenteGiaLoggato);
            TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
            textView.setTextColor(Color.parseColor("#FF4500"));
            toast.show();
        }


        if (MyApplication.getInstance().getDbChat().getAllConversazioni().size() == 0) {
            viewPager.setCurrentItem(1);
        }

        Intent intent = getIntent();
           if(intent!=null && intent.getBooleanExtra("main ricevuto",false) && MyApplication.getInstance().getDbChat().getAmici().size()==0){

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.layouttoast,
                    (ViewGroup) findViewById(R.id.relativeLayout1));
            ImageView imageView=(ImageView)view.findViewById(R.id.imageView1);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG,"click");
                    Intent intentCerca = new Intent(getApplicationContext(), SearchActivity.class);
                    MyApplication.getInstance().setPaginaDaVisualizzare(viewPager.getCurrentItem());
                    startActivity(intentCerca);
                }
            });
            Toast toast = new Toast(this);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(view);
            toast.show();

       }


    }


    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        // bisogna metterma a posto/////////////////////////////////////////////////////////
        tabLayout.getTabAt(2).setIcon(tabIcons[0]);
        //tabLayout.getTabAt(2).setIcon(tabIcons[0]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(fragmentChat, "CHAT");
        viewPagerAdapter.addFragment(fragmentContacts, "CONTACTS");
        viewPagerAdapter.addFragment(fragmentMap, "MAP");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(MyApplication.getInstance().getPaginaDaVisualizzare());

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFrgmentlist = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            return mFrgmentlist.get(position);
        }

        @Override
        public int getCount() {
            return mFrgmentlist.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFrgmentlist.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //solo le icone qua torno nulllo
            return mFragmentTitleList.get(position);
            //solo icone
            //return null ;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (MyApplication.getInstance().getDbChat().getAllConversazioni() == null || MyApplication.getInstance().getDbChat().getAllConversazioni().size() == 0) {
            fragmentContacts = new FragmentContacts();
            fragmentChat = new FragmentChat();
            fragmentMap = new FragmentMap();
        } else {
            fragmentChat = new FragmentChat();
            fragmentContacts = new FragmentContacts();
            fragmentMap = new FragmentMap();

        }
        setupViewPager(viewPager);
        Log.e(TAG, "MAIN on RESTART");


    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "MAIN on Resume");
        NotificationUtils.clearNotifications();

        user=MyApplication.getInstance().getPrefManager().getUser();
        ArrayList<User> users=MyApplication.getInstance().getDbChat().getAllAmici();
        for(User u: users){
            MyApplication.getInstance().getPrefManager().clearNotification(u.NAME);
        }
        if(user.isLocation) {
            Intent intent = new Intent(this, LocalizationService.class);
            startService(intent);
        }
        Log.e(TAG,""+MyApplication.getInstance().getDbChat().getAmici().size());

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (user.isLocation)
            menuInflater.inflate(R.menu.menu_main, menu);
        else
            menuInflater.inflate(R.menu.menu_main_location_on, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog))
                        .setTitle("Logout")
                        .setMessage("Sei sicuro di voler uscire?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                MyApplication.getInstance().logout();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                return;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)

                        .show();
                return true;

            case R.id.cerca:
                Intent intentCerca = new Intent(getApplicationContext(), SearchActivity.class);
                MyApplication.getInstance().setPaginaDaVisualizzare(viewPager.getCurrentItem());
                startActivity(intentCerca);
                return true;

            case R.id.settings:
                MyApplication.getInstance().setPaginaDaVisualizzare(viewPager.getCurrentItem());
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));

                return true;

            case R.id.changeLocationOn:
                Toast.makeText(getApplicationContext(), "Localizzazione Disattivata", Toast.LENGTH_LONG).show();
                // devo mettere a 1 la localizzazione dell'utente cosi si vede
                new CambioAggiornamentoPosizione().execute("0");
                MyApplication.getInstance().getPrefManager().setLocation(false);
                user=MyApplication.getInstance().getPrefManager().getUser();
                user.isLocation=false;
                this.invalidateOptionsMenu();
                stopService(new Intent(this,LocalizationService.class));

                return true;
            case R.id.changeLocationOff:
                Toast.makeText(getApplicationContext(), "Localizzazione Attiva", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    // devo mettere a 0 la localizzazione dell'utente cosi NON si vede
                    new CambioAggiornamentoPosizione().execute("1");
                    MyApplication.getInstance().getPrefManager().setLocation(true);
                    user = MyApplication.getInstance().getPrefManager().getUser();
                    user.isLocation = true;
                    startService(new Intent(this, LocalizationService.class));
                    this.invalidateOptionsMenu();
                }
                return true;
            case R.id.changeKey:
                startActivity(new Intent(this, ActivityKey.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    class CambioAggiornamentoPosizione extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                String idString = MyApplication.getInstance().getPrefManager().getUser().getID_USER();
                String valore = params[0];
                URL url = new URL(EndPoint.BASE_URL + "updatePositionNota.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                //attacco tutti i parametri che voglio inviare al server
                builder.appendQueryParameter("id_utente", idString).appendQueryParameter("valore", valore);
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

                Log.e("mm->", stringBuilder.toString());


            } catch (Exception e) {

                e.printStackTrace();
                return null;
            }


            return null;
        }
    }


}