package com.javetest.helio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

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

    String redirection;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_scan); //created by default

        Intent intent_extra = getIntent();
        redirection = intent_extra.getStringExtra("redirection"); //übergebener Text, der spezifiziert, wohin die Nachricht aus dem QR Code gehen soll
        if (redirection == null) {
         //TODO was machen wir hier?
            //ich würde sagen das fangen wir als Fehler ab und bauen den Code so dass dieser Fall nicht eintritt
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //zurückbutton initialisieren

        CodeScannerView scannerView = findViewById(R.id.scanner_view); //Objekt bezieht sich auf com.budiyev.android.codescanner.CodeScannerView aus dem GUI
        mCodeScanner = new CodeScanner(this, scannerView);

        //override the callback that is executed when a QR code is scanned and decoded
        mCodeScanner.setDecodeCallback(new DecodeCallback()
        {
            @Override
            public void onDecoded(@NonNull final Result result)
            {
                //this method is called after QR code is decoded -> decoded message contained in result

                //Auswahl zur Weiterleitung basierend auf der Herkunft des INTENTS
                if (redirection.equals("DecryptEnterPasswordActivity")) {
                    //start the DecryptEnterPasswordActivity to start decryption displaying of the message and
                    intent = new Intent(ScanActivity.this, DecryptEnterPasswordActivity.class);
                }
                if (redirection.equals("KeyExchangeSavePublicKeyActivty")) {
                    //start the KeyExchangeSavePublicKeyActivty to save the public key
                    intent = new Intent(ScanActivity.this, KeyExchangeSavePublicKeyActivty.class);
                }
                intent.putExtra("encryptedMessage", result.getText());
                startActivity(intent);
                ScanActivity.this.finish();
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
                if (redirection.equals("DecryptEnterPasswordActivity")) {
                    //start the DecryptEnterPasswordActivity to start decryption displaying of the message and
                    intent = new Intent(ScanActivity.this, MainActivity.class);
                }
                if (redirection.equals("KeyExchangeSavePublicKeyActivty")) {
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
