package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Button;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.HashMap;

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
                    //TODO checken ob die passwörter die geforderte sicherheit aufweisen? Länge des numerischen Passwortes?
                    //TODO ist das der beste weg alles einzeln mit exceptions abzufangen?
                    if (p1.equals(p2)) // update the new password in the preferences
                    {
                        // the two passwords are equal -> generate shared preferences and store password
                        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");
                        SharedPreferences.Editor editor = settings.edit();

                        // now hash the password and save it
                        HashedPasswordInfo hashedPasswordInfo = HelperFunctionsCrypto.hashPassword(p1.toCharArray());

                        //serialize hashed password object as json and store it in settings
                        Gson gson = new Gson();
                        String json = gson.toJson(hashedPasswordInfo);
                        editor.putString("hashedPWInfo", json);

                        //generate RSA key pair, encrypt the private one and save both
                        KeyPair keyPair = HelperFunctionsCrypto.generateRSAKeyPair();
                        HashMap<String, byte[]> privateKeyEncrypted = HelperFunctionsCrypto.encryptBytes(keyPair.getPrivate().getEncoded(), p1.toCharArray());

                        //serialize encrypted/encoded keys and store it in settings as well
                        String jsonPrivate = gson.toJson(privateKeyEncrypted);
                        editor.putString("RSAPrivate", jsonPrivate);
                        editor.putString("RSAPublic", HelperFunctionsStringByteEncoding.byte2string(keyPair.getPublic().getEncoded()));

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