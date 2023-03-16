package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Button;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class CreatePasswordActivity extends AppCompatActivity {

    private EditText password, passwordRepeat;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        // assign view objects to code variables
        password = (EditText) findViewById(R.id.password);
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeat);
        button = (Button) findViewById(R.id.button);

        //implement a Callable for a button click
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String p1 = password.getText().toString();
                String p2 = passwordRepeat.getText().toString();

                if(p1.equals("") || p2.equals(""))
                {
                    //no password was entered -> show massage as "Toast", a small pop up at the bottom screen
                    Toast.makeText(CreatePasswordActivity.this, "No password entered!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (p1.equals(p2)) // update the new password in the preferences
                    {
                        // the two passwords are equal -> generate shared preferences and store password
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

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("pw", p1);
                        editor.apply();

                        //start the main application
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else //passwords do not match -> pop up message
                    {
                        Toast.makeText(CreatePasswordActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}