package com.javetest.helio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

/**
 * generelle Infos:
 * Die Activity wird von der Main Activity gestartet, wenn dort auf den Scan and decrypt message button gedrückt wird.
 * Wenn ein QR-Code eingescannt wurde, wird die DecryptEnterPasswort Activity gestartet und die Nachricht aus dem QR-Code übergeben.
 *
 * (paule) wo genau wird der QR-Code reader gestartet?
 */
public class ScanActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_scan); //created by default

        CodeScannerView scannerView = findViewById(R.id.scanner_view); //Objekt bezieht sich auf com.budiyev.android.codescanner.CodeScannerView aus dem GUI
        mCodeScanner = new CodeScanner(this, scannerView);

        //override the callback that is executed when a QR code is scanned and decoded
        mCodeScanner.setDecodeCallback(new DecodeCallback()
        {
            @Override
            public void onDecoded(@NonNull final Result result)
            {
                //this method is called after QR code is decoded -> decoded message contained in result
                //start the KeyListActivity so that the user can chose the right key
                Intent intent = new Intent(ScanActivity.this, DecryptEnterPasswordActivity.class);
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
}
