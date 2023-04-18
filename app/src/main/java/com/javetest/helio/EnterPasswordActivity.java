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
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when "ENTER" button clicked: speichere die Eingabe, lade den Hash-Wert und den Salt des APP-PWs, hashe das eingegebene PW mit dem geladenen Salt und vergleiche die Hash-Werte. Wenn gleich, dann starte Main Activity sonst zeige eine Fehlermeldung an.
            {
                String p = enteredPW.getText().toString();
                if(p.equals(""))
                {
                    Toast.makeText(EnterPasswordActivity.this, "please type in password", Toast.LENGTH_SHORT).show();
                }
                else {

                    //load password from memory
                    MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                    SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

                    //reorganize the hashed password from memory
                    Gson gson = new Gson();
                    String json = settings.getString("hashedPWInfo", ""); //TODO sichergehen dass an dieser stelle niemals "" zurückgegeben wird

                    //json wurde in CreatePasswort Activity aufwendig aufgeteilt und verknüpft, dass müssen wir jetzt wieder entdrosseln:
                    String[] json_split = json.split(";"); //json = "salt ; hash ; algorithm"
                    byte[] pw_salt = gson.fromJson(json_split[0], byte[].class);
                    byte[] pw_SecretKeySpec_key = gson.fromJson(json_split[1], byte[].class);
                    String pw_SecretKeySpec_algorithm = gson.fromJson(json_split[2], String.class);
                    SecretKeySpec secretKeySpec = new SecretKeySpec(pw_SecretKeySpec_key, pw_SecretKeySpec_algorithm); //erstelle ein secretKeySpec Objekt, damit wir das für die HashedPasswordInfo class verwenden können
                    HashedPasswordInfo trueHashedPasswordInfo = new HashedPasswordInfo(pw_salt, secretKeySpec); //Objekt enthält Hash-Wert des App-PWs und den Salt


                    //hash the newly entered password by reusing the salt from the trueHashedPassword
                    HashedPasswordInfo hashedPasswordInfo = HelperFunctionsCrypto.hashPassword(trueHashedPasswordInfo.getSalt(), p.toCharArray());

                    // compare hashes with overwritten equals ("==") operator
                    if (hashedPasswordInfo.equals(trueHashedPasswordInfo)) //hashedPasswordInfo = eingegebenes PW // trueHashedPasswordInfo = tatsächliches PW-Passwort
                    {
                        //start the main application
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EnterPasswordActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}