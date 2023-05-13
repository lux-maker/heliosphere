package com.javetest.helio;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.Map;
import java.util.Random;

/**
 * class to eniterly delete all traces of personal data
 */
public class TotalAnnilihator {

    /**
     * function loads shared prefernces and replaces all values with random strings and than deletes the shared preferences and triggers garbage collection
     * @param context context of the app from which the function is called (basically getApplicationContext())
     */
    public void clearAll(Context context)
    {
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(context);
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(context, masterKey, "keys");
        SharedPreferences.Editor editor = settings.edit();

        //iterate through all keys and replace the values by random strings
        Map<String, ?> allKeys = settings.getAll();
        for (Map.Entry<String,?> key : allKeys.entrySet())
        {
            editor.putString(key.getKey(), generateRandomString(settings.getString(key.getKey(), "").length()));
        }

        editor.apply();
        editor.clear();
        editor.apply();

        //trigger garbage collection to free memory
        System.gc();
    }

    /**
     * @param length length of random String
     * @return randoms string with length length
     */
    private String generateRandomString(int length)
    {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

}
