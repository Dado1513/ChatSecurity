package com.example.dado.chatsecurity.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dado on 29/08/16.
 */
public class FragmentChangePassword2 extends Fragment {

    EditText inputPassowrd,inputConfermaPassword;
    Button button;
    String nuovaPassword;
    TextInputLayout inpuPasswordLayout,inputCOnfermaPasswordLayout;
    private static final String TAG=FragmentChangePassword2.class.getSimpleName();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_change_password2,container,false);
        inputPassowrd=(EditText)view.findViewById(R.id.inputNewPassword);
        inputConfermaPassword=(EditText)view.findViewById(R.id.inputConfermaNewPassword);
        inpuPasswordLayout=(TextInputLayout)view.findViewById(R.id.inputNewPasswordLayout);
        inputCOnfermaPasswordLayout=(TextInputLayout)view.findViewById(R.id.inputConfermaNewPasswordLayout);


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button=(Button)view.findViewById(R.id.buttonCSendPassword2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.haveInternetConnection()) {
                    if (validaPassword()) {
                        nuovaPassword = inputPassowrd.getText().toString().trim();
                        new changePasswordThread().execute();
                        button.setClickable(false);
                    }
                }else{
                    Toast.makeText(getActivity(),"Per cambiare password bisogna disporre di una connessione internet",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validaPassword() {
        if (!inputPassowrd.getText().toString().trim().equals("")) {
            if (inputConfermaPassword.getText().toString().trim() != null && inputPassowrd.getText().toString().trim().equals(inputConfermaPassword.getText().toString().trim())) {
                inpuPasswordLayout.setErrorEnabled(false);
                return true;
            } else {
                inputCOnfermaPasswordLayout.setError("Password Non corrispondenti");
                return false;
            }
        } else {
            inputConfermaPassword.setEnabled(false);
            requestFocus(inputPassowrd);
            return false;
        }

    }

    private class MyTextWatcher implements TextWatcher {


        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch (view.getId()) {

            }


        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
            }
        }
    }




    private class changePasswordThread extends AsyncTask<Void,Void,Void> {

        String responseServer;
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url=new URL(EndPoint.BASE_URL+"updatePassword.php");
                Uri.Builder builder=new Uri.Builder();
                builder.appendQueryParameter("email", MyApplication.getInstance().getPrefManager().getUser().EMAIL).
                        appendQueryParameter("newPassword",ActivityLogin.md5(nuovaPassword)).
                        appendQueryParameter("oldPassword",MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5()).
                        appendQueryParameter("root","caputotavellamantovani99").
                        appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER());


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));


                String data=builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();


                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);

                responseServer=stringBuilder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG,responseServer);
            try {
                JSONObject object=new JSONObject(responseServer);
                if(!object.getBoolean("errore")){
                    //il cambiamento ha avuto successo
                    button.setClickable(true);
                    //aggiornare anche la password nel db??
                    Toast.makeText(getActivity(),"La Password è stata cambiata",Toast.LENGTH_LONG).show();
                    User user=MyApplication.getInstance().getPrefManager().getUser();
                    user.setPasswordMD5(ActivityLogin.md5(nuovaPassword));
                    MyApplication.getInstance().getPrefManager().storeUser(user);
                    startActivity(new Intent(getActivity(),SettingsActivity.class));
                    getActivity().finish();

                }else{
                    Toast.makeText(getActivity(),"Errore",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}
