package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ComposeMessageActivity extends AppCompatActivity {

    Button confirmButton;
    EditText composedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        // assign view objects to code variables
        composedMessage = (EditText) findViewById(R.id.editTextTextMultiLine);
        confirmButton = (Button) findViewById(R.id.button6);

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String message = composedMessage.getText().toString();
                Intent intent = new Intent(ComposeMessageActivity.this, KeyListActivity.class);
                intent.putExtra("clearMessage", message);
                startActivity(intent);
                finish();
            }
        });
    }
}