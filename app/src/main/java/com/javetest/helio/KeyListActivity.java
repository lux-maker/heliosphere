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
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

        //load the public key map, an instance that maps key names to public keys
        String publicKeyMapJson = settings.getString("publicKeyMap",""); //if preference does not exist, return ""

        //check if keymap contains any keys
        if (publicKeyMapJson.equals(""))
        {
            // no public keys exists -> since the own public key is added in CreatePasswordActivity, this can actually never happen if everything goes as planned
            //TODO how to handle this situation?
        }
        //if public key map exists in shared preferences, parse the string to a HashMap object
        Gson gson = new Gson();
        HashMap<String, String> publicKeyMap = gson.fromJson(publicKeyMapJson, new TypeToken<HashMap<String, String>>(){}.getType()); //enthält alle public keys jeweils mit dem key namen indiziert
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
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(HelperFunctionsStringByteEncoding.string2byte(publicKeyString)); //public key muss zunächst entschlüsselt werden mit Base64
                    publicKey = keyFactory.generatePublic(publicKeySpec);
                } catch (Exception e) {
                    Log.e("Keylistactivity", "RSA key decoding failure", e); //logging, when an error occurs
                }

                //verschlüsseln der Nachricht mit dem öffentlichen Schlüssel
                byte[] encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(clearMessage.getBytes(StandardCharsets.UTF_8), publicKey);
                //byte[] encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(HelperFunctionsStringByteEncoding.string2byte(clearMessage), publicKey);

                //ruft die nächste Methode dieser Klasse auf und übergibt die Verschlüsselte Nachricht
                showCustomDialog(HelperFunctionsStringByteEncoding.byte2string(encryptedMessageBytes));
            }
        });
    }

    //Zeige verschlüsslte Nachricht in einem QR Code an:
    //Achtung GUI wird gewechselt: activity_key_list.xml -> qrcode_dialog.xml
    void showCustomDialog(String encryptedMessage) {
        Log.i("KeyListActivity", encryptedMessage);

        //erzeuge neues Dialog-Fenster (Objekt):
        dialog = new Dialog(KeyListActivity.this);
        //We have added a title in the custom layout. So let's disable the default title:
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog:
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog:
        dialog.setContentView(R.layout.qrcode_dialog); //bezieht sich auf qrcode_dialog.xml

        //Initializing the views of the dialog.
        final ImageView imageCode = dialog.findViewById(R.id.imageCode); // Objekt bezieht sich auf ImageView im GUI
        Button closeButton = dialog.findViewById(R.id.close_button); // Objekt bezieht sich auf "CLOSE CODE AND MESSAGE" button im GUI

        //Herausbekommen der Breite des Bildschirms für den QR-Code
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        imageCode.getLayoutParams().width = width;
        imageCode.getLayoutParams().height = width; //weil quadratisch width = height


        //initializing MultiFormatWriter for QR code generation
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //BitMatrix class to encode entered text and set Width & Height
            BitMatrix matrix = multiFormatWriter.encode(encryptedMessage, BarcodeFormat.QR_CODE, width, width); //weil quadratisch width = height

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.show();

        //implement a Callable for a "CLOSE CODE AND MESSAGE" button click
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when clicked: starte wieder die Main Activity
            {
                Intent intent = new Intent(KeyListActivity.this, MainActivity.class);
                dialog.dismiss();
                startActivity(intent);
                KeyListActivity.this.finish();
            }
        });
    }
}