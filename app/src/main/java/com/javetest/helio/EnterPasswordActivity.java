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


public class EnterPasswordActivity extends AppCompatActivity {

    EditText enteredPW;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        // assign view objects to code variables
        enteredPW = (EditText) findViewById(R.id.password);
        button = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String p = enteredPW.getText().toString();

                //load password from memory
                MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

                //reorganize the hashed password from memory
                Gson gson = new Gson();
                String json = settings.getString("hashedPWInfo", ""); //TODO sichergehen dass an dieser stelle niemals "" zurückgegeben wird
                HashedPasswordInfo trueHashedPasswordInfo = gson.fromJson(json, HashedPasswordInfo.class);

                //hash the newly entered password by reusing the salt from the trueHashedPassword
                HashedPasswordInfo hashedPasswordInfo = HelperFunctionsCrypto.hashPassword(trueHashedPasswordInfo.getSalt(), p.toCharArray());

                // compare hashes with overwritten equals ("==") operator
                if(hashedPasswordInfo.equals(trueHashedPasswordInfo))
                {
                    //start the main application
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(EnterPasswordActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}