package com.javetest.helio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * generelle Infos
 * Diese Activity wird aufgerufen, wenn bei der Main Activity auf Scan key gedrückt wird.
 * Hier gibt es zwei Buttons und es wird entschieden, ob du deinen öffentlichen Schlüssel anzeigen möchtest oder einen externen öffentlichen Schlüssel einscannen möchtest.
 */
public class KeyExchangeDecisionActivity extends AppCompatActivity {

    Button buttonshowOwnPublicKey, buttonscanExternalKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_key_exchange_decision); //created by default

        buttonshowOwnPublicKey = (Button) findViewById(R.id.showOwnPublicKey); //objekt, was sich auf den "show Own Public Key" button aus dem GUI bezieht
        buttonscanExternalKey = (Button) findViewById(R.id.scanExternalKey); //objekt, was sich auf den "scan External Key" button aus dem GUI bezieht


        //implement a Callable for a "show Own Public Key" button click
        buttonshowOwnPublicKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) //when click: starte Scan Activity
            {

                Intent intent = new Intent(getApplicationContext(), KeyExchangeShowPublicKeyActivity.class);
                startActivity(intent);
                finish();


            }
        });

        //implement a Callable for a "scan External Key" button click
        buttonscanExternalKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) //when click: starte Scan Activity
            {
                /*
                Intent intent = new Intent(getApplicationContext(), Activity.class);
                startActivity(intent);
                finish();
                //beim nächsten mal weitermachen TODO
                 */
            }
        });

    }

}
