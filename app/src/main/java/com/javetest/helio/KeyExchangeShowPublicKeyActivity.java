package com.javetest.helio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

/**
 * generelle Infos
 * Diese Activity wird von der KeyExchangeDecision Activity aufgerufen, wenn dort auf den "show Own Public Key" button gedrückt wird.
 * Es kann eine Message bei dem Intent übergeben werden "keyname", die spezifiziert, welcher public key als QR-Code angezeigt werden soll.
 * Hier soll der der eigene public Key, gespeicherte groß angezeigt werden, damit er von einem anderen Handy abgescannt werden kann.
 * Außerdem können alle anderen gespeicherten public key angeziegt werden.
 *
 * Wird keine Message überbegeben, so wird standardmäßig der eigene public key angezeigt.
 * Wird auf den show other public key button gedrückt, ändert sich das GUI der Activity von activity_key_exchange_schowpublickey zu activtiy_key_list!
 * Dort wird dann die Liste mit public keys angezeigt. Wurde hier eine Auswahl getroffen, ruft die Activity sich selbst mit einem neuen Intent wieder auf
 * und übergibt dabei die Auswahl (=also welcher key angezeigt werden soll) als message zum Intent.
 *
 */
public class KeyExchangeShowPublicKeyActivity extends AppCompatActivity {

    Button buttonshowOtherPublicKey;
    SharedPreferences settings;
    HashMap<String,String> publicKeyMap;
    int width;
    ImageView imageCode;
    ArrayAdapter<String> listAdapter;
    String public_keyname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activtiy_key_exchange_showpublickey); //created by default GUI, wo QR-Code und Button angezeigt werden

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //zurückbutton initialisieren

        Intent intent = getIntent();
        public_keyname = intent.getStringExtra("keyname"); //übergebener Text, der spezifiziert, welchen public key wir anzeigen sollen.
        if (public_keyname == null) { //wenn kein Text übergeben wurde, kommt der Intent von der Main Activity, dann soll zunächst erstmal der eigene public key angezeigt werden.
            public_keyname = "own key";
        }

        // assign view objects to code variables
        buttonshowOtherPublicKey = (Button) findViewById(R.id.showOtherPublicKey); //objekt, was sich auf den "show other public key" button aus dem GUI bezieht
        imageCode = (ImageView) findViewById(R.id.imageCode); // Objekt bezieht sich auf ImageView im GUI, hier soll der QR-Code angezeigt werden


        //load password from memory
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");

        publicKeyMap = new HashMap<String,String>();
        String json = (settings.getString("publicKeyMap", ""));
        publicKeyMap = GsonHelper.fromJson(json,HashMap.class); //aus json wieder in Objekt umwandeln

        //QR-Code anzeigen
        this.showQR(public_keyname); //zeige zunächst den eigenen public key an, sonst ist die eingabe aus dem Intent in dem String public_keyname übergeben.

        //add all keys from the hashmap to a string array and include the resulting array in the ArrayAdapter ( to display it on screen in iniScanbutton().onClick() )
        //das List zeug kommmt jetzt schon weil this beim ArrayAdapter nicht mehr in der onClick methode funktioniert. (dann kann ich nicht mehr this übergeben)
        String[] keys = publicKeyMap.keySet().toArray(new String[0]);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);
        //implement a Callable for a "Scan other key" button click
        this.iniScanbutton();
    }

    //initialisiere den "Scan other key" button und zeige eine Liste an public keys an, wenn der Button gedrückt wird. Nach der Auswahl des public keys wird erneut diese Activity gestartet und die Auswahl als message übergeben.
    public void iniScanbutton() {
        //implement a Callable for a "Scan other key" button click
        buttonshowOtherPublicKey.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when click:
            {
                //Key List Auswahl anzeigen
                setContentView(R.layout.activity_key_list); //GUI Umstellen auf List-anzeige
                ListView listView = (ListView) findViewById(R.id.list_view); //objekt, was sich auf den ListView aus dem GUI bezieht
                //add all keys from the hashmap to a string array and include the resulting array in the ArrayAdapter to display it on screen
                //der ArrayAdapter wurde schon in onCreate() gebildet.
                listView.setAdapter(listAdapter); //Zeigt die public keys in List Viewer in der APP an.

                //implement a Callable for the List
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        Log.i("KeyListActivity", "test");
                        // get the name of the key that was selected
                        String itemName = listView.getItemAtPosition(position).toString();
                        String publicKeyString = publicKeyMap.get(itemName);

                        //rufe diese Activtiy von vorne auf und übergebe dabei als message den namen des neuen public keys, welcher angezeigt werden soll
                        Intent intent = new Intent(KeyExchangeShowPublicKeyActivity.this, KeyExchangeShowPublicKeyActivity.class);
                        intent.putExtra("keyname", itemName);
                        startActivity(intent);
                        finish();
                    }
                });
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
            BitMatrix matrix = multiFormatWriter.encode(publicKeyMap.get(keyname), BarcodeFormat.QR_CODE, width, width); //weil quadratisch width = height

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Zurückbutton richtung festlegen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(KeyExchangeShowPublicKeyActivity.this, KeyExchangeDecisionActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
