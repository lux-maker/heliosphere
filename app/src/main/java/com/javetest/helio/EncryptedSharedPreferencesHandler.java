package com.javetest.helio;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * generelle Infos:
 * EncryptedSharedPreferences is a implementation of SharedPreferences that encrypts keys and values.
 * sharedPreferences: Data Storage option in Android.
 * Shared Preferences is the way in which one can store and retrieve small amounts of primitive data as key/value pairs to a file on the device storage
 * that make up your preferences in an XML file inside the app on the device storage.
 *
 * Interface for accessing and modifying preference data returned by Context.getSharedPreferences(String, int).
 * For any particular set of preferences, there is a single instance of this class that all clients share.
 * Modifications to the preferences must go through an Editor object to ensure the preference values remain in a consistent state and control when they are committed to storage.
 *
 * In den shared Preferences werden eigentlich nur jsons gespeichert.
 *
 * TOOD:The preference file should not be backed up with Auto Backup.
 * When restoring the file it is likely the key used to encrypt it will no longer be present.
 * You should exclude all EncryptedSharedPreferences from backup using backup rules.
 */
public class EncryptedSharedPreferencesHandler
{
    public static MasterKey getMasterKey(Context context)
    //gibt einen neu erzeugten master key wieder
    {
        MasterKey masterKey = null;
        try {
            masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        } catch (Exception e) {
            Log.e("EncryptedSharedPreferencesHandler", "Master Key building exception", e); //logging, when error occurs
        }
        return masterKey;
    }


    public static SharedPreferences getESP(Context context, MasterKey masterKey, String name)
    //gibt unverschlüsselten Inhalt der SharedPreferences aus dem file spezifiziert im Parameter name wieder
    {
        SharedPreferences settings = null;
        try
        {
            settings = EncryptedSharedPreferences.create(
                    context, name, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e("EncryptedSharedPreferencesHandler", "EncryptedSharedPreferences building exception", e); //logging, when error occurs
        }
        return settings;
    }
}
