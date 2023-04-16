package com.javetest.helio;

import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;
/**
 * generelle Infos:
 * class to store hashed password information
 * enhält salt und secretKeySpec
 */
public class HashedPasswordInfo
{

    HashedPasswordInfo(byte[] salt, SecretKeySpec secretKeySpec) //Constructor
    {
        salt_ = salt;
        secretKeySpec_ = secretKeySpec;
    }
    //getter functions
    SecretKeySpec getSecretKeySpec() {return secretKeySpec_;}
    byte[] getSalt()
    {
        return salt_;
    }


    //override the "==" operator
    boolean equals(HashedPasswordInfo other) //ermöglicht das Abgleichen von dem Has-Wert eines externen PWs mit dem Hash-Wert des in der Klasse gespeicherten PWs, gibt true zurück, wenn PWs gleich
    {
        // perform sanity checks
        if (null == other) return false;
        if (!(other instanceof HashedPasswordInfo)) return false;

        //compare the hashed passwords from both objects
        return Arrays.equals(this.secretKeySpec_.getEncoded(), other.getSecretKeySpec().getEncoded());
    }

    private byte[] salt_;
    SecretKeySpec secretKeySpec_;
}
