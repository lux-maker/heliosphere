package com.javetest.helio;

import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

public class HashedPasswordInfo
{
    /*
    class to store hashed password information
     */
    HashedPasswordInfo(byte[] salt, SecretKeySpec secretKeySpec)
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
    boolean equals(HashedPasswordInfo other)
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
