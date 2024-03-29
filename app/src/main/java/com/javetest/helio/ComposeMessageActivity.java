package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * generelle Infos:
 * Die Activity wird von der Main Activity gestartet, wenn dort auf den Compose and Encrypt message button gedrückt wird.
 * Hier kann der zu verschlüsselnde Nachrichten-Text eingetippt werden und mit dem "ENCRYPT AND SEND" button bestätigt werden.
 * Anschließend wird die KeyList Activity gestartet und der Text übergeben.
 */

public class ComposeMessageActivity extends AppCompatActivity {

    Button confirmButton;
    EditText composedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        setContentView(R.layout.activity_compose_message); //created by default

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //zurückbutton initialisieren

        // assign view objects to code variables
        composedMessage = (EditText) findViewById(R.id.editTextTextMultiLine); //objekt, was sich auf das Textfeld im GUI bezieht, darüber steht "enter your message", inputType="textMultiLine"
        confirmButton = (Button) findViewById(R.id.button6); //objekt, was sich auf den "ENCRYPT AND SEND" button aus dem GUI bezieht

        //implement a Callable for a "ENCRYPT AND SEND" button click
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) //when clicked: speichert die Message und startet KeyList Activity und übergibt den eingegeben Text mit
            {
                String message = composedMessage.getText().toString();

                //+++++++++++message splitting Test+++++++++++
                //überprüfen, ob der Text zu lang für die RSA blöcke und QR Codes ist
                MessageSplittingHandler messageSplittingHandler = new MessageSplittingHandler(20, 255); //use the values (maxNumberOfChunks = 20 /chars = 255) of the class

                //Fehlermeldung, wenn der Text zu lang ist:
                if (messageSplittingHandler.loadMessage(message) == false){
                    Toast.makeText(ComposeMessageActivity.this, "Text too long. Please cut and try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ComposeMessageActivity.this, KeyListActivity.class);
                intent.putExtra("clearMessage", message);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(ComposeMessageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        finish();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        finish();
    }
}