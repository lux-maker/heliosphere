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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.HashMap;

/**
 * generelle Infos:
 *
 * diese Activity wird einzig nur in der FirstAcessDecision Activity gestartet, wenn dort festgestellt wurde, dass die App das erste Mal geöffnet wird (=noch kein PW hinterlegt ist)
 * die Activity warte auf den Click auf den "CONFIRM" Button. Dann werde die beiden eingegeben Texte (PW + PW wiederholen) verglichen und der Hash-Wert des PWs in den shared Preferences abgespeichert, wenn die Texte gleich sind.
 * Außerdem wird ein RSA key erzeugt und eine PublicKeyMap, wo alle öffentlichen RSA-Schlüssel gesammelt werden. Dort wird auch der eigene öffentliche Schlüssel hinzugefügt.
 * Der private RSA-key wird mit dem neuen App-PW verschlüsselt und zusammen mit dem mit Base64 verschlüsselen public RSA key und der PublicKeyMap in die shared Preferences gespeichert, die nochmal mit dem Masterkey gesichert sind.
 * Am Ende ruft wird die Main Activity aufgerufen.
 */

public class CreatePasswordActivity extends AppCompatActivity {

    private EditText password, passwordRepeat;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_create_password); //created by default

        // assign view objects to code variables
        password = (EditText) findViewById(R.id.password); //objekt, was sich auf das erste Textfeld im GUI bezieht, darüber steht "Enter new Password", inputType="numberPassword"
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeat); //objekt, was sich auf das zweite Textfeld im GUI bezieht, darüber steht "Repeat Password", inputType="numberPassword"
        button = (Button) findViewById(R.id.button); //objekt, was sich auf den "CONFIRM" button aus dem GUI bezieht

        //implement a Callable for a "CONFIRM" button click
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when "CONFIRM" button clicked: 1. Schaue, ob etwas eingeben wurde 2.
            {
                String enteredPW = password.getText().toString();
                String enteredPWRepeat = passwordRepeat.getText().toString();

                if(enteredPW.equals("") || enteredPWRepeat.equals(""))
                {
                    //no password was entered -> show massage as "Toast", a small pop up at the bottom screen
                    Toast.makeText(CreatePasswordActivity.this, "No password entered!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //TODO checken ob die passwörter die geforderte sicherheit aufweisen? Länge des numerischen Passwortes?

                    if (enteredPW.equals(enteredPWRepeat)) // update the new password in the preferences
                    {
                        // the two passwords are equal -> generate shared preferences and store password
                        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");
                        SharedPreferences.Editor editor = settings.edit();

                        // now hash the password and save it
                        HashedPasswordInfo hashedPasswordInfo = HelperFunctionsCrypto.hashPassword(enteredPW.toCharArray());

                        //serialize hashed password object as json and store it in settings
                        //the serialization is done within the GsonHelper class (static member functions)
                        editor.putString("hashedPWInfo", GsonHelper.HashedPWInfo2String(hashedPasswordInfo)); //schreibe das Hash-Wert des PWs unter hashedPWInfo in die Datei AccessKey in den sharedPreferences

                        //generate RSA key pair; the private key gets encrypted with the app password
                        KeyPair keyPair = HelperFunctionsCrypto.generateRSAKeyPair();
                        HashMap<String, byte[]> privateKeyEncrypted = HelperFunctionsCrypto.encryptBytes(keyPair.getPrivate().getEncoded(), enteredPW.toCharArray());

                        // instantiate new hashMap to store the public keys that are potentially scanned by the user later adn store the own public key in it by default
                        HashMap<String,String> publicKeyMap = new HashMap<String,String>();
                        publicKeyMap.put("own key", HelperFunctionsStringByteEncoding.byte2string(keyPair.getPublic().getEncoded()));

                        String publicKeyMapJson = GsonHelper.toJson(publicKeyMap); //serialize public key as json
                        editor.putString("publicKeyMap", publicKeyMapJson); //and store it in settings in publicKeyMap

                        //serialize encrypted/encoded keys and store it in settings as well
                        String jsonPrivate = GsonHelper.toJson(privateKeyEncrypted);
                        editor.putString("RSAPrivate", jsonPrivate); //privater Schlüssel verschlüsselt mit App-PW

                        // apply changes to shared preferences
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