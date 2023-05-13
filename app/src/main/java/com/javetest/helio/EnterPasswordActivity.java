package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;
import android.widget.Toast;

import com.google.gson.Gson;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;

/**
 * generelle Infos:
 *
 * diese Activity wird einzig nur in der FirstAcessDecision Activity gestartet, wenn dort festgestellt wurde, dass die App nicht das erste Mal geöffnet wird (=ein App-PW hinterlegt ist)
 * und dieses nun Überprüft werden soll.
 * Dazu wird der Hash-Wert des APP-PWs aus den (encrypted) shared Preferences geladen, sowie der verwendete Salt. Mit dem Salt wird das zu überprüfende PW gehashed und mit dem Hash-Wert des App-PWs verglichen.
 * Wenn die PWs gleich sind, wird die Main Activity gestartet, ansonsten wird eine Fehlermeldung angezeigt.
 *
 */
public class EnterPasswordActivity extends AppCompatActivity {

    EditText enteredPW;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_enter_password); //created by default

        // assign view objects to code variables
        enteredPW = (EditText) findViewById(R.id.password); //objekt, was sich auf das erste Textfeld im GUI bezieht, darüber steht "Enter new Password", inputType="numberPassword"
        button = (Button) findViewById(R.id.button2); //objekt, was sich auf den "ENTER" button aus dem GUI bezieht

        //implement a Callable for a "ENTER" button click
        button.setOnClickListener((View view) ->
        {
            //when "ENTER" button clicked: speichere die Eingabe, lade den Hash-Wert und den Salt des APP-PWs, hashe das eingegebene PW mit dem geladenen Salt und vergleiche
            //die Hash-Werte. Wenn gleich, dann starte Main Activity sonst zeige eine Fehlermeldung an.
            String enteredPassword = enteredPW.getText().toString();
            if(enteredPassword.equals(""))
            {
                Toast.makeText(EnterPasswordActivity.this, "please type in password", Toast.LENGTH_SHORT).show();
            }
            else {
                //load hashed password from memory
                MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");

                //reorganize the hashed password from memory, log error if password doesn't exists yet (shouldn't ever happen at this point
                String string = settings.getString("hashedPWInfo", "");
                if (string.equals(""))
                {
                    Log.e("EnterPasswordActivity", "hashed password loading failure");
                }

                HashedPasswordInfo hashedPasswordInfo = GsonHelper.String2HashedPWInfo(string);

                //hash the newly entered password by reusing the salt from the trueHashedPassword
                HashedPasswordInfo enteredHashedPasswordInfo = HelperFunctionsCrypto.hashPassword(hashedPasswordInfo.getSalt(), enteredPassword.toCharArray());

                // compare hashes with overwritten equals ("==") operator
                if (hashedPasswordInfo.equals(enteredHashedPasswordInfo)) //hashedPasswordInfo = eingegebenes PW // trueHashedPasswordInfo = tatsächliches PW-Passwort
                {
                    //reset failedAttempts tp 0
                    PasswordAttemptsHandler.setCurrentFailedAttemptsCounter(getApplicationContext(), 0);

                    //start the main application
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                { //entered passwort is not the correct password

                    //load the failed attempts from memory and increment it
                    int failedAttempts = PasswordAttemptsHandler.getCurrentFailedAttemptsCounter(getApplicationContext());
                    ++failedAttempts;

                    PasswordAttemptsHandler.setCurrentFailedAttemptsCounter(getApplicationContext(), failedAttempts);

                    //check if the maximum number of attempts is reached and delete everything if so
                    if (failedAttempts >= PasswordAttemptsHandler.getMaxAllowedNumOfFailedAttempts())
                    {
                        TotalAnnilihator totalAnnilihator = new TotalAnnilihator();
                        totalAnnilihator.clearAll(getApplicationContext());
                        Toast.makeText(EnterPasswordActivity.this, "Maximum amount of failed Attempts reached. The application was reset. All keys were deleted.", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), FirstAcessDecisionAcitivty.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(EnterPasswordActivity.this, "Wrong password. " + Integer.toString(PasswordAttemptsHandler.getLeftFailedAttempts(getApplicationContext())) + " Attempts left until the application will reset and all keys will be deleted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}