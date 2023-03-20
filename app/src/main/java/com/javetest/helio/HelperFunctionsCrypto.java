package com.javetest.helio;


import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class HelperFunctionsCrypto
{
    /*
    class contains static functions for cryptographic operations
    -> use char arrays instead of strings since they are mutable -> memory can be overwritten

    References:
    https://www.baeldung.com/java-password-hashing
    https://code.tutsplus.com/de/tutorials/storing-data-securely-on-android--cms-30558
     */

    public static HashMap<String, byte[]> encryptBytes(byte[] plainTextBytes, char[] clearPassword)
    {
        HashMap<String, byte[]> map = new HashMap<String, byte[]>();

        try //handle all kinds of exceptions TODO sollte hier auf einige exceptions speziell eingegangen werden?
        {
            // hash password and generate salt
            HashedPasswordInfo hashedPasswordInfo = hashPassword(clearPassword);

            //generate initialization vector
            SecureRandom sr = new SecureRandom(); //must definitely be a new instance of SecureRandom (don't reuse the instance previously generated to obtain the salt
            byte[] iv = new byte[16]; // TODO benutzt der algorithmus 16 byte blocks? wird die sicherheit verbessert für groessere bloecke?
            sr.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            //finally encrypt the data
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, hashedPasswordInfo.getSecretKeySpec(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainTextBytes);

            //store everything in the HashMap
            map.put("salt", hashedPasswordInfo.getSalt());
            map.put("iv", iv);
            map.put("encrypted", encrypted);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsCrypto", "encryption exception", e);
        }
        return map;
    }

    private static byte[] decryptData(HashMap<String, byte[]> map, char[] clearPassword)
    {
        byte[] decrypted = null; //forward declaration to return outside try() block
        try
        {
            //fetch information from map
            byte[] salt = map.get("salt");
            byte[] iv = map.get("iv");
            byte[] encrypted = map.get("encrypted");

            //regenerate key from password
            HashedPasswordInfo hashedPasswordInfo = hashPassword(salt, clearPassword);

            //Decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, hashedPasswordInfo.getSecretKeySpec(), ivSpec); //TODO was passiert bei falschem passwort?
            decrypted = cipher.doFinal(encrypted);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsCrypto", "decryption exception", e);
        }
        return decrypted;
    }

    //define overloaded function to enable the use of optional salt parameter
    public static HashedPasswordInfo hashPassword(byte[] salt, char[] clearPassword)
    {
        /*
        return the hash of the provided string
        */

        HashedPasswordInfo hashedPasswordInfo = null;
        try
        {
            KeySpec pbeKeySpec = new PBEKeySpec(clearPassword, salt, 65536, 128); //iterationCount can be used to slow down the algorithm -> increases protection level against brute force attacks
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hashedPasswordBytes = secretKeyFactory.generateSecret(pbeKeySpec).getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(hashedPasswordBytes, "AES");
            hashedPasswordInfo = new HashedPasswordInfo(salt, secretKeySpec);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsCrypto", "hashPassword exception", e);
        }
        return hashedPasswordInfo;
    }

    public static HashedPasswordInfo hashPassword(char[] clearPassword)
    {
        return hashPassword(getSalt(), clearPassword);
    }

    public static byte[] getSalt()
    {
        /*
        generates salt to modify password before hashing -> avoids hash collisions
        */
        byte[] salt = null;
        try
        {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG"); //SHA1PRNG is a cryptographic pseudo-random number generator algorithm that is used to generate random bytes for cryptographic purposes.
            //generate 256 random bytes
            salt = new byte[256];
            sr.nextBytes(salt);
        }
        catch(Exception e)
        {
            Log.e("HelperFunctionsCrypto", "salt generation exception", e);
        }

        return salt;
    }
}
