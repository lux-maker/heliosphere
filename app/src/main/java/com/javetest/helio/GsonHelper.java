package com.javetest.helio;

import com.google.gson.Gson;

import java.util.HashMap;

import javax.crypto.spec.SecretKeySpec;

public class GsonHelper {

    /**
    transforms an instance of HashedPasswordInfo into a String; the returned String has the form: >> salt ; hash ; algorithm <<
    */
    public static String HashedPWInfo2String(HashedPasswordInfo hashedPasswordInfo)

    {
        Gson gson = new Gson();
        String json_salt = gson.toJson(hashedPasswordInfo.getSalt()); //enhält den salt
        String json_keySpec_key = gson.toJson(hashedPasswordInfo.getSecretKeySpec().getEncoded()); //enhält den Hash des keys
        String json_keySpec_alogrithm = gson.toJson(hashedPasswordInfo.getSecretKeySpec().getAlgorithm()); //enhält das Verschlüsselungsverfahren "algorithm": "AES"
        return json_salt+";"+json_keySpec_key+";"+json_keySpec_alogrithm; //setze die Strings mit ; zusammen, danach können wir sie durch ";" später wieder trennen.
    }

    /**
     transforms a String of the form >> salt ; hash ; algorithm << into an instance of HashedPasswordInfo
    */
    public static HashedPasswordInfo String2HashedPWInfo(String string)

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

    /**
     transforms any generic object into a json string
     */
    public static <T> String toJson(T object) //generic function with template T
    {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    //TODO implement inverse of toJson
}