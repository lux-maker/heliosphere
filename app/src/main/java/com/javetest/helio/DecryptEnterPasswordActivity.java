package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class DecryptEnterPasswordActivity extends AppCompatActivity {
    EditText enteredPW;
    Button button;

    SharedPreferences settings;
    Gson gson = new Gson();
    HashedPasswordInfo trueHashedPasswordInfo;
    PrivateKey privateKey = null;
    HashMap<String, byte[]> privateKeyEncrypted;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt_enter_password);

        enteredPW = (EditText) findViewById(R.id.password);
        button = (Button) findViewById(R.id.button2);

        //run the loading of passwords on a different thread to preserve frame rate
        new Thread(new Runnable() {
            public void run() {
                Log.i("DecryptEnterPasswordActivity", "thread is running");

                //load password from memory
                MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

                //load true hashed password
                String json = (settings.getString("hashedPWInfo", ""));
                trueHashedPasswordInfo = gson.fromJson(json, HashedPasswordInfo.class);

                //load RSA key from memory
                String jsonKey = settings.getString("RSAPrivate", "");
                Type type = new TypeToken<HashMap<String, byte[]>>(){}.getType();
                privateKeyEncrypted = gson.fromJson(jsonKey, type);

            }
        }).start();

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //hash entered password
                String p = enteredPW.getText().toString();
                HashedPasswordInfo enteredHashedPasswordInfo = HelperFunctionsCrypto.hashPassword(trueHashedPasswordInfo.getSalt(), p.toCharArray());

                //compare passwords
                if (trueHashedPasswordInfo.equals(enteredHashedPasswordInfo))
                {
                    //passwords match -> start to decrypt message
                    Log.i("DecryptEnterPasswordActivity", "passwords match");

                    //decrypt private RSA key
                    try
                    {
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(HelperFunctionsCrypto.decryptBytes(privateKeyEncrypted, p.toCharArray()));
                        privateKey = keyFactory.generatePrivate(privateKeySpec);
                    }
                    catch(Exception e)
                    {
                        Log.e("DecryptEnterPasswordActivity", "RSA key decoding failure", e);
                    }

                    //load encrypted message and decrypt it
                    String encryptedMessage = getIntent().getStringExtra("encryptedMessage");
                    byte[] clearMessage = HelperFunctionsCrypto.decryptWithRSA(HelperFunctionsStringByteEncoding.string2byte(encryptedMessage), privateKey);
                    showCustomDialog(new String(clearMessage, StandardCharsets.UTF_8));
                }
                else
                {
                    Toast.makeText(DecryptEnterPasswordActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void showCustomDialog(String clearMessage)
    {
        Log.i("KeyListActivity", clearMessage);

        dialog = new Dialog(DecryptEnterPasswordActivity.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.encodedmessage_dialog);

        //Initializing the views of the dialog.
        final TextView textView = dialog.findViewById(R.id.message_display);
        Button closeButton = dialog.findViewById(R.id.close_button);

        textView.setText(clearMessage);

        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(DecryptEnterPasswordActivity.this, MainActivity.class);
                dialog.dismiss();
                startActivity(intent);
                DecryptEnterPasswordActivity.this.finish();
            }
        });
    }
}