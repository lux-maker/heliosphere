package com.javetest.helio;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class KeyExchangeDeleteKeyActivity extends AppCompatActivity {

    ListView listView;
    HashMap<String, String> publicKeyMap;
    String publicKeyMapJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //created by default
        //setContentView(R.layout.activity_key_exchange_delete_key); //created by default, but first load key list:
        setContentView(R.layout.activity_key_list); //created by default

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //zurückbutton initialisieren

        // assign view objects to code variables
        listView = (ListView) findViewById(R.id.list_view); //objekt, was sich auf den ListView aus dem GUI bezieht

        //load public keys from shared preferences + edit
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(getApplicationContext());
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(getApplicationContext(), masterKey, "keys");
        SharedPreferences.Editor editor = settings.edit();

        //load the public key map, an instance that maps key names to public keys
        publicKeyMapJson = settings.getString("publicKeyMap",""); //if preference does not exist, return ""

        //check if keymap contains any keys
        if (publicKeyMapJson.equals(""))
        {
            // no public keys exists -> since the own public key is added in CreatePasswordActivity, this can actually never happen if everything goes as planned
            //TODO how to handle this situation? -> catch error and avoid this situation
        }

        //if public key map exists in shared preferences, parse the string to a HashMap object
        publicKeyMap = GsonHelper.fromJson(publicKeyMapJson, new TypeToken<HashMap<String, String>>(){}.getType()); //enthält alle public keys jeweils mit dem key namen indiziert
        //add all keys from the hashmap to a string array and include the resulting array in the ArrayAdapter to display it on screen
        String[] keys = publicKeyMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);
        listView.setAdapter(listAdapter); //Zeigt die public keys in List Viewer in der APP an.

        //implement a Callable for the List
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get the name of the key that was selected
                String itemName = listView.getItemAtPosition(position).toString();
                String publicKeyString = publicKeyMap.get(itemName);

                //abfrage ob es sich um den own key handelt:
                if (itemName.equals("own key")){

                    Toast.makeText(KeyExchangeDeleteKeyActivity.this, "Own key cannot be deleted", Toast.LENGTH_SHORT).show();
                } else {

                    //frage ab, ob der key wirklich gelöscht werden soll:
                    //create a alert dialog box to confirm the users decision
                    AlertDialog.Builder alert = new AlertDialog.Builder(KeyExchangeDeleteKeyActivity.this);
                    alert.setTitle("Delete Key:" + itemName);
                    alert.setMessage("Are you sure you want to delete " + itemName + "? The key can not be restored and communication must be established from scratch again.");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            publicKeyMap.remove(itemName);
                            publicKeyMapJson = GsonHelper.toJson(publicKeyMap); //serialize public key as json
                            editor.putString("publicKeyMap", publicKeyMapJson); //and store it in settings in publicKeyMap

                            // apply changes to shared preferences
                            editor.apply();

                            Toast.makeText(KeyExchangeDeleteKeyActivity.this, itemName + " deleted and removed", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), KeyExchangeDeleteKeyActivity.class);
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //mache nichts
                            Toast.makeText(KeyExchangeDeleteKeyActivity.this, "No keys deleted", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), KeyExchangeDeleteKeyActivity.class);
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });
                    alert.show();
                }
            }
        });
    }


    //Zurückbutton richtung festlegen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(KeyExchangeDeleteKeyActivity.this, KeyExchangeDecisionActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}