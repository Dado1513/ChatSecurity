package com.example.dado.chatsecurity.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.R;

/**
 * Created by dado on 29/08/16.
 */
public class FragmentChangePassword1 extends Fragment {
    TextInputLayout textInputLayoutOldPassword;
    EditText editTextOldPassword;
    private static final String TAG =FragmentChangePassword1.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_change_password1,container,false);
        textInputLayoutOldPassword=(TextInputLayout)view.findViewById(R.id.inputOldPasswordLayout);
        editTextOldPassword=(EditText)view.findViewById(R.id.inputOldPassword);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button button=(Button)view.findViewById(R.id.buttonCambiaPassword1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(),"CLICK of"+FragmentChangePassword1.class.getSimpleName(),Toast.LENGTH_LONG).show();
                String oldPassword=editTextOldPassword.getText().toString();

                String md5OldPassword=ActivityLogin.md5(oldPassword);
                Log.e(TAG,md5OldPassword);
                if(md5OldPassword.equals(MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5())) {
                    textInputLayoutOldPassword.setErrorEnabled(false);
                    FragmentChangePassword2 fragmentChangePassword2 = new FragmentChangePassword2();
                    FragmentManager manager = getFragmentManager();//create an instance of fragment manager

                    FragmentTransaction transaction = manager.beginTransaction();//create an instance of Fragment-transaction

                    transaction.replace(R.id.changePasswordActivity, fragmentChangePassword2, "Frag_Top_tag");
                    transaction.commit();
                }else{
                    textInputLayoutOldPassword.setError("Password non corretta");
                }
            }
        });



    }
}
