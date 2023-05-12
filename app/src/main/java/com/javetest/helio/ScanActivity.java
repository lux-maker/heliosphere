package com.javetest.helio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.w3c.dom.Text;

import java.util.Objects;

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

        //if redirectionInfo is null, throw error
        if (redirectionInfo == null) {Log.e("ScanActivity onCreate", "Intent String 'redirection' is null");}

        //zurück button initialisieren
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        CodeScannerView scannerView = findViewById(R.id.scanner_view); //Objekt bezieht sich auf com.budiyev.android.codescanner.CodeScannerView aus dem GUI
        mCodeScanner = new CodeScanner(this, scannerView);

        //override the callback that is executed when a QR code is scanned and decoded
        mCodeScanner.setDecodeCallback(new DecodeCallback()
        {
            @Override
            public void onDecoded(@NonNull final Result result)
            {
                //this method is called after QR code is decoded -> decoded message contained in result

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
                    boolean allChunksLoaded = messageAssemblyHandler.loadMessageChunk(result.getText());

                    //update the information
                    String infoTextViewContent = "Total number of QR-Codes: " + Integer.toString(messageAssemblyHandler.getTotalNumberOfChunks()) + "\n Missing number of QR-Codes: " + Integer.toString(messageAssemblyHandler.getNumberOfMissingChunks());
                    textView.setText(infoTextViewContent);

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
                }
                else
                {
                    Log.e("ScanActivity onDecode","The intent contains an unknown redirection information.");
                }
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
