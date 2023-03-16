package com.javetest.helio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button buttonScan, buttonCompose, buttonKeyExchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonScan = (Button) findViewById(R.id.button3);
        buttonCompose = (Button) findViewById(R.id.button4);
        buttonKeyExchange = (Button) findViewById(R.id.button5);

        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

//https://www.youtube.com/watch?v=drH63NpSWyk&t=22s