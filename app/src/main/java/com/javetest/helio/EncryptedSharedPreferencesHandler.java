package com.javetest.helio;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class EncryptedSharedPreferencesHandler
{
    public static MasterKey getMasterKey(Context context)
    {
        MasterKey masterKey = null;
        try {
            masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        } catch (Exception e) {
            Log.e("EncryptedSharedPreferencesHandler", "Master Key building exception", e);
        }
        return masterKey;
    }

    public static SharedPreferences getESP(Context context, MasterKey masterKey, String name)
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
            Log.e("EncryptedSharedPreferencesHandler", "EncryptedSharedPreferences building exception", e);
        }
        return settings;
    }
}
