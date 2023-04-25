package com.javetest.helio;

import com.google.gson.Gson;

import javax.crypto.spec.SecretKeySpec;

public class GsonHelper {

    public static String HashedPWInfo2String(HashedPasswordInfo hashedPasswordInfo)
    /*
    transforms an instance of HashedPasswordInfo into a String
    the returned String has the form: >> salt ; hash ; algorithm <<
    */
    {
        Gson gson = new Gson();
        String json_salt = gson.toJson(hashedPasswordInfo.getSalt()); //enhält den salt
        String json_keySpec_key = gson.toJson(hashedPasswordInfo.getSecretKeySpec().getEncoded()); //enhält den Hash des keys
        String json_keySpec_alogrithm = gson.toJson(hashedPasswordInfo.getSecretKeySpec().getAlgorithm()); //enhält das Verschlüsselungsverfahren "algorithm": "AES"
        return json_salt+";"+json_keySpec_key+";"+json_keySpec_alogrithm; //setze die Strings mit ; zusammen, danach können wir sie durch ";" später wieder trennen.
    }

    public static HashedPasswordInfo String2HashedPWInfo(String string)
    /*
         inverse of the HashedPWInfo2String
    */
    {
        Gson gson = new Gson();

        // split the string at ";" and assemble an instance of HashedPasswordInfo
        String[] json_split = string.split(";"); //string = "salt ; hash ; algorithm"
        byte[] pw_salt = gson.fromJson(json_split[0], byte[].class);
        byte[] pw_SecretKeySpec_key = gson.fromJson(json_split[1], byte[].class);
        String pw_SecretKeySpec_algorithm = gson.fromJson(json_split[2], String.class);
        SecretKeySpec secretKeySpec = new SecretKeySpec(pw_SecretKeySpec_key, pw_SecretKeySpec_algorithm); //erstelle ein secretKeySpec Objekt, damit wir das für die HashedPasswordInfo class verwenden können

        return new HashedPasswordInfo(pw_salt, secretKeySpec); //Objekt enthält Hash-Wert des App-PWs und den Salt
    }
}