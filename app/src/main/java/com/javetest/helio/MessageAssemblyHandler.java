package com.javetest.helio;

import android.util.Log;

import com.google.zxing.common.StringUtils;

/**
    class assembles messages from single message junks
    -> it reads and extracts the position number from every junk
    -> it returns true if the message is complete, i.e. all junks are assembled
    -> it returns the RSA blocks separately
*/
public class MessageAssemblyHandler
{
    private boolean firstJunk = true; //will be set to false after hte first message junk was loaded
    int totalNumberOfJunks; // will hold the number of junks of the message after the first message junk was loaded
    String[] messageJunks; // will contain the message junks in an ordered manner

    /**
     * the function enables the class to load message junks consecutively
     * @param messageJunk a concatenation of the position number and the actual message junk
     * @return true if the laoded messageJunk is the last one to assemble the entire message, otherwise falls
     */
    public boolean loadMessageJunk(String messageJunk)
    {
        if (this.firstJunk) //then this function is called for the first time since the instance of this class was created
        {
            //extract the total number of junks and initialize the string message junk array to null
            this.totalNumberOfJunks = extractPositionNumberTuple(messageJunk)[0];
            messageJunks = new String[this.totalNumberOfJunks];
            this.firstJunk = false;
        }

        int currentPosition = extractPositionNumberTuple(messageJunk)[1];
        this.messageJunks[currentPosition] = extractMessage(messageJunk);

        return this.allJunksLoaded();
    }

    /**
     * @return the ordered message junks, without the position number, but not processed in any other way
     */
    public String[] getOrderedMessageJunks()
    {
        return this.messageJunks;
    }

    /**
     * the function takes the message junks from the QR-codes, combines them and cuts them in order to return the distinct RSA blocks
     * @return the distinct RSA blocks
     */
    public String[] getRSABlocks()
    {
        // TODO soll diese methode nur funktionieren wenn alle junks geladen wurden?
        if (!this.allJunksLoaded())
        {
            Log.w("MessageAssemblyHandler.getRSABlocks", "RSA blocks were requested but not all message junks have been loaded yet");
            return new String[1];
        }

        // transform the array that holds the message junks into one single String
        String entireMessage = this.messageJunks.toString();

        //RSA blocks end with "==\n" zumindest glauben wir das grade sollte eventuell nochmal gecheckt werden lol
        String[] rsaBlocks = entireMessage.split("==\n"); //die funktion String.split() gibt die Strings zurück ohne das matching pattern.
        return rsaBlocks;
    }

    /**
     * the function extracts the position numbers from the message junk
     * @param messageJunk a concatenation of the position number and the actual message junk
     * @return a tuple of ints, the first one is the total number of expected junks, the second one is the position of the current junk within the message
     */
    private int[] extractPositionNumberTuple(String messageJunk)
    {
        //extract the position number from the string (the first two character) and parse it to integer
        int positionNumber = Integer.parseInt(messageJunk.substring(0,2));

        /*
        * das genaue vorgehen ist in der Git Readme hinterlegt
        * es wird die Gaußsche summenformel verwendet
        */

        //TODO folgende Zeilen debugen
        int totalNumberOfJunks = (int) Math.ceil(-0.5 + Math.sqrt(0.25 + positionNumber * 2.0));
        int currentPosition = (((totalNumberOfJunks + 1) * totalNumberOfJunks) / 2) - positionNumber;

        return new int[]{totalNumberOfJunks, currentPosition};
    }

    /**
     * the function extracts the actual message from the message junk
     * @param messageJunk  a concatenation of the position number and the actual message junk
     * @return the actual message
     */
    private String extractMessage(String messageJunk)
    {
        return messageJunk.substring(2);
    }

    /**
     * @return returns true if all junks are loaded, false otherwise
     */
    private boolean allJunksLoaded()
    {
        //iterate through the String array and check if an value is null
        for (String value : this.messageJunks) if (value == null) return false;
        return true;
    }
}
