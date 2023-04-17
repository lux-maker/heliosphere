package com.javetest.helio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

/**
 * generelle Infos
 * Diese Activity wird von der KeyExchangeDecision Activity aufgerufen, wenn dort auf den "show Own Public Key" button gedrückt wird.
 * Hier soll der der eigene public Key groß angezeigt werden, damit er von einem anderen Handy abgescannt werden kann.
 *
 * Außerdem können alle anderen gespeicherten public key angeziegt werden.
 */
public class KeyExchangeShowPublicKeyActivity extends AppCompatActivity {

    Button buttonshowOtherPublicKey;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activtiy_key_exchange_showpublickey); //created by default

        buttonshowOtherPublicKey = (Button) findViewById(R.id.showOtherPublicKey); //objekt, was sich auf den "show other public key" button aus dem GUI bezieht
        ImageView imageCode = (ImageView) findViewById(R.id.imageCode); // Objekt bezieht sich auf ImageView im GUI

        //load password from memory
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");


        HashMap<String,String> publicKeyMap = new HashMap<String,String>();
        Gson gson = new Gson();
        String json = (settings.getString("publicKeyMap", ""));
        publicKeyMap = gson.fromJson(json,HashMap.class); //serialize public key as json


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
            BitMatrix matrix = multiFormatWriter.encode(publicKeyMap.get("own key"), BarcodeFormat.QR_CODE, width, width); //weil quadratisch width = height

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView
        } catch (Exception e) {
            e.printStackTrace();
        }


        //implement a Callable for a "Scan key" button click
        buttonshowOtherPublicKey.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when click:
            {
            /* TODO
            Intent intent = new Intent(getApplicationContext(), Activity.class);
            startActivity(intent);
            finish();

             */
            }
        });
    }



}
