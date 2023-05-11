package com.javetest.helio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

/**
 * generelle Infos
 * Diese Aktivity wird von der Scan Avtivity aufgerufen.
 * Zuvor lief die Scan Aktivtiy, die bereits den public key eingescannt hat und diesen mit dem Intent mitgegeben.
 * Hier wird ein Name für den key abgefragt und dann in den Shared Preferences abgespeichert.
 *
 * Abschließend wird zur Main Activtiy weitergeleitet.
 */

public class KeyExchangeSavePublicKeyActivty extends AppCompatActivity {


    String publickey;
    Button buttonsave;

    EditText enteredKeyName;

    int width;
    ImageView imageCode;

    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activtiy_key_exchange_savepublickey); //created by default

        String publickey = getIntent().getStringExtra("encryptedMessage"); //Der Inhalt des QR-Codes wurde angehängt bei dem Intent von der Scan Aktivity

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //zurückbutton initialisieren

        // assign view objects to code variables
        buttonsave = (Button) findViewById(R.id.save); //objekt, was sich auf den "show other public key" button aus dem GUI bezieht
        imageCode = (ImageView) findViewById(R.id.imageCode); // Objekt bezieht sich auf ImageView im GUI, hier soll der QR-Code angezeigt werden
        enteredKeyName = (EditText) findViewById(R.id.keyname); //objekt, was sich auf das erste Textfeld im GUI bezieht, wo der Name eingeben werden soll

        this.showQR(publickey); //zeige den gescannten QR nochmal an, zur Überprüfung und Abgleich für den User -> Wurde der richtige QR-Code eingescannt?



        //implement a Callable for a "Save" button
        buttonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) //when "SAVE" button clicked: prüfe ob ein name eingegeben wurde, wenn ja 1. Lade die public key map 2. füge den key unter dem Namen hinzu 3. speichere die public key Map wieder in den Shared Preferences 4. Schließe alles und gehe zur Main Activity.
            {
                String keyname = enteredKeyName.getText().toString();
                if(keyname.equals(""))
                {
                    Toast.makeText(KeyExchangeSavePublicKeyActivty.this, "no name assigned", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //load public keys from shared preferences
                    MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
                    SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");
                    SharedPreferences.Editor editor = settings.edit(); //damit wir die SahredPreferences bearbeiten können.

                    //load the public key map, an instance that maps key names to public keys
                    String publicKeyMapJson = settings.getString("publicKeyMap",""); //if preference does not exist, return ""

                    //check if keymap contains any keys
                    if (publicKeyMapJson.equals(""))
                    {
                        // no public keys exists -> since the own public key is added in CreatePasswordActivity, this can actually never happen if everything goes as planned
                        //TODO how to handle this situation?
                    }
                    //if public key map exists in shared preferences, parse the string to a HashMap object

                    HashMap<String, String> publicKeyMap = GsonHelper.fromJson(publicKeyMapJson, new TypeToken<HashMap<String, String>>(){}.getType()); //enthält alle public keys jeweils mit dem key namen indiziert

                    //public key zur HashMap hinzufügen:
                    publicKeyMap.put(keyname, publickey);

                    //HasHMap wieder zu Json machen und dann in den SharedPreferences ablegen.
                    String publicKeyMapJson2 = GsonHelper.toJson(publicKeyMap); //serialize public key as json
                    editor.putString("publicKeyMap", publicKeyMapJson2);
                    editor.apply();

                    //Gebe Erfolgsnachricht aus
                    Toast.makeText(KeyExchangeSavePublicKeyActivty.this, "key stored", Toast.LENGTH_SHORT).show();

                    //gehe zurück zu Main Aktivity
                    Intent intent = new Intent(KeyExchangeSavePublicKeyActivty.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }

        });
    }

    //erzeuge einen QR-Code und zeige ihn im GUI an
    public void showQR(String keyname) {

        //Herausbekommen der Breite des Bildschirms für den QR-Code
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        imageCode.getLayoutParams().width = width;
        imageCode.getLayoutParams().height = width; //weil quadratisch width = height

        //initializing MultiFormatWriter for QR code generation
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //BitMatrix class to encode entered text and set Width & Height
            BitMatrix matrix = multiFormatWriter.encode(keyname, BarcodeFormat.QR_CODE, width, width); //weil quadratisch width = height

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Zurückbutton Richtung festlegen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(KeyExchangeSavePublicKeyActivty.this, KeyExchangeDecisionActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
