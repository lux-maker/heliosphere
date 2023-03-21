package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
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

public class KeyListActivity extends AppCompatActivity {

    ListView listView;
    String clearMessage;
    PublicKey publicKey = null;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_list);

        listView = (ListView) findViewById(R.id.list_view);

        Intent intent = getIntent();
        clearMessage = intent.getStringExtra("clearMessage");

        //######################################### only for debugging
        //load the own public RSA key into the list
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "AccessKey");

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(HelperFunctionsStringByteEncoding.string2byte(settings.getString("RSAPublic", "")));
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            Log.e("DecryptEnterPasswordActivity", "RSA key decoding failure", e);
        }
        //######################################### only for debugging END

        String[] keys = {"own key", "fake key name 1", "fake key name 2", "fake key name 3"}; //TODO load existing keys from memory

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Log.i("KeyListActivity", "test");
                // for now just use the own public key no matter what the selection actually was //TODO add key selection
                byte[] encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(clearMessage.getBytes(StandardCharsets.UTF_8), publicKey);
                //byte[] encryptedMessageBytes = HelperFunctionsCrypto.encryptWithRSA(HelperFunctionsStringByteEncoding.string2byte(clearMessage), publicKey);

                showCustomDialog(HelperFunctionsStringByteEncoding.byte2string(encryptedMessageBytes));
            }
        });
    }

    void showCustomDialog(String encryptedMessage) {
        Log.i("KeyListActivity", encryptedMessage);

        dialog = new Dialog(KeyListActivity.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.qrcode_dialog);

        //Initializing the views of the dialog.
        final ImageView imageCode = dialog.findViewById(R.id.imageCode);
        Button closeButton = dialog.findViewById(R.id.close_button);

        //initializing MultiFormatWriter for QR code generation
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //BitMatrix class to encode entered text and set Width & Height
            BitMatrix matrix = multiFormatWriter.encode(encryptedMessage, BarcodeFormat.QR_CODE, 400, 400);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(matrix);//creating bitmap of code
            imageCode.setImageBitmap(bitmap);//Setting generated QR code to imageView
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(KeyListActivity.this, MainActivity.class);
                dialog.dismiss();
                startActivity(intent);
                KeyListActivity.this.finish();
            }
        });
    }
}