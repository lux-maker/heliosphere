package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * generelle Infos:
 * MainActivity wird nicht als erstes beim Starten der App aufgerufen - zunächst wird die FirstAcessDecission Activity angesprochen.
 * Dann muss ein App-Passwort eingegeben werden oder (beim ersten Starten der App) ein App-PW neu festgelegt werden, dabei wird gleich auch ein RSA key pair generiert und alles in den shared Preferences abgespeichert.
 *
 * Wird der Scan and decrypt message button gedrückt, wird die Scan Activity gestartet.
 * Wird der Compose and Encrypt message button gedrückt, wird die ComposeMessage Activity gestartet.

 * Die Main Activity wird nie mit finish(); geschlossen, auch wenn andere Activitys gestartet werden, damit die zurück Pfeiltaste des Handys immernoch zur Main zurück springt.
 * Sonst würde die App komplett geschlossen werden. mit android:launchMode="singleTask" in der AndroidManifest.xml wird festgelegt, dass dabei nicht tausende Instanzen der Activity angelegt werden.

 * TODO Wird der scan key button gedrückt, passiert gerade noch nichts
 * TODO standardmäßig alle Attribute auf private?! zumindest da wo es möglich ist - ist bisher noch nicht so häufig
 */
public class MainActivity extends AppCompatActivity {

    Button buttonScan, buttonCompose, buttonKeyExchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_main); //created by default

        buttonScan = (Button) findViewById(R.id.button3); //objekt, was sich auf den "Scan and decrypt message" button aus dem GUI bezieht
        buttonCompose = (Button) findViewById(R.id.button4); //objekt, was sich auf den "Compose and Encrypt message" button aus dem GUI bezieht
        buttonKeyExchange = (Button) findViewById(R.id.button5); //objekt, was sich auf den "scan key" button aus dem GUI bezieht

        //implement a Callable for a "Scan and decrypt message" button click
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when click: starte Scan Activity
            {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra("redirection", "DecryptEnterPasswordActivity"); //nach dem Scan des QR codes soll dannach mit dem Inhalt die DecryptEnterPassword Activity aufgerufen werden.
                startActivity(intent);
                //finish(); //Wenn wir hier die Activity beenden, hat die Pfeiltaste bei android nichts mehr, wo sie zurück springen könnte und der Pfeil zurück schließt immer die App
                // So würde man mit der Pfeil zurück taste immer in der Main Activity landen.
            }
        });

        //implement a Callable for a "Compose and Encrypt message" button click
        buttonCompose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when click: starte ComposeMessage Activity
            {
                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                startActivity(intent);
                //finish(); //Wenn wir hier die Activity beenden, hat die Pfeiltaste bei android nichts mehr, wo sie zurück springen könnte und der Pfeil zurück schließt immer die App
                // So würde man mit der Pfeil zurück taste immer in der Main Activity landen.
            }
        });

        //implement a Callable for a "Scan key" button click
        buttonKeyExchange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when click: starte KeyExchangeDecision Activity
            {
                Intent intent = new Intent(getApplicationContext(), KeyExchangeDecisionActivity.class);
                startActivity(intent);
                //finish(); //Wenn wir hier die Activity beenden, hat die Pfeiltaste bei android nichts mehr, wo sie zurück springen könnte und der Pfeil zurück schließt immer die App
                // So würde man mit der Pfeil zurück taste immer in der Main Activity landen.
            }
        });
    }
}

// QR-Code Scanner: https://www.youtube.com/watch?v=drH63NpSWyk&t=22s