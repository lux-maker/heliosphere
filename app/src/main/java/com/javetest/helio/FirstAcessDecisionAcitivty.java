package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.content.Intent;
import android.os.Handler;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.security.KeyPair;

public class FirstAcessDecisionAcitivty extends AppCompatActivity {

    String json; //global declaration (within class)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_acess_decision_acitivty);

        //load SharedPreferences from memory and check if it already contains a password, otherwise define one
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

        json = settings.getString("hashedPWInfo",""); //if preference does not exist, return ""

        //create runnable and delay execution with handler (warum genau?)
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                if(json.equals("")) //first registration in application -> require user password input
                {
                    Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class); //create intent to start CreatePasswordActivity
                    startActivity(intent); //start CreatePasswordActivity
                    finish(); //finnish current activity
                }
                else // password was created before, request it
                {
                    Intent intent = new Intent(getApplicationContext(), EnterPasswordActivity.class); //create intent to start CreatePasswordActivity
                    startActivity(intent); //start CreatePasswordActivity
                    finish(); //finnish current activity
                }
            }
        };

        //run handler
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(r, 2000);

    }
}