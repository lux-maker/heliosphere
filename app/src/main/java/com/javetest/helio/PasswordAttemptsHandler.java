package com.javetest.helio;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.MasterKey;

public class PasswordAttemptsHandler {

    /**
     * @return the number of failed attempts until the app resets itself
     */
    public static int getMaxAllowedNumOfFailedAttempts()
    {
        return 3;
    }

    /**
     *
     * @param context curremt application context
     * @return how many failed attempts were registered since the last time the password was entered correctly
     */
    public static int getCurrentFailedAttemptsCounter(Context context)
    {
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(context);
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(context, masterKey, "keys");

        String string = settings.getString("failedAccessAttempts", "");
        return Integer.parseInt(string);
    }

    /**
     * function stores the failed attempts counter in the memory
     * @param context application context
     * @param failedAttemptsCounter the number of failed attempts since the password was entered correctly the last time
     */
    public static void setCurrentFailedAttemptsCounter(Context context, int failedAttemptsCounter)
    {
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(context);
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(context, masterKey, "keys");

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("failedAccessAttempts", Integer.toString(failedAttemptsCounter));

        editor.apply();
    }

    /**
     *
     * @param context application context
     * @return the number iof attempts until the app resets itself
     */
    public static int getLeftFailedAttempts(Context context)
    {
        MasterKey masterKey = EncryptedSharedPreferencesHandler.getMasterKey(context);
        SharedPreferences settings = EncryptedSharedPreferencesHandler.getESP(context, masterKey, "keys");

        String string = settings.getString("failedAccessAttempts", "");
        return getMaxAllowedNumOfFailedAttempts() - Integer.parseInt(string);
    }
}
