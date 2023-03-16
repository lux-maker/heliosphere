package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.widget.Toast;

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
                //load password from memory (prove data handling security -> true password will be encrypted)
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
                if(p.equals(settings.getString("pw", "")))
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