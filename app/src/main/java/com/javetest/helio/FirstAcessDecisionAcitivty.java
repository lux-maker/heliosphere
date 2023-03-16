package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.os.Bundle;
import android.os.Looper;
import android.content.Intent;
import android.os.Handler;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class FirstAcessDecisionAcitivty extends AppCompatActivity {

    String password; //global declaration (within class)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_acess_decision_acitivty);

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        EncryptedSharedPreferences settings = null;
        try
        {
            settings = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "PREFS",
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        password = settings.getString("pw",""); //if preference does not exist, return ""

        //create runnable and delay execution with handler (warum genau?)
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                if(password.equals("")) //first registration in application -> require user password input
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