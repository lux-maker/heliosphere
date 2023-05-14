package com.javetest.helio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.content.Intent;
import android.os.Handler;
import android.Manifest;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.security.KeyPair;
import java.util.concurrent.Semaphore;

/**
 * generelle Infos:
 * die Actifity wird beim Starten der App zuallererst aufgerufen
 * [festgelegt in AndroidManifest.xml:
 * Ist mit einem Intent-Filter belegt, wodruch nur Intents der Art: <action android:name="android.intent.action.MAIN" />
 * mit <category android:name="android.intent.category.LAUNCHER" /> die Klasse ansprechen können.
 * Klicken des App-Icons im Home-Screen des Handys ruft diese FirstAcessDecision Activity auf.]
 *
 * Es wird überprüft, ob bereits ein Passwort für die App existiert, also ob sie schonmal geöffnet wurde oder gerade die App das aller erste Mal geöffnet wird.
 * Ist bereits ein Passwort hinterlegt (= die App wurde schonmal ausgeführt) soll dieses abgefragt werden und die Activity leitet dazu an die EnterPassword Activity weiter.
 * Ist noch kein Passwort hinterlegt (=die App wird das erste Mal geöffnet) soll ein Passwort angelegt werden und die Activity leitet dazu an die CreatePassword Activity weiter.
*/

public class FirstAcessDecisionAcitivty extends AppCompatActivity {

    String json; //global declaration (within class)

    //initialize handler that repeats the sanity checks every 30 seconds
    private Handler sanityHandler;
    private Runnable sanityRunnable;
    int PERMISSION_REQUEST_CODE = 1;

    private static final long DELAY_MS = 1000; // 1 second
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_first_acess_decision_acitivty);

        //start the handler that performs the sanity checks in the background
        sanityHandler = new Handler();
        sanityRunnable = () -> {
                //perform sanity checks connectivity
                SanityChecks sanityChecks = new SanityChecks();

                boolean checksAreOK = sanityChecks.performChecks(getApplicationContext(), FirstAcessDecisionAcitivty.this);

                if (!checksAreOK)
                {
                    Intent intent = new Intent(getApplicationContext(), FirstAcessDecisionAcitivty.class);
                    startActivity(intent);
                    finish();
                    return; //to finnish the runnable loop
                }
                sanityHandler.postDelayed(sanityRunnable, DELAY_MS);
        };
        sanityHandler.postDelayed(sanityRunnable, DELAY_MS);

        //load SharedPreferences from memory and check if it already contains a password, otherwise define one
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext()); //enthält neu erzeugten neuen master key, den es braucht um jetzt gleich die verschlüsselten sharedPreferences zu öffnen //MasterKey: Wrapper-class, references a key that's stored in the Android Keystore //context: in order to access the stored preferences
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys"); //enthält unverschlüsselten Inhalt aus der Datei AcessKey in den sharedPreferences.

        json = settings.getString("hashedPWInfo",""); //if preference does not exist, return ""

        //create runnable and delay execution with handler (warum genau?)
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent;
                if(json.equals("")) //first registration in application -> require user password input
                {
                    intent = new Intent(getApplicationContext(), CreatePasswordActivity.class); //create intent to start CreatePasswordActivity
                }
                else // password was created before, request it
                {
                    intent = new Intent(getApplicationContext(), EnterPasswordActivity.class); //create intent to start EnterPasswordActivity
                }
                startActivity(intent); //start EnterPasswordActivity
                finish();
            }
        };

        //run handler
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(r, 2000);
    }
}