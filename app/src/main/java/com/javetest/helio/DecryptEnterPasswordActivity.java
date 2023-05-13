package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import javax.crypto.spec.SecretKeySpec;

/**
 * generelle Infos:
 * Die Activity wird von der Scan Activity gestartet. Die Scan Actvity hat den QR-Code bereits eingescannt und den Inhalt mit übergeben.
 * Nun wird nach dem App-PW gefragt und die Hash-Werte geladen und verglichen. Außerdem wird noch der private key aus den shared Preferences geladen.
 * Wenn das PW richtig ist, wird dieser entschlüsselt und damit die Nachricht entschlüsselt.
 * Diese wird dann in einem Textfeld ausgegeben.
 * Zwischendurch gibt es einen Wechsel beim GUI: die PW-Abfrage wird im activity_decrypt_enter_password.xml angezeigt, die Nachricht im encodedmessage_dialog.xml
 * Wird der Dialog durch den "CLOSE CODE AND MESSAGE" button geschlossen, wird die Main Activity wieder gestartet.
 */
public class DecryptEnterPasswordActivity extends AppCompatActivity {
    EditText enteredPW;
    Button button;

    SharedPreferences settings;
    HashedPasswordInfo hashedPasswordInfo;
    PrivateKey privateKey = null;
    HashMap<String, byte[]> privateKeyEncrypted;

    Dialog dialog;

    String encryptedMessage;
    String assembledClearMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_decrypt_enter_password); //created by default

        // assign view objects to code variables
        enteredPW = (EditText) findViewById(R.id.password); //objekt, was sich auf das Textfeld im GUI bezieht, darüber steht "Enter password..", inputType="numberPassword"
        button = (Button) findViewById(R.id.button2); //objekt, was sich auf den "ENTER" button im GUI bezieht.

        //run the loading of passwords on a different thread to preserve frame rate
        new Thread(new Runnable() {
            public void run() {

                //load password from memory
                MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");

                //load true hashed password
                String json = (settings.getString("hashedPWInfo", ""));
                hashedPasswordInfo = GsonHelper.String2HashedPWInfo(json);

                //load RSA key from memory
                String jsonKey = settings.getString("RSAPrivate", "");
                Type type = new TypeToken<HashMap<String, byte[]>>(){}.getType();
                privateKeyEncrypted = GsonHelper.fromJson(jsonKey, type);
            }
        }).start();

        //implement a Callable for "ENTER" button click
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when clicked: hash pw, vergleiche Hash-Wert mit App-PW-Hash-Wert. Richtiges PW: entschlüsseln der Nachricht und übergeben an showCustomDialog()
            {

                //auslagern in externen Thread
                Thread calculationThread = new Thread(pwcheck);
                calculationThread.start();
                setContentView(R.layout.activity_first_acess_decision_acitivty); //zeige den lade... screen

            }
        });
    }

    //Anzeigen der entschlüsselten Nachricht
    //Achtung das GUI wechselt: activity_decrypt_enter_password.xml -> encodedmessage_dialog.xml
    void showCustomDialog(String clearMessage)
    {
        Log.i("DecryptedEnterPasswordAcitivity - show dialog", clearMessage);

        //erzeuge neues Dialog-Fenster (Objekt):
        dialog = new Dialog(DecryptEnterPasswordActivity.this);
        //We have added a title in the custom layout. So let's disable the default title:
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.encodedmessage_dialog); //bezieht sich auf encodedmessage_dialog.xml

        //Initializing the views of the dialog.
        final TextView textView = dialog.findViewById(R.id.message_display); // Objekt bezieht sich auf TextView im GUI
        Button closeButton = dialog.findViewById(R.id.close_button); // Objekt bezieht sich auf "CLOSE CODE AND MESSAGE" button im GUI

        textView.setText(clearMessage);

        dialog.show();

        //implement a Callable for a "CLOSE CODE AND MESSAGE" button click
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when clicked: starte wieder die Main Activity
            {
                Intent intent = new Intent(DecryptEnterPasswordActivity.this, MainActivity.class);
                dialog.dismiss();
                startActivity(intent);
                DecryptEnterPasswordActivity.this.finish();
            }
        });
    }

    Runnable pwcheck = new Runnable() {

        public void run() {

            //hash entered password
            String p = enteredPW.getText().toString();
            HashedPasswordInfo enteredHashedPasswordInfo = HelperFunctionsCrypto.hashPassword(hashedPasswordInfo.getSalt(), p.toCharArray());

            //load encrypted message json from Intent
            encryptedMessage = getIntent().getStringExtra("encryptedMessage");

            //compare passwords
            if (hashedPasswordInfo.equals(enteredHashedPasswordInfo))
            {
                //reset failedAttempts
                PasswordAttemptsHandler.setCurrentFailedAttemptsCounter(getApplicationContext(), 0);

                //passwords match -> start to decrypt message
                Log.i("DecryptEnterPasswordActivity", "passwords match");

                //decrypt private RSA key
                try
                {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(HelperFunctionsCrypto.decryptBytes(privateKeyEncrypted, p.toCharArray())); //zunächst muss der privtae key mit Base64 entschlüsselt werden
                    privateKey = keyFactory.generatePrivate(privateKeySpec);
                }
                catch(Exception e)
                {
                    Log.e("DecryptEnterPasswordActivity", "RSA key decoding failure", e);
                }


                //parse the json string into a String array

                String[] rsaBlocks = GsonHelper.fromJson(encryptedMessage, new TypeToken<String[]>(){}.getType());

                int i = 0;
                for (String rsaBlock : rsaBlocks)
                {
                    Log.i("DecrpytEnterPasswordActivity", "Block " + i + rsaBlock);
                    i++;
                }

                assembledClearMessage = "";

                //iterate through all rsa blocks and decrypt them one by one
                for (String rsaBlock : rsaBlocks)
                {
                    try
                    {
                        byte[] clearMessageChunk = HelperFunctionsCrypto.decryptWithRSA(HelperFunctionsStringByteEncoding.string2byte(rsaBlock), privateKey);
                        assembledClearMessage = assembledClearMessage + new String(clearMessageChunk, StandardCharsets.UTF_8);
                    }
                    catch(Exception e)
                    {
                        //fehler, wenn rsa entschlüsselung nicht geklappt hat anzeigen:
                        DecryptEnterPasswordActivity.this.runOnUiThread(() -> Toast.makeText(DecryptEnterPasswordActivity.this,"Decryption failed. Check whether you just scanned the right QR code and that it has been created for you! ", Toast.LENGTH_LONG).show());
                        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                        intent.putExtra("redirection", "DecryptEnterPasswordActivity"); //nach dem Scan des QR codes soll dannach mit dem Inhalt die DecryptEnterPassword Activity aufgerufen werden.
                        startActivity(intent);
                        finish();

                    }
                }

                //display the message
                //lamda Ausdruck, weil das Dialog feld als Teil des GUIs nicht vom externen Thread angesprochen werden kann. Deswegen wird damit der Befehl über den Main Thread ausgeführt.
                DecryptEnterPasswordActivity.this.runOnUiThread(() -> showCustomDialog(assembledClearMessage));
            }
            else
            {
                int failedAttempts = PasswordAttemptsHandler.getCurrentFailedAttemptsCounter(getApplicationContext());
                ++failedAttempts;

                PasswordAttemptsHandler.setCurrentFailedAttemptsCounter(getApplicationContext(), failedAttempts);

                //check if the maximum number if attempts is reached and dekete everything if so
                if (failedAttempts >= PasswordAttemptsHandler.getMaxAllowedNumOfFailedAttempts())
                {
                    TotalAnnilihator totalAnnilihator = new TotalAnnilihator();
                    totalAnnilihator.clearAll(getApplicationContext());

                    //lamda Ausdruck, weil wir sonst nicht vom externen Thread auf das GUI zugegriffen werden kann. Das kann nur über den Main Thread geändert werden.
                    DecryptEnterPasswordActivity.this.runOnUiThread(() -> Toast.makeText(DecryptEnterPasswordActivity.this,"Maximum amount of failed Attempts reached. The application was reset. All keys were deleted.", Toast.LENGTH_LONG).show());
                    
                    Intent intent = new Intent(getApplicationContext(), FirstAcessDecisionAcitivty.class);
                    startActivity(intent);
                    finish();
                }

                //lamda Ausdruck, weil wir sonst nicht vom externen Thread auf das GUI zugegriffen werden kann. Das kann nur über den Main Thread geändert werden.
                DecryptEnterPasswordActivity.this.runOnUiThread(() -> Toast.makeText(DecryptEnterPasswordActivity.this,"Wrong password. " + Integer.toString(PasswordAttemptsHandler.getLeftFailedAttempts(getApplicationContext())) + " Attempts left until the application will reset and all keys will be deleted", Toast.LENGTH_LONG).show());

                Intent intent = new Intent(DecryptEnterPasswordActivity.this, DecryptEnterPasswordActivity.class); //starte erneut die selbe Activity zur erneuten PW eingabe
                intent.putExtra("encryptedMessage", encryptedMessage);
                startActivity(intent);
                return;

            }

        }
    };
}