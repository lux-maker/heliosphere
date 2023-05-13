package com.javetest.helio;

public class UnexpectedConnectivityExceptions extends Exception{
    public UnexpectedConnectivityExceptions(String errorMessage)
    {
        super(errorMessage);
    }
}
