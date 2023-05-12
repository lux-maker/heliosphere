package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

/**
 * generelle Infos:
 * Die ComposeMessage Activity übergibt mit dem Parameter "clearMessage" den zuvor eingegeben Text und startet diese Activity hier.
 * Dann wird eine Liste an gespeicherten public keys geladen. Nach der Auswahl, wird damit der Text verschlüsselt und anschließend als QR-Code angezeigt.
 * Zwischendurch gibt es einen Wechsel beim GUI: die Liste wird im activity_key_list.xml angezeigt, der QR-Code im qrcode_dialog.xml
 * Wird der Dialog mit dem QR-Code geschlossen, wird die Main Activity wieder gestartet.
 */
public class KeyListActivity extends AppCompatActivity {

    ListView listView;
    String clearMessage;
    PublicKey publicKey = null;
    Dialog dialog;

    int width;
    MessageSplittingHandler messageSplittingHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_key_list); //created by default

        // assign view objects to code variables
        listView = (ListView) findViewById(R.id.list_view); //objekt, was sich auf den ListView aus dem GUI bezieht

        Intent intent = getIntent();
        clearMessage = intent.getStringExtra("clearMessage"); //zuvor eingegebener Text aus CopmposeMessage Activity

        //load public keys from shared preferences
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");

        //load the public key map, an instance that maps key names to public keys
        String publicKeyMapJson = settings.getString("publicKeyMap",""); //if preference does not exist, return ""

        //check if keymap contains any keys
        if (publicKeyMapJson.equals(""))
        {
            // no public keys exists -> since the own public key is added in CreatePasswordActivity, this can actually never happen if everything goes as planned
            //TODO how to handle this situation? -> catch error and avoid this situation
        }

        //if public key map exists in shared preferences, parse the string to a HashMap object
        HashMap<String, String> publicKeyMap = GsonHelper.fromJson(publicKeyMapJson, new TypeToken<HashMap<String, String>>(){}.getType()); //enthält alle public keys jeweils mit dem key namen indiziert
        //add all keys from the hashmap to a string array and include the resulting array in the ArrayAdapter to display it on screen
        String[] keys = publicKeyMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);
        listView.setAdapter(listAdapter); //Zeigt die public keys in List Viewer in der APP an.


        //implement a Callable for the List
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Log.i("KeyListActivity", "test");
                // get the name of the key that was selected
                String itemName = listView.getItemAtPosition(position).toString();
                String publicKeyString = publicKeyMap.get(itemName);

                try {
                    //entschlüsseln des Hash-Wertes -> herauslesen des öffentlichen Schlüssels
                    //(paule) verstehe das hier noch nicht komplett. Also was sagt genau jede Zeile dieser nächsten 3?
                    //(lux) die java libvrary für RSA verschlüsselung arbeitet mit public keys die nach dem x509 schema encoded sind,
                    // die klasse EncodedKeySpec hält den schlüssel im richtigen format bereit
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(HelperFunctionsStringByteEncoding.string2byte(publicKeyString)); //public key muss zunächst entschlüsselt werden mit Base64
                    publicKey = keyFactory.generatePublic(publicKeySpec);
                } catch (Exception e) {
                    Log.i("Keylistactivity", "RSA key decoding failure", e); //logging, when an error occurs
                }

                Log.i("Keylistactivity - clear message", clearMessage);

                //+++++++++++message splitting+++++++++++
                //Message splitting for multi QR-codes
                MessageSplittingHandler messageSplittingHandler = new MessageSplittingHandler(50, 20); //use the values (maxNumberOfJunks = 13 /chars = 300) of the class //TODO genaue Char anzahl für max RSA block herausfinden
                messageSplittingHandler.loadMessage(clearMessage);
                int requiredNumberOfJunks = messageSplittingHandler.getRequiredNumberOfJunks();
                //debug
                Log.i("KeyListActivity - requiredNumberOfJunks",  Integer.toString(requiredNumberOfJunks) );

                //verschlüsseln der Nachricht mit dem öffentlichen Schlüssel in den einzelnen Junks
                String mergedencryptedMessage = "";
                byte[] encryptedMessageBytes = null;
                for (int i = 0; i < requiredNumberOfJunks; i++){
                    encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(messageSplittingHandler.getMessageJunkAtPosition(i, false).getBytes(StandardCharsets.UTF_8), publicKey);
                    //byte[] encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(HelperFunctionsStringByteEncoding.string2byte(clearMessage), publicKey);
                    Log.i("KeyListActivity - Junks",  encryptedMessageBytes + " ...i: " + i);
                    mergedencryptedMessage = mergedencryptedMessage + HelperFunctionsStringByteEncoding.byte2string(encryptedMessageBytes);
                   // Log.i("KeyListActivity - merged Junks encrypted",  mergedencryptedMessage  + " ...i: " + i);

                }
                Log.i("KeyListActivity - merged Junks encrypted final",  mergedencryptedMessage + "");



                //ruft die nächste Methode dieser Klasse auf und übergibt die Verschlüsselte Nachricht
                showCustomDialog(mergedencryptedMessage);
            }
        });
    }

    //Zeige verschlüsslte Nachricht in einem QR Code an:
    //Achtung GUI wird gewechselt: activity_key_list.xml -> qrcode_dialog.xml
    void showCustomDialog(String encryptedMessage) {
        Log.i("KeyListActivity", encryptedMessage);
        setContentView(R.layout.qrcode_dialog); //created by default GUI, wo QR-Code und Button angezeigt werden

        // **for debugging only:**
        //encryptedMessage = new String(new char[1000]).replace('\0', ' ');
        // ****

        //Initializing the views of the dialog.
        final ImageView imageCode = (ImageView) findViewById(R.id.imageCode); // Objekt bezieht sich auf ImageView im GUI
        TextView qrStatusText = (TextView) findViewById(R.id.qrStatusText); // Bezieht sich auf Textfeld im GUI, dort soll: encyrpted and encoded message /n 1 von 3 angeziegt werden
        Button closeButton = (Button) findViewById(R.id.close_button); // Objekt bezieht sich auf "CLOSE CODE AND MESSAGE" button im GUI
        Button nextqrButton = (Button) findViewById(R.id.nextqr_button); // Objekt bezieht sich auf "next QR Button" button im GUI

        //Herausbekommen der Breite des Bildschirms für den QR-Code
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        imageCode.getLayoutParams().width = width;
        imageCode.getLayoutParams().height = width; //weil quadratisch width = height


        //Anzeigen in verschiedenen QR codes
        //+++++++++++message splitting+++++++++++
        //Message splitting for multi QR-codes
        messageSplittingHandler = new MessageSplittingHandler(13, 500); //use the values (maxNumberOfJunks = 13 /chars = 300) of the class //TODO genaue Char anzahl für max RSA block herausfinden
        messageSplittingHandler.loadMessage(encryptedMessage);
        int requiredNumberOfJunks = messageSplittingHandler.getRequiredNumberOfJunks();

        //debug
        Log.i("KeyListActivity - QR - requiredNumberOfJunks",  Integer.toString(requiredNumberOfJunks) );

        //zeige den erste QR-Code an:
        showQRCode(0, imageCode, qrStatusText);


        nextqrButton.setOnClickListener(new View.OnClickListener() {
            int runde = 1;

            public void onClick(View view) {

                if (runde < messageSplittingHandler.getRequiredNumberOfJunks()){

                    showQRCode(runde, imageCode, qrStatusText); //zeige den QR-Code mit dem jeweiligen Junk an
                    runde++; //springe einen QR Code weiter fürs nächste mal klicken

                } else {
                    Toast.makeText(KeyListActivity.this, "Alle QR Codes durch", Toast.LENGTH_SHORT).show();

                }


            }
        });

        //dialog.show();

        //implement a Callable for a "CLOSE CODE AND MESSAGE" button click
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when clicked: starte wieder die Main Activity
            {
                Intent intent = new Intent(KeyListActivity.this, MainActivity.class);
                startActivity(intent);
                KeyListActivity.this.finish();
            }
        });
    }

    void showQRCode(int qrindex, ImageView imageCode, TextView qrStatusText) {
        //initializing MultiFormatWriter for QR code generation
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //BitMatrix class to encode entered text and set Width & Height
                    BitMatrix matrix = multiFormatWriter.encode(messageSplittingHandler.getMessageJunkAtPosition(qrindex, true), BarcodeFormat.QR_CODE, width, width); //weil quadratisch width = height

            Log.i("KeyListActivity - qr text: ", messageSplittingHandler.getMessageJunkAtPosition(qrindex, true)+ "");
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView


            qrStatusText.setText("encrypted and encoded message\n QR-Code " + Integer.toString(qrindex+1) +" out of " + Integer.toString(messageSplittingHandler.getRequiredNumberOfJunks()-1));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}