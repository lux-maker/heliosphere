package com.javetest.helio;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.w3c.dom.Text;

import java.util.Objects;
import java.util.function.Function;

/**
 * generelle Infos:
 * (1) Die Activity wird von der Main Activity gestartet, wenn dort auf den Scan and decrypt message button gedrückt wird.
 * (2) Oder von der KeyExchangeDecission Akctivtiy, wenn dort auf scan External Key gedrückt wird.
 *
 * In dem INTENT wird immer extra unter "redirection" angehangen, welche Klasse danach als nächstes Aufgerufen werden soll.
 *
 * Weiterleitung
 * (1) Wenn ein QR-Code eingescannt wurde, wird die DecryptEnterPasswort Activity gestartet und die Nachricht aus dem QR-Code übergeben.
 * (2) Wenn ein QR-Code eingescannt wurde, wird die KeyExchangeSavePublicKeyActivtyActivity gestartet und die Nachricht aus dem QR-Code übergeben.
 *
 * (paule) wo genau wird der QR-Code reader gestartet?
 */
public class ScanActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    MessageAssemblyHandler messageAssemblyHandler = new MessageAssemblyHandler();
    String redirectionInfo;
    TextView textView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_scan); //created by default

        textView = (TextView)findViewById(R.id.textView7);

        //fetch the current intent and check where it was created
        Intent intent_extra = getIntent();
        redirectionInfo = intent_extra.getStringExtra("redirection"); //übergebener Text, der spezifiziert, wohin die Nachricht aus dem QR Code gehen soll

        //if redirectionInfo is null, throw error, if the activity is called to scan a key, disbale the buttom textview by displaying an empty string
        if (redirectionInfo == null) {Log.e("ScanActivity onCreate", "Intent String 'redirection' is null");}
        else if (redirectionInfo.equals("KeyExchangeSavePublicKeyActivty")) textView.setText(" ");

        //zurück button initialisieren
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        CodeScannerView scannerView = findViewById(R.id.scanner_view); //Objekt bezieht sich auf com.budiyev.android.codescanner.CodeScannerView aus dem GUI
        mCodeScanner = new CodeScanner(this, scannerView);

        //check if camera permissions are granted
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        //override the callback that is executed when a QR code is scanned and decoded with a lambda function
        mCodeScanner.setDecodeCallback(result -> {

            //check out the current activation to be able to run tasks on the UI thread later (rendering and view modifiations)
            //Activity currentActivity = (Activity) (getApplicationContext());

            //check the redirection information to choose a proper handling
            if (redirectionInfo.equals("KeyExchangeSavePublicKeyActivty")) {
                //start the KeyExchangeSavePublicKeyActivty to save the public key
                intent = new Intent(ScanActivity.this, KeyExchangeSavePublicKeyActivty.class);
                intent.putExtra("encryptedMessage", result.getText());
                startActivity(intent);
                ScanActivity.this.finish();
            }
            else if (redirectionInfo.equals("DecryptEnterPasswordActivity")) {
                // the result contains a message chunk. save it in MessageAssemblyHandler

                boolean allChunksLoaded = false;

                //check if the scan is ok
                try
                {
                    allChunksLoaded = messageAssemblyHandler.loadMessageChunk(result.getText());
                } catch (IllegibleScanException e)
                {
                    ScanActivity.this.runOnUiThread(() -> Toast.makeText(ScanActivity.this, "The QR-Code could not be decoded. Make sure that the QR-Code contains a Heliosphere message chunk and is legible.", Toast.LENGTH_SHORT).show());
                } catch (DoubleScanException e)
                {
                    ScanActivity.this.runOnUiThread(() -> Toast.makeText(ScanActivity.this, "This QR code was already scanned before", Toast.LENGTH_SHORT).show());
                }

                //update the information text on screen
                String infoTextViewContent = "Total number of QR-Codes: " + Integer.toString(messageAssemblyHandler.getTotalNumberOfChunks()) + "\n Missing number of QR-Codes: " + Integer.toString(messageAssemblyHandler.getNumberOfMissingChunks());
                ScanActivity.this.runOnUiThread(() -> textView.setText(infoTextViewContent));

                if (allChunksLoaded)
                {
                    //send the RSA blocks to the decryption activity
                    String[] rsaBlocks = messageAssemblyHandler.getRSABlocks();
                    String rsaBlocksJson = GsonHelper.toJson(rsaBlocks);

                    intent = new Intent(ScanActivity.this, DecryptEnterPasswordActivity.class);
                    intent.putExtra("encryptedMessage", rsaBlocksJson);
                    startActivity(intent);
                    ScanActivity.this.finish();
                }
                else
                {
                    //make sure that the ScanActivity is again brought to the front so that the camera remains active and scanning can be continued
                    Intent resumeScanActivity = new Intent(ScanActivity.this, ScanActivity.class);
                    resumeScanActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityIfNeeded(resumeScanActivity, 0);
                }
            }
            else //if the redirection information is unknown, log an error
            {
                Log.e("ScanActivity onDecode","The intent contains an unknown redirection information.");
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
        {
            //permission not granted
            Toast.makeText(ScanActivity.this, "The application must be granted access to the camera to be operational. Grant access in system settings.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:


                //Auswahl zur Weiterleitung basierend auf der Herkunft des INTENTS
                if (redirectionInfo.equals("DecryptEnterPasswordActivity")) {
                    //start the DecryptEnterPasswordActivity to start decryption displaying of the message and
                    intent = new Intent(ScanActivity.this, MainActivity.class);
                }
                if (redirectionInfo.equals("KeyExchangeSavePublicKeyActivty")) {
                    //start the KeyExchangeSavePublicKeyActivty to save the public key
                    intent = new Intent(ScanActivity.this, KeyExchangeDecisionActivity.class);
                }
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
