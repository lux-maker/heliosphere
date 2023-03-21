package com.javetest.helio;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;

public class HelperFunctionsStringByteEncoding
{
    public static byte[] string2byte(String string)
    {
        return Base64.decode(string, Base64.DEFAULT);
        /*
        byte[] returnBytes = null;
        try
        {
            returnBytes = string.getBytes(StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsStringByteEncoding", "string2byte", e);
        }
        return returnBytes;
        */
    }

    public static String byte2string(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
        /*
        String returnString = null;
        try
        {
            returnString = new String(bytes, StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsStringByteEncoding", "byte2string", e);
        }
;       return returnString;
        */
    }
}
